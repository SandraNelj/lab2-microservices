package org.example.userservice;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.example.userservice.grpc.UserServiceGrpc;
import org.example.userservice.grpc.GetUserRequest;
import org.example.userservice.grpc.UserResponse;

@GrpcService
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    private final UserRepository userRepository;

    public UserGrpcService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void getUser(GetUserRequest request, StreamObserver<UserResponse> responseObserver) {
        String username = request.getUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        UserResponse response = UserResponse.newBuilder()
                .setId(user.getId())
                .setUsername(user.getUsername())
                .setEmail(user.getEmail() != null ? user.getEmail() : "")
                .setDisplayName(user.getDisplayName() != null ? user.getDisplayName() : "")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}