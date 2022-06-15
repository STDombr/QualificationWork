package com.knu.service.web.manager;

import com.knu.service.web.model.Question;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import service.ChatServiceGrpc;
import service.chat.*;

import java.security.Principal;

public class ChatManager {

    private ManagedChannel channel;
    private final ChatServiceGrpc.ChatServiceBlockingStub blockingStub;
    private final ChatServiceGrpc.ChatServiceStub stub;

    private boolean isLogged = false;
    private ClientInfoOuterClass.ClientInfo clientInfo;
    private String username;
    private Principal principal;

    private SimpMessagingTemplate template;

    public ChatManager(String host, String port, ClientInfoOuterClass.ClientInfo clientInfo, SimpMessagingTemplate template) {

        this.template = template;

        channel = ManagedChannelBuilder.forTarget(host + ":" + port)
                .usePlaintext()
                .build();

        blockingStub = ChatServiceGrpc.newBlockingStub(channel);
        stub = ChatServiceGrpc.newStub(channel);

        this.clientInfo = clientInfo;

    }

    public void login() {

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

                        System.out.println("Received new chat response");

                        template.convertAndSendToUser(username, "/queue/private-chatResponse", value.getChatResponse());

                        break;

                    case CHAT_INFO:

                        System.out.println("Received new chat info");

                        Question question = new Question();
                        question.setQuestion(value.getChatInfo().getQuestion().getBody());

                        template.convertAndSendToUser(username, "/queue/private-chatInfo", question);

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

    public void logout() {

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

    public ChatInfoOuterClass.ChatInfoList getAllChats() {
        return blockingStub.getAllChats(clientInfo);
    }

    public ChatMessage.ChatResponseList getChatMessages(ChatInfoOuterClass.ChatInfo chatInfo) {
        return blockingStub.getChatMessages(chatInfo);
    }

    public void sendMessage(ChatMessage.ChatRequest chatRequest) {

        StatusOuterClass.Status status = blockingStub.sendMessage(chatRequest);

    }

    public boolean isLogged() {
        return isLogged;
    }

    public ClientInfoOuterClass.ClientInfo getClientInfo() {
        return clientInfo;
    }

    public void setClientInfo(ClientInfoOuterClass.ClientInfo clientInfo) {
        this.clientInfo = clientInfo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
