package com.knu.service.chat.service;

import com.google.protobuf.Empty;
import com.knu.service.chat.manager.ChatMessagesManager;
import com.knu.service.chat.manager.DBManager;
import com.knu.service.chat.manager.NewPairsNotification;
import com.knu.service.chat.manager.PropertiesManager;
import io.grpc.stub.StreamObserver;
import service.ChatServiceGrpc;
import service.chat.*;

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
    public void login(ClientInfoOuterClass.ClientInfo request, StreamObserver<UnsolicitedMessageOuterClass.UnsolicitedMessage> responseObserver) {

        if (!pairsNotification.isLogged(request) && !chatMessagesManager.isLogged(request)) {
            pairsNotification.addNewClient(request, responseObserver);
            logger.info("Client: " + request + " - added to NewPairsNotification");
            chatMessagesManager.addNewClient(request, responseObserver);
            logger.info("Client: " + request + " - added to ChatMessagesManager");

            UnsolicitedMessageOuterClass.UnsolicitedMessage message = UnsolicitedMessageOuterClass.UnsolicitedMessage.newBuilder()
                    .setStatus(StatusOuterClass.Status.newBuilder()
                            .setEnum(StatusOuterClass.Status.Enum.LOGIN_SUCCESS)
                            .build())
                    .build();

            responseObserver.onNext(message);

        } else {
            responseObserver.onError(new Exception("This client already logged: " + request));
        }

    }

    @Override
    public void logout(ClientInfoOuterClass.ClientInfo request, StreamObserver<StatusOuterClass.Status> responseObserver) {

        pairsNotification.removeClient(request);

        chatMessagesManager.removeClient(request);

        responseObserver.onNext(StatusOuterClass.Status.newBuilder()
                .setEnum(StatusOuterClass.Status.Enum.LOGOUT_SUCCESS)
                .build());

        responseObserver.onCompleted();

    }

    @Override
    public void getQuestion(Empty request, StreamObserver<QuestionOuterClass.Question> responseObserver) {
        responseObserver.onNext(dbManager.getQuestion(System.currentTimeMillis()));
        responseObserver.onCompleted();
    }

    @Override
    public void sendAnswer(QuestionOuterClass.Answer request, StreamObserver<Empty> responseObserver) {

        if (pairsNotification.isLogged(request.getClientInfo())) {
            pairsNotification.addAnswer(request);
        }
        responseObserver.onNext(null);
        responseObserver.onCompleted();
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
    public void getChatMessages(ChatInfoOuterClass.ChatInfo request, StreamObserver<ChatMessage.ChatResponseList> responseObserver) {

        if (chatMessagesManager.isLogged(request.getSenderId())) {
            ChatMessage.ChatResponseList list = dbManager.getAllChatHistory(request);

            responseObserver.onNext(list);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void sendMessage(ChatMessage.ChatRequest request, StreamObserver<StatusOuterClass.Status> responseObserver) {

        logger.info("Received ChatInfo request on chat server:\n" + request.toString());

        if (chatMessagesManager.isLogged(request.getChatInfo().getSenderId())) {

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
            logger.warning("CLIENT_NOT_LOGGED");
            responseObserver.onNext(StatusOuterClass.Status.newBuilder()
                    .setEnum(StatusOuterClass.Status.Enum.CLIENT_NOT_LOGGED)
                    .build());
        }

        responseObserver.onCompleted();

    }

}