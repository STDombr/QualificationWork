package com.knu.service.web.manager;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import service.ChatServiceGrpc;
import service.chat.*;

public class ChatManager {

    private ManagedChannel channel;
    private final ChatServiceGrpc.ChatServiceBlockingStub blockingStub;
    private final ChatServiceGrpc.ChatServiceStub stub;

    private boolean isLogged = false;

    public ChatManager(String host, String port) {

        channel = ManagedChannelBuilder.forTarget(host + ":" + port)
                .usePlaintext()
                .build();

        blockingStub = ChatServiceGrpc.newBlockingStub(channel);
        stub = ChatServiceGrpc.newStub(channel);

    }

    public void login(ClientInfoOuterClass.ClientInfo clientInfo) {

        StreamObserver<UnsolicitedMessageOuterClass.UnsolicitedMessage> observer = new StreamObserver<UnsolicitedMessageOuterClass.UnsolicitedMessage>() {
            @Override
            public void onNext(UnsolicitedMessageOuterClass.UnsolicitedMessage value) {

                UnsolicitedMessageOuterClass.UnsolicitedMessage.MessageCase messageCase = value.getMessageCase();

                switch (messageCase) {
                    case STATUS:

                        StatusOuterClass.Status.Enum e = value.getStatus().getEnum();
                        if (e == StatusOuterClass.Status.Enum.LOGIN_SUCCESS) {
                            isLogged = true;
                        } else if (e == StatusOuterClass.Status.Enum.LOGOUT_SUCCESS) {
                            isLogged = false;
                        }

                        break;
                    case CHAT_RESPONSE:

                        break;

                    case CHAT_INFO:

                        break;
                }
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        };

        stub.login(clientInfo, observer);

    }

    public void logout(ClientInfoOuterClass.ClientInfo clientInfo) {

        StatusOuterClass.Status status = blockingStub.logout(clientInfo);

        if (status.getEnum() == StatusOuterClass.Status.Enum.LOGOUT_SUCCESS) {
            isLogged = false;
        }

    }

    public QuestionOuterClass.Question getQuestion() {
        return blockingStub.getQuestion(null);
    }

    public void sendAnswer(QuestionOuterClass.Answer answer) {
        blockingStub.sendAnswer(answer);
    }

    public ChatInfoOuterClass.ChatInfoList getAllChats(ClientInfoOuterClass.ClientInfo clientInfo) {
        return blockingStub.getAllChats(clientInfo);
    }

    public ChatMessage.ChatResponseList getChatMessages(ChatInfoOuterClass.ChatInfo clientInfo) {
        return blockingStub.getChatMessages(clientInfo);
    }

    public void sendMessage(ChatMessage.ChatRequest chatRequest) {

        StatusOuterClass.Status status = blockingStub.sendMessage(chatRequest);

    }

    public boolean isLogged() {
        return isLogged;
    }

}
