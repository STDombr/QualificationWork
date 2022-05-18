package com.knu.service.chat.manager;

import com.google.common.collect.Sets;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.tuple.MutablePair;
import service.ClientInfoOuterClass;
import service.chat.ChatInfoOuterClass;
import service.chat.ChatMessage;

import java.util.Set;
import java.util.logging.Logger;

public class ChatMessagesManager {

    private static final Logger logger = Logger.getLogger(NewPairsNotification.class.getName());
    private Set<MutablePair<ChatInfoOuterClass.ChatInfo, StreamObserver<ChatMessage.ChatResponse>>> clients = Sets.newConcurrentHashSet();

    public void addNewClient(ChatInfoOuterClass.ChatInfo chatInfo, StreamObserver<ChatMessage.ChatResponse> streamObserver) {
        clients.add(new MutablePair<>(chatInfo, streamObserver));
    }

    public void removeChat(ChatInfoOuterClass.ChatInfo chatInfo) {

        for (MutablePair<ChatInfoOuterClass.ChatInfo, StreamObserver<ChatMessage.ChatResponse>> pair : clients) {

            ChatInfoOuterClass.ChatInfo temp = pair.getKey();

            if (temp.getChatId().equals(chatInfo.getChatId())) {
                clients.remove(pair);
            }
        }
    }

    public void removeChat(ClientInfoOuterClass.ClientInfo clientInfo) {

        for (MutablePair<ChatInfoOuterClass.ChatInfo, StreamObserver<ChatMessage.ChatResponse>> pair : clients) {

            ChatInfoOuterClass.ChatInfo temp = pair.getKey();

            if (temp.getSenderId().equals(clientInfo.getClientId())) {
                clients.remove(pair);
            }
        }
    }

    public void boardCast(ChatMessage.ChatResponse response) {

        for (MutablePair<ChatInfoOuterClass.ChatInfo, StreamObserver<ChatMessage.ChatResponse>> pair : clients) {

            ChatInfoOuterClass.ChatInfo chatInfo = pair.getKey();

            if (response.getChatInfo().getChatId().equals(chatInfo.getChatId())) {
                try {
                    pair.getValue().onNext(response);
                } catch (Exception e) {
                    logger.warning("Chat: " + pair.getKey() + " - isnt available");
                    removeChat(pair.getKey());
                }

            }
        }
    }

    public boolean isLogged(ChatInfoOuterClass.ChatInfo chatInfo) {

        for (MutablePair<ChatInfoOuterClass.ChatInfo, StreamObserver<ChatMessage.ChatResponse>> pair : clients) {

            ChatInfoOuterClass.ChatInfo temp = pair.getKey();
            StreamObserver<ChatMessage.ChatResponse> observer = pair.getValue();

            if (temp.getChatId().equals(chatInfo.getChatId()) && temp.getSenderId().equals(chatInfo.getSenderId())) {
                try {
                    observer.onNext(null);
                    return true;
                } catch (Exception e) {
                    removeChat(chatInfo);
                    return false;
                }
            }
        }

        return false;
    }

    public boolean isLogged(ClientInfoOuterClass.ClientInfo clientInfo) {

        for (MutablePair<ChatInfoOuterClass.ChatInfo, StreamObserver<ChatMessage.ChatResponse>> pair : clients) {

            ChatInfoOuterClass.ChatInfo temp = pair.getKey();
            StreamObserver<ChatMessage.ChatResponse> observer = pair.getValue();

            if (temp.getSenderId().equals(clientInfo.getClientId())) {
                try {
                    observer.onNext(null);
                    return true;
                } catch (Exception e) {
                    removeChat(clientInfo);
                    return false;
                }
            }
        }

        return false;
    }
}
