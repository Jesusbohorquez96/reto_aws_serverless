package org.example.users.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.example.users.handler.SendEmailHandler;
import org.example.users.model.User;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserService {

    private final DynamoDbTable<UserDynamo> table;
    private final SendEmailHandler emailHandler;
    private final ObjectMapper objectMapper;

    public UserService() {
        DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
                .region(Region.US_EAST_2)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();

        String tableName = System.getenv("USERS_TABLE");
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(UserDynamo.class));
        this.emailHandler = new SendEmailHandler();
        this.objectMapper = new ObjectMapper();
    }

    public List<User> getAllUsers() {
        List<User> result = new ArrayList<>();
        table.scan().items().forEach(ud -> result.add(toUser(ud)));
        return result;
    }

    public Optional<User> getUserById(String id) {
        UserDynamo ud = table.getItem(Key.builder().partitionValue(id).build());
        return ud == null ? Optional.empty() : Optional.of(toUser(ud));
    }

    public User createUser(User user) {
        String newId = UUID.randomUUID().toString();
        user.setId(newId);
        table.putItem(toUserDynamo(user));
        try {
            String messageBody = objectMapper.writeValueAsString(user);
            emailHandler.sendMessageToQueue(messageBody);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing user or sending message to SQS: " + user, e);
        }
        return user;
    }

    public User updateUser(String id, User user) {
        Optional<User> existing = getUserById(id);
        if (existing.isEmpty()) return null;
        user.setId(id);
        table.putItem(toUserDynamo(user));
        return user;
    }

    public boolean deleteUser(String id) {
        UserDynamo ud = table.deleteItem(Key.builder().partitionValue(id).build());
        return ud != null;
    }

    private User toUser(UserDynamo ud) {
        return new User(ud.getId(), ud.getName(), ud.getEmail());
    }

    private UserDynamo toUserDynamo(User u) {
        UserDynamo ud = new UserDynamo();
        ud.setId(u.getId());
        ud.setName(u.getName());
        ud.setEmail(u.getEmail());
        return ud;
    }

    @DynamoDbBean
    @Data
    public static class UserDynamo {
        private String id;
        private String name;
        private String email;

        @DynamoDbPartitionKey
        public String getId() { return id; }
    }
}