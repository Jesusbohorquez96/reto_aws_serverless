package org.example.users.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.Map;

public class SendEmailHandler implements RequestHandler<Map<String, Object>, String> {

    private final SnsClient snsClient = SnsClient.create();
    private final SqsClient sqsClient = SqsClient.create();
    private final String snsTopicArn = System.getenv("SNS_TOPIC_ARN");
    private final String userQueueUrl = System.getenv("USER_QUEUE_URL");
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String handleRequest(Map<String, Object> event, Context context) {
        try {
            var records = (Iterable<Map<String, Object>>) event.get("Records");
            for (Map<String, Object> record : records) {
                String body = (String) record.get("body");
                JsonNode jsonBody = objectMapper.readTree(body);
                String name = jsonBody.get("name").asText();
                String email = jsonBody.get("email").asText();
                publishToSNS(name, email);
            }
            return "All messages processed successfully.";
        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
            return "Error processing messages.";
        }
    }

    public void sendMessageToQueue(String messageBody) {
        try {
            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(userQueueUrl)
                    .messageBody(messageBody)
                    .build();
            sqsClient.sendMessage(sendMessageRequest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send message to SQS: " + e.getMessage(), e);
        }
    }

    private void publishToSNS(String name, String email) {
        try {
            String subject = "Welcome, " + name;
            String message = "Hello " + name + ",\n\nYour account with email " + email + " has been created.";
            PublishRequest request = PublishRequest.builder()
                    .topicArn(snsTopicArn)
                    .subject(subject)
                    .message(message)
                    .build();
            snsClient.publish(request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish message to SNS: " + e.getMessage(), e);
        }
    }
}
