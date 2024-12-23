package org.example.users.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.users.service.UserService;
import org.example.users.util.Constants;

import java.util.HashMap;
import java.util.Map;

public class GetUsersHandler implements RequestHandler<Map<String,Object>, Map<String,Object>> {
    private final UserService userService = new UserService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String,Object> handleRequest(Map<String,Object> input, Context context) {
        try {
            Map<String,Object> response = new HashMap<>();
            response.put(Constants.STATUS_CODE, 200);
            response.put(Constants.HEADERS, Map.of(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON));
            response.put(Constants.BODY, objectMapper.writeValueAsString(userService.getAllUsers()));
            return response;
        } catch (Exception e) {
            return errorResponse();
        }
    }

    private Map<String,Object> errorResponse() {
        Map<String,Object> response = new HashMap<>();
        response.put(Constants.STATUS_CODE, 500);
        response.put(Constants.HEADERS, Map.of(Constants.CONTENT_TYPE, Constants.TEXT_PLAIN));
        response.put(Constants.BODY, Constants.INTERNAL_SERVER_ERROR);
        return response;
    }
}