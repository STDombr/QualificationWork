package com.knu.service.chat.manager;

import com.google.common.collect.Sets;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.tuple.MutablePair;
import service.chat.*;

import java.util.*;
import java.util.logging.Logger;

public class NewPairsNotification {

    private static final Logger logger = Logger.getLogger(ChatMessagesManager.class.getName());
    private Set<MutablePair<ClientInfoOuterClass.ClientInfo, StreamObserver<UnsolicitedMessageOuterClass.UnsolicitedMessage>>> clients = Sets.newConcurrentHashSet();

    private Map<String, Set<QuestionOuterClass.Answer>> allAnswers = new HashMap<>();
    private DBManager dbManager;

    private NewPairsNotification() {

    }

    public NewPairsNotification(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    public void addNewClient(ClientInfoOuterClass.ClientInfo clientInfo, StreamObserver<UnsolicitedMessageOuterClass.UnsolicitedMessage> streamObserver) {
        clients.add(new MutablePair<>(clientInfo, streamObserver));
    }

    public void removeClient(ClientInfoOuterClass.ClientInfo clientInfo) {

        for (MutablePair<ClientInfoOuterClass.ClientInfo, StreamObserver<UnsolicitedMessageOuterClass.UnsolicitedMessage>> pair : clients) {

            ClientInfoOuterClass.ClientInfo temp = pair.getKey();

            if (temp.getClientId().equals(clientInfo.getClientId())) {
                clients.remove(pair);
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
                UnsolicitedMessageOuterClass.UnsolicitedMessage unsolicitedMessage = UnsolicitedMessageOuterClass.UnsolicitedMessage.newBuilder()
                        .setChatInfo(chatInfo)
                        .build();

                for (MutablePair<ClientInfoOuterClass.ClientInfo, StreamObserver<UnsolicitedMessageOuterClass.UnsolicitedMessage>> pair : clients) {
                    ClientInfoOuterClass.ClientInfo tempClientInfo = pair.getKey();

                    if (tempClientInfo.equals(clientInfo)) {
                        pair.getValue().onNext(unsolicitedMessage);
                    }

                    if (tempClientInfo.equals(temp.getClientInfo())) {
                        pair.getValue().onNext(unsolicitedMessage);
                        answers.remove(temp);
                    }
                }
                return;
            }
        }

        answers.add(answer);
    }

}
