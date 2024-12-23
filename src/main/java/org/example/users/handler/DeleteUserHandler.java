package org.example.users.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.example.users.service.UserService;
import org.example.users.util.Constants;

import java.util.HashMap;
import java.util.Map;

public class DeleteUserHandler implements RequestHandler<Map<String,Object>, Map<String,Object>> {
    private final UserService userService = new UserService();

    @Override
    public Map<String,Object> handleRequest(Map<String,Object> event, Context context) {
        try {
            Map<String,String> pathParameters = (Map<String,String>) event.get("pathParameters");
            String id = pathParameters.get("id");
            boolean deleted = userService.deleteUser(id);

            Map<String,Object> response = new HashMap<>();
            if (!deleted) {
                response.put(Constants.STATUS_CODE, 404);
                response.put(Constants.BODY, Constants.USER_NOT_FOUND);
            } else {
                response.put(Constants.STATUS_CODE, 204);
                response.put(Constants.BODY, "");
            }
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