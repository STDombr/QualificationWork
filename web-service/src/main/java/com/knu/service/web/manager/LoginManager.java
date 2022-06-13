package com.knu.service.web.manager;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import service.LoginServiceGrpc;
import service.login.ClientInfoOuterClass;
import service.login.StatusOuterClass;

public class LoginManager {

    private ManagedChannel channel;
    private final LoginServiceGrpc.LoginServiceBlockingStub blockingStub;

    private boolean isLogged = false;
    private String clientId = null;

    public LoginManager(String host, String port) {

        channel = ManagedChannelBuilder.forTarget(host + ":" + port)
                .usePlaintext()
                .build();

        blockingStub = LoginServiceGrpc.newBlockingStub(channel);

    }

    public StatusOuterClass.Status signIn(ClientInfoOuterClass.ClientInfo clientInfo) {

        StatusOuterClass.Status status = blockingStub.signIn(clientInfo);

        if (status.getEnum() == StatusOuterClass.Status.Enum.SUCCESS) {
            isLogged = true;
            clientId = status.getClientId();
        }

        return status;

    }

    public StatusOuterClass.Status signUp(ClientInfoOuterClass.ClientInfo clientInfo) {

        return blockingStub.signUp(clientInfo);

    }

    public StatusOuterClass.Status changeInfo(ClientInfoOuterClass.ClientInfo clientInfo) {

        return blockingStub.changeInfo(clientInfo);

    }

    public boolean isLogged() {
        return isLogged;
    }

    public String getClientId() {
        return clientId;
    }
}
