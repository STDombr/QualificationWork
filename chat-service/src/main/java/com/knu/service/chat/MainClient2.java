package com.knu.service.chat;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import service.ChatServiceGrpc;
import service.ClientInfoOuterClass;
import service.StatusOuterClass;
import service.chat.ChatInfoOuterClass;
import service.chat.ChatMessage;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class MainClient2 {
    private static final Logger logger = Logger.getLogger(MainClient2.class.getName());

    private final ChatServiceGrpc.ChatServiceBlockingStub blockingStub;
    private final ChatServiceGrpc.ChatServiceStub stub;

    public MainClient2(Channel channel) {
        blockingStub = ChatServiceGrpc.newBlockingStub(channel);
        stub = ChatServiceGrpc.newStub(channel);
    }

    public void login(ClientInfoOuterClass.ClientInfo clientInfo) {

        StreamObserver<ChatInfoOuterClass.ChatInfo> observer = new StreamObserver<ChatInfoOuterClass.ChatInfo>() {
            @Override
            public void onNext(ChatInfoOuterClass.ChatInfo value) {
                logger.info("Created new chat: " + value.toString());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                logger.info("Server closed stream with receiving new chats to client: " + clientInfo);
            }
        };

        stub.login(clientInfo, observer);

    }

    public void getAllChats(ClientInfoOuterClass.ClientInfo clientInfo) {

        ChatInfoOuterClass.ChatInfoList list = blockingStub.getAllChats(clientInfo);

        logger.info("Read chats: " + list);

    }

    public void openChat(ChatInfoOuterClass.ChatInfo chatInfo) {

        StreamObserver<ChatMessage.ChatResponse> observer = new StreamObserver<ChatMessage.ChatResponse>() {
            @Override
            public void onNext(ChatMessage.ChatResponse value) {
                logger.info("Received new message: " + value);
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                logger.info("Server closed stream with chat: " + chatInfo);
            }
        };

        stub.openChat(chatInfo, observer);

    }

    public void sendMessage(ChatMessage.ChatRequest chatRequest) {

        StatusOuterClass.Status status = blockingStub.sendMessage(chatRequest);

        System.out.println(status);

    }

    public void closeChat(ChatInfoOuterClass.ChatInfo chatInfo) {

        StatusOuterClass.Status status = blockingStub.closeChat(chatInfo);

        System.out.println(status);

    }

    public static void main(String[] args) throws Exception {

        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:5566")
                .usePlaintext()
                .build();

        MainClient2 client = new MainClient2(channel);

        ClientInfoOuterClass.ClientInfo clientInfo = ClientInfoOuterClass.ClientInfo.newBuilder()
                .setClientId("2")
                .build();

        client.login(clientInfo);

        ChatInfoOuterClass.ChatInfo chatInfo = ChatInfoOuterClass.ChatInfo.newBuilder()
                .setChatId("1")
                .setSenderId("2")
                .setRecipientId("1")
                .build();

        client.openChat(chatInfo);

        ChatMessage.ChatRequest request = ChatMessage.ChatRequest.newBuilder()
                .setChatInfo(chatInfo)
                .setBody("Test message from TestUser")
                .build();

        while (true) {
            System.out.println();
            client.sendMessage(request);
            Thread.sleep(10000);
        }

        /*client.closeChat(chatInfo);

        channel.awaitTermination(30, TimeUnit.SECONDS);*/

    }
}