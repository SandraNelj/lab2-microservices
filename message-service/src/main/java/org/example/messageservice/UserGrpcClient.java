package org.example.messageservice;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import org.example.userservice.grpc.UserServiceGrpc;
import org.example.userservice.grpc.GetUserRequest;
import org.example.userservice.grpc.UserResponse;

@Service
public class UserGrpcClient {

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userServiceStub;

    public String getUserDisplayName(String username) {
        try {
            GetUserRequest request = GetUserRequest.newBuilder().setUsername(username).build();

            UserResponse response = userServiceStub.getUser(request);
            return response.getDisplayName();
        } catch (Exception e) {
            return username;
        }
    }
}
