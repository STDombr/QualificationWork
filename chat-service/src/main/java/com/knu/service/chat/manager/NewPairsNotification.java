package com.knu.service.chat.manager;

import com.google.common.collect.Sets;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.tuple.MutablePair;
import service.ClientInfoOuterClass;
import service.QuestionOuterClass;
import service.chat.ChatInfoOuterClass;
import service.chat.ChatMessage;

import java.util.*;
import java.util.logging.Logger;

public class NewPairsNotification {

    private static final Logger logger = Logger.getLogger(ChatMessagesManager.class.getName());
    private Set<MutablePair<ClientInfoOuterClass.ClientInfo, StreamObserver<ChatInfoOuterClass.ChatInfo>>> clients = Sets.newConcurrentHashSet();

    private Map<String, Set<QuestionOuterClass.Answer>> allAnswers = new HashMap<>();
    private DBManager dbManager;

    private NewPairsNotification() {

    }

    public NewPairsNotification(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    public void addNewClient(ClientInfoOuterClass.ClientInfo clientInfo, StreamObserver<ChatInfoOuterClass.ChatInfo> streamObserver) {
        clients.add(new MutablePair<>(clientInfo, streamObserver));
    }

    public void removeClient(ClientInfoOuterClass.ClientInfo clientInfo) {

        for (MutablePair<ClientInfoOuterClass.ClientInfo, StreamObserver<ChatInfoOuterClass.ChatInfo>> pair : clients) {

            ClientInfoOuterClass.ClientInfo temp = pair.getKey();

            if (temp.getClientId().equals(clientInfo.getClientId())) {
                clients.remove(pair);
            }
        }
    }

    public boolean isLogged(ClientInfoOuterClass.ClientInfo clientInfo) {

        for (MutablePair<ClientInfoOuterClass.ClientInfo, StreamObserver<ChatInfoOuterClass.ChatInfo>> pair : clients) {

            ClientInfoOuterClass.ClientInfo temp = pair.getKey();
            StreamObserver<ChatInfoOuterClass.ChatInfo> observer = pair.getValue();

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

    public synchronized void addAnswer(QuestionOuterClass.Answer answer) {

        ClientInfoOuterClass.ClientInfo clientInfo = answer.getClientInfo();

        if (!isLogged(clientInfo)) {
            return;
        }

        Set<QuestionOuterClass.Answer> answers = allAnswers.get(answer.getQuestionId());

        if (answers == null) {
            answers = new HashSet<>();
            answers.add(answer);
            allAnswers.put(answer.getQuestionId(), answers);
            return;
        }

        int option = answer.getOption().getEnum().getNumber();

        for (QuestionOuterClass.Answer temp : answers) {
            int tempOption = temp.getOption().getEnum().getNumber();

            if (tempOption != option) {

                ChatInfoOuterClass.ChatInfo chatInfo = dbManager.addNewChat(clientInfo.getClientId(), answer.getClientInfo().getClientId());

                for (MutablePair<ClientInfoOuterClass.ClientInfo, StreamObserver<ChatInfoOuterClass.ChatInfo>> pair : clients) {
                    ClientInfoOuterClass.ClientInfo tempClientInfo = pair.getKey();

                    if (tempClientInfo.equals(clientInfo)) {
                        pair.getValue().onNext(chatInfo);
                    }

                    if (tempClientInfo.equals(temp.getClientInfo())) {
                        pair.getValue().onNext(chatInfo);
                        answers.remove(temp);
                    }
                }
                return;
            }
        }

        answers.add(answer);
    }

}
