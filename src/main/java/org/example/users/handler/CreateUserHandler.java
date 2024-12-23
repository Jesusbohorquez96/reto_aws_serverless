package org.example.users.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.users.model.User;
import org.example.users.service.UserService;

import java.util.HashMap;
import java.util.Map;

public class CreateUserHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    public CreateUserHandler() {
        this.userService = new UserService();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        try {
            String body = (String) event.get("body");
            User user = objectMapper.readValue(body, User.class);

            User createdUser = userService.createUser(user);

            Map<String, Object> response = new HashMap<>();
            response.put("statusCode", 201);
            response.put("body", objectMapper.writeValueAsString(createdUser));
            return response;
        } catch (Exception e) {
            context.getLogger().log("Error creating user: " + e.getMessage());
            return buildErrorResponse(e.getMessage());
        }
    }

    private Map<String, Object> buildErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 500);
        response.put("body", "{\"error\": \"" + message + "\"}");
        return response;
    }
}
