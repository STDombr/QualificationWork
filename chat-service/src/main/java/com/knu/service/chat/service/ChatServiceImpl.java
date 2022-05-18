package com.knu.service.chat.service;

import com.google.protobuf.Empty;
import com.knu.service.chat.manager.ChatMessagesManager;
import com.knu.service.chat.manager.DBManager;
import com.knu.service.chat.manager.NewPairsNotification;
import com.knu.service.chat.manager.PropertiesManager;
import io.grpc.stub.StreamObserver;
import service.ChatServiceGrpc;
import service.ClientInfoOuterClass;
import service.QuestionOuterClass;
import service.StatusOuterClass;
import service.chat.ChatInfoOuterClass;
import service.chat.ChatMessage;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class ChatServiceImpl extends ChatServiceGrpc.ChatServiceImplBase {

    private static final Logger logger = Logger.getLogger(ChatServiceImpl.class.getName());
    private final DBManager dbManager;
    private final NewPairsNotification pairsNotification;
    private final ChatMessagesManager chatMessagesManager;

    public ChatServiceImpl() throws IOException {

        PropertiesManager propertiesManager = new PropertiesManager("application.properties");

        dbManager = new DBManager(propertiesManager.getProperty("db.postgres.url"), propertiesManager.getProperty("db.postgres.user"), propertiesManager.getProperty("db.postgres.password"));

        pairsNotification = new NewPairsNotification(dbManager);

        chatMessagesManager = new ChatMessagesManager();

    }

    @Override
    public void login(ClientInfoOuterClass.ClientInfo request, StreamObserver<ChatInfoOuterClass.ChatInfo> responseObserver) {

        if (!pairsNotification.isLogged(request) && !chatMessagesManager.isLogged(request)) {
            pairsNotification.addNewClient(request, responseObserver);
            logger.info("Client: " + request + " - added to NewPairsNotification");
        } else {
            responseObserver.onError(new Exception("This client already logged: " + request));
        }

    }

    @Override
    public void logout(ClientInfoOuterClass.ClientInfo request, StreamObserver<StatusOuterClass.Status> responseObserver) {

        pairsNotification.removeClient(request);

        chatMessagesManager.removeChat(request);

        responseObserver.onNext(StatusOuterClass.Status.newBuilder()
                .setEnum(StatusOuterClass.Status.Enum.SUCCESS)
                .build());

        responseObserver.onCompleted();

    }

    @Override
    public void getQuestion(Empty request, StreamObserver<QuestionOuterClass.Question> responseObserver) {
        responseObserver.onNext(dbManager.getQuestion(System.currentTimeMillis()));
    }

    @Override
    public void sendAnswer(QuestionOuterClass.Answer request, StreamObserver<Empty> responseObserver) {

        if (pairsNotification.isLogged(request.getClientInfo())) {
            pairsNotification.addAnswer(request);
        }
    }

    @Override
    public void getAllChats(ClientInfoOuterClass.ClientInfo request, StreamObserver<ChatInfoOuterClass.ChatInfoList> responseObserver) {

        ChatInfoOuterClass.ChatInfoList list = dbManager.getAllChats(request);

        if (list != null) {
            responseObserver.onNext(list);
        }

        responseObserver.onCompleted();

    }

    @Override
    public void openChat(ChatInfoOuterClass.ChatInfo request, StreamObserver<ChatMessage.ChatResponse> responseObserver) {

        if (!chatMessagesManager.isLogged(request)) {
            chatMessagesManager.addNewClient(request, responseObserver);
            logger.info("Chat: " + request + " - added to ChatMessagesManager");
        } else {
            responseObserver.onError(new Exception("This chat already logged: " + request));
        }

        List<ChatMessage.ChatResponse> list = dbManager.getAllChatHistory(request);

        for (ChatMessage.ChatResponse response : list) {
            responseObserver.onNext(response);
        }

    }

    @Override
    public void sendMessage(ChatMessage.ChatRequest request, StreamObserver<StatusOuterClass.Status> responseObserver) {

        logger.info("Received ChatInfo request on chat server:\n" + request.toString());

        if (chatMessagesManager.isLogged(request.getChatInfo())) {

            ChatMessage.ChatResponse response = dbManager.addNewMessage(request);

            if (response != null) {

                chatMessagesManager.boardCast(response);

                responseObserver.onNext(StatusOuterClass.Status.newBuilder()
                        .setEnum(StatusOuterClass.Status.Enum.SUCCESS)
                        .build());

            } else {
                responseObserver.onNext(StatusOuterClass.Status.newBuilder()
                        .setEnum(StatusOuterClass.Status.Enum.ERROR)
                        .build());
            }

        } else {
            logger.warning("CHAT_NOT_OPENED");
            responseObserver.onNext(StatusOuterClass.Status.newBuilder()
                    .setEnum(StatusOuterClass.Status.Enum.CHAT_NOT_OPENED)
                    .build());
        }

        responseObserver.onCompleted();

    }

    @Override
    public void closeChat(ChatInfoOuterClass.ChatInfo request, StreamObserver<StatusOuterClass.Status> responseObserver) {

        chatMessagesManager.removeChat(request);

        responseObserver.onNext(StatusOuterClass.Status.newBuilder()
                .setEnum(StatusOuterClass.Status.Enum.SUCCESS)
                .build());

        responseObserver.onCompleted();

    }
}