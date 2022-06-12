package com.knu.service.login.manager;

import service.login.StatusOuterClass;

import java.io.IOException;
import java.sql.*;
import java.util.logging.Logger;

public class DBManager {

    private static final String getClient = "SELECT * FROM chat_clients WHERE client_nickname=?";
    private static final String allClients = "SELECT * FROM chat_clients";
    private static final String addClient = "INSERT INTO chat_clients (client_id, client_nickname, client_password) VALUES (?, ?, ?)";

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

    public service.login.StatusOuterClass.Status checkClient(service.login.ClientInfoOuterClass.ClientInfo clientInfo) {

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(getClient, Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setString(1, clientInfo.getUsername());

            ResultSet result = preparedStatement.executeQuery();

            while (result.next()) {
                if (result.getString("client_password").equals(clientInfo.getPassword())) {
                    return service.login.StatusOuterClass.Status.newBuilder()
                            .setClientId(result.getString("client_id"))
                            .setEnum(service.login.StatusOuterClass.Status.Enum.SUCCESS)
                            .build();
                } else {
                    return service.login.StatusOuterClass.Status.newBuilder()
                            .setEnum(StatusOuterClass.Status.Enum.WRONG_PASSWORD)
                            .build();
                }
            }
        } catch (SQLException throwables) {
            logger.warning(throwables.getMessage());
            return service.login.StatusOuterClass.Status.newBuilder()
                    .setEnum(StatusOuterClass.Status.Enum.ERROR)
                    .build();
        }

        return service.login.StatusOuterClass.Status.newBuilder()
                .setEnum(StatusOuterClass.Status.Enum.CLIENT_NOT_REGISTERED)
                .build();
    }

    public String addClient(service.login.ClientInfoOuterClass.ClientInfo clientInfo) {

        StatusOuterClass.Status temp = checkClient(clientInfo);

        if (temp.getEnum() == StatusOuterClass.Status.Enum.CLIENT_NOT_REGISTERED) {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(allClients, Statement.RETURN_GENERATED_KEYS);

                ResultSet result = preparedStatement.executeQuery();

                result.last();

                String stringId = result.getString("client_id");

                if (stringId != null) {
                    int id = Integer.parseInt(stringId) + 1;
                    stringId = String.valueOf(id);

                    PreparedStatement statement = connection.prepareStatement(addClient, Statement.RETURN_GENERATED_KEYS);

                    statement.setString(1, stringId);
                    statement.setString(2, clientInfo.getUsername());
                    statement.setString(3, clientInfo.getPassword());

                    preparedStatement.executeUpdate();
                    logger.info("Client added");

                    return stringId;

                }

            } catch (SQLException throwables) {
                logger.warning(throwables.getMessage());
                return null;
            }
        }

        return null;
    }
}
