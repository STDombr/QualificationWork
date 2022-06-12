package com.knu.service.chat.manager;

import com.knu.service.chat.tools.Converter;
import service.chat.ClientInfoOuterClass;
import service.chat.QuestionOuterClass;
import service.chat.ChatInfoOuterClass;
import service.chat.ChatMessage;

import java.io.IOException;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class DBManager {

    private static final String allChatMessages = "SELECT * FROM chat_messages WHERE chat_id=?";
    private static final String getQuestion = "SELECT * FROM chat_questions WHERE id=?";
    private static final String addNewMessage = "INSERT INTO chat_messages  (chat_id, sender_id, recipient_id, body, timestamp) VALUES (?, ?, ?, ?, ?)";
    private static final String addChat = "INSERT INTO chats (chat_id, first_client_id, second_client_id) VALUES (?, ?, ?)";
    private static final String getClientChats = "SELECT * FROM chats WHERE first_client_id=? OR second_client_id=?";
    private static final String allChats = "SELECT * FROM chats";

    private static final Logger logger = Logger.getLogger(DBManager.class.getName());
    private Connection connection = null;

    public DBManager(String url, String user, String password) throws IOException {

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            logger.warning("PostgreSQL JDBC Driver is not found.");
            e.printStackTrace();
            return;
        }

        logger.info("PostgreSQL JDBC Driver successfully connected");

        try {

            connection = DriverManager.getConnection(url, user, password);
            logger.info("Connected to DB");

        } catch (SQLException e) {
            logger.warning("Connection Failed");
            e.printStackTrace();
        }
    }

    public ChatMessage.ChatResponseList getAllChatHistory(ChatInfoOuterClass.ChatInfo chatInfo) {

        ChatMessage.ChatResponseList.Builder listBuilder = ChatMessage.ChatResponseList.newBuilder();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(allChatMessages, Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setString(1, chatInfo.getChatId());

            ResultSet result = preparedStatement.executeQuery();

            while (result.next()) {
                ChatMessage.ChatResponse response = Converter.getUserFromResultSet(result);

                if ((chatInfo.getSenderId().equals(response.getChatInfo().getSenderId()) && chatInfo.getRecipientId().equals(response.getChatInfo().getRecipientId()))
                        || (chatInfo.getSenderId().equals(response.getChatInfo().getRecipientId()) && chatInfo.getRecipientId().equals(response.getChatInfo().getSenderId()))) {
                    listBuilder.addList(response);
                }
            }
        } catch (SQLException throwables) {
            logger.warning(throwables.getMessage());
            return null;
        }
        logger.info("messages successfully found");
        return listBuilder.build();
    }

    public ChatMessage.ChatResponse addNewMessage(ChatMessage.ChatRequest request) {

        long timestamp = System.currentTimeMillis();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(addNewMessage, Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setString(1, request.getChatInfo().getChatId());
            preparedStatement.setString(2, request.getChatInfo().getSenderId());
            preparedStatement.setString(3, request.getChatInfo().getRecipientId());
            preparedStatement.setString(4, request.getBody());
            preparedStatement.setLong(5, timestamp);

            preparedStatement.executeUpdate();

            logger.info("Message successfully added");
        } catch (SQLException throwables) {
            logger.warning("Cound not add message: " + throwables.getMessage());
            return null;
        }
        return ChatMessage.ChatResponse.newBuilder()
                .setChatInfo(request.getChatInfo())
                .setBody(request.getBody())
                .setTimestampInMillis(timestamp)
                .build();
    }

    public ChatInfoOuterClass.ChatInfoList getAllChats(ClientInfoOuterClass.ClientInfo clientInfo) {

        ChatInfoOuterClass.ChatInfoList.Builder list = ChatInfoOuterClass.ChatInfoList.newBuilder();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(getClientChats, Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setString(1, clientInfo.getClientId());
            preparedStatement.setString(2, clientInfo.getClientId());

            ResultSet result = preparedStatement.executeQuery();

            while (result.next()) {
                list.addList(Converter.getChatsFromResultSet(result));
            }
        } catch (SQLException throwables) {
            logger.warning(throwables.getMessage());
            return null;
        }
        logger.info("chats successfully found");
        return list.build();
    }

    public QuestionOuterClass.Question getQuestion(long currentTime) {

        Time time = new Time(currentTime);

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(getQuestion, Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setString(1, String.valueOf(time.getMinutes() + 1));

            ResultSet result = preparedStatement.executeQuery();

            while (result.next()) {
                logger.info("Question successfully found");
                return Converter.getQuestionFromResultSet(result);
            }
        } catch (SQLException throwables) {
            logger.warning(throwables.getMessage());
            return null;
        }
        return null;
    }

    public ChatInfoOuterClass.ChatInfo addNewChat(String senderId, String recipientId) {

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(allChats, Statement.RETURN_GENERATED_KEYS);

            ResultSet result = preparedStatement.executeQuery();

            result.last();

            String stringId = result.getString("chat_id");

            if (stringId != null) {
                int id = Integer.parseInt(stringId) + 1;
                stringId = String.valueOf(id);

                PreparedStatement statement = connection.prepareStatement(addChat, Statement.RETURN_GENERATED_KEYS);

                statement.setString(1, stringId);
                statement.setString(2, senderId);
                statement.setString(3, recipientId);

                preparedStatement.executeUpdate();
                logger.info("Chat created");

                return ChatInfoOuterClass.ChatInfo.newBuilder()
                        .setSenderId(senderId)
                        .setRecipientId(recipientId)
                        .setChatId(stringId)
                        .build();

            }

        } catch (SQLException throwables) {
            logger.warning(throwables.getMessage());
            return null;
        }

        return null;
    }
}
