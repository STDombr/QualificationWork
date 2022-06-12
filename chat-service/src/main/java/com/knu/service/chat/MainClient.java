package com.knu.service.chat;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import service.ChatServiceGrpc;
import service.chat.*;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class MainClient {
    private static final Logger logger = Logger.getLogger(MainClient.class.getName());

    private final ChatServiceGrpc.ChatServiceBlockingStub blockingStub;
    private final ChatServiceGrpc.ChatServiceStub stub;

    public MainClient(Channel channel) {
        blockingStub = ChatServiceGrpc.newBlockingStub(channel);
        stub = ChatServiceGrpc.newStub(channel);
    }

    public void login(ClientInfoOuterClass.ClientInfo clientInfo, StreamObserver<UnsolicitedMessageOuterClass.UnsolicitedMessage> observer) {

        stub.login(clientInfo, observer);

    }

    public void getAllChats(ClientInfoOuterClass.ClientInfo clientInfo) {

        ChatInfoOuterClass.ChatInfoList list = blockingStub.getAllChats(clientInfo);

        logger.info("Read chats: " + list);

    }

    public void getChatMessages(ChatInfoOuterClass.ChatInfo chatInfo) {

        ChatMessage.ChatResponseList list = blockingStub.getChatMessages(chatInfo);

        logger.info("Received messages: " + list);
    }

    public void sendMessage(ChatMessage.ChatRequest chatRequest) {

        StatusOuterClass.Status status = blockingStub.sendMessage(chatRequest);

        System.out.println(status);

    }

    public static void main(String[] args) throws Exception {

        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:5566")
                .usePlaintext()
                .build();

        MainClient client = new MainClient(channel);

        ClientInfoOuterClass.ClientInfo clientInfo = ClientInfoOuterClass.ClientInfo.newBuilder()
                .setClientId("1")
                .build();

        ChatInfoOuterClass.ChatInfo chatInfo = ChatInfoOuterClass.ChatInfo.newBuilder()
                .setChatId("1")
                .setSenderId("1")
                .setRecipientId("2")
                .build();

        ChatMessage.ChatRequest request = ChatMessage.ChatRequest.newBuilder()
                .setChatInfo(chatInfo)
                .setBody("Test message from STDombr")
                .build();

        StreamObserver<UnsolicitedMessageOuterClass.UnsolicitedMessage> observer = new StreamObserver<UnsolicitedMessageOuterClass.UnsolicitedMessage>() {
            @Override
            public void onNext(UnsolicitedMessageOuterClass.UnsolicitedMessage value) {
                switch (value.getMessageCase()) {
                    case STATUS:
                        if (value.getStatus().getEnum() == StatusOuterClass.Status.Enum.SUCCESS) {

                            client.getChatMessages(chatInfo);

                            client.sendMessage(request);

                        }
                        break;
                    case CHAT_INFO:
                        logger.info("Created new chat: " + value.toString());
                        break;
                    case CHAT_RESPONSE:
                        logger.info("Received new chat message: " + value.toString());
                        break;
                }
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                logger.info("Server closed stream with unsolicited messages");
            }
        };

        client.login(clientInfo, observer);

        while (true) {

        }
        /*client.closeChat(chatInfo);

        channel.awaitTermination(30, TimeUnit.SECONDS);*/

    }
}