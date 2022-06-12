package com.knu.service.chat.manager;

import com.google.common.collect.Sets;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.tuple.MutablePair;
import service.chat.ClientInfoOuterClass;
import service.chat.ChatInfoOuterClass;
import service.chat.ChatMessage;
import service.chat.UnsolicitedMessageOuterClass;

import java.util.Set;
import java.util.logging.Logger;

public class ChatMessagesManager {

    private static final Logger logger = Logger.getLogger(ChatMessagesManager.class.getName());
    private Set<MutablePair<ClientInfoOuterClass.ClientInfo, StreamObserver<UnsolicitedMessageOuterClass.UnsolicitedMessage>>> clients = Sets.newConcurrentHashSet();

    public void addNewClient(ClientInfoOuterClass.ClientInfo chatInfo, StreamObserver<UnsolicitedMessageOuterClass.UnsolicitedMessage> streamObserver) {
        clients.add(new MutablePair<>(chatInfo, streamObserver));
    }

    public void removeClient(ClientInfoOuterClass.ClientInfo clientInfo) {

        for (MutablePair<ClientInfoOuterClass.ClientInfo, StreamObserver<UnsolicitedMessageOuterClass.UnsolicitedMessage>> pair : clients) {

            ClientInfoOuterClass.ClientInfo temp = pair.getKey();

            if (temp.getClientId().equals(clientInfo.getClientId())) {
                clients.remove(pair);
            }
        }
    }

    public void boardCast(ChatMessage.ChatResponse response) {

        UnsolicitedMessageOuterClass.UnsolicitedMessage unsolicitedMessage = UnsolicitedMessageOuterClass.UnsolicitedMessage.newBuilder()
                .setChatResponse(response)
                .build();

        for (MutablePair<ClientInfoOuterClass.ClientInfo, StreamObserver<UnsolicitedMessageOuterClass.UnsolicitedMessage>> pair : clients) {

            ClientInfoOuterClass.ClientInfo clientInfo = pair.getKey();

            if (response.getChatInfo().getSenderId().equals(clientInfo.getClientId()) ||
                    response.getChatInfo().getRecipientId().equals(clientInfo.getClientId())) {
                try {
                    pair.getValue().onNext(unsolicitedMessage);
                } catch (Exception e) {
                    logger.warning("Client: " + pair.getKey() + " - isnt available");
                    removeClient(pair.getKey());
                }

            }
        }
    }

    public boolean isLogged(ClientInfoOuterClass.ClientInfo clientInfo) {

        for (MutablePair<ClientInfoOuterClass.ClientInfo, StreamObserver<UnsolicitedMessageOuterClass.UnsolicitedMessage>> pair : clients) {

            ClientInfoOuterClass.ClientInfo temp = pair.getKey();
            StreamObserver<UnsolicitedMessageOuterClass.UnsolicitedMessage> observer = pair.getValue();

            if (temp.getClientId().equals(clientInfo.getClientId())) {
                try {
                    observer.onNext(null);
                    return true;
                } catch (Exception e) {
                    removeClient(clientInfo);
                    return false;
                }
            }
        }

        return false;
    }

    public boolean isLogged(String clientID) {

        for (MutablePair<ClientInfoOuterClass.ClientInfo, StreamObserver<UnsolicitedMessageOuterClass.UnsolicitedMessage>> pair : clients) {

            ClientInfoOuterClass.ClientInfo temp = pair.getKey();
            StreamObserver<UnsolicitedMessageOuterClass.UnsolicitedMessage> observer = pair.getValue();

            if (temp.getClientId().equals(clientID)) {
                try {
                    observer.onNext(null);
                    return true;
                } catch (Exception e) {
                    removeClient(temp);
                    return false;
                }
            }
        }

        return false;
    }
}
