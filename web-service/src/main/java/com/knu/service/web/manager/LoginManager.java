package com.knu.service.web.manager;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import service.LoginServiceGrpc;
import service.login.ClientInfoOuterClass;
import service.login.StatusOuterClass;

public class LoginManager {

    private ManagedChannel channel;
    private final LoginServiceGrpc.LoginServiceBlockingStub blockingStub;

    public LoginManager(String host, String port) {

        channel = ManagedChannelBuilder.forTarget(host + ":" + port)
                .usePlaintext()
                .build();

        blockingStub = LoginServiceGrpc.newBlockingStub(channel);

    }

    public StatusOuterClass.Status signIn(ClientInfoOuterClass.ClientInfo clientInfo) {

        return blockingStub.signIn(clientInfo);

    }

    public StatusOuterClass.Status signIn(String username, String password) {

        ClientInfoOuterClass.ClientInfo clientInfo = ClientInfoOuterClass.ClientInfo.newBuilder()
                .setUsername(username)
                .setPassword(password)
                .build();

        return blockingStub.signIn(clientInfo);

    }

    public StatusOuterClass.Status signUp(ClientInfoOuterClass.ClientInfo clientInfo) {

        return blockingStub.signUp(clientInfo);

    }

    public StatusOuterClass.Status changeInfo(ClientInfoOuterClass.ClientInfo clientInfo) {

        return blockingStub.changeInfo(clientInfo);

    }
}
