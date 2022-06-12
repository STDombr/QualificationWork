package com.knu.service.login.service;

import com.knu.service.login.manager.DBManager;
import com.knu.service.login.manager.PropertiesManager;
import io.grpc.stub.StreamObserver;
import service.LoginServiceGrpc;

import java.io.IOException;
import java.util.logging.Logger;

public class LoginServiceImpl extends LoginServiceGrpc.LoginServiceImplBase {

    private static final Logger logger = Logger.getLogger(LoginServiceImpl.class.getName());
    private final DBManager dbManager;

    public LoginServiceImpl() throws IOException {

        PropertiesManager propertiesManager = new PropertiesManager("application.properties");

        dbManager = new DBManager(propertiesManager.getProperty("db.postgres.url"), propertiesManager.getProperty("db.postgres.user"), propertiesManager.getProperty("db.postgres.password"));

    }

    @Override
    public void signIn(service.login.ClientInfoOuterClass.ClientInfo request, StreamObserver<service.login.StatusOuterClass.Status> responseObserver) {

        service.login.StatusOuterClass.Status status = dbManager.checkClient(request);

        responseObserver.onNext(status);

        responseObserver.onCompleted();
    }

    @Override
    public void signUp(service.login.ClientInfoOuterClass.ClientInfo request, StreamObserver<service.login.StatusOuterClass.Status> responseObserver) {

        String clientID = dbManager.addClient(request);

        if (clientID != null) {
            service.login.StatusOuterClass.Status status = service.login.StatusOuterClass.Status.newBuilder()
                    .setClientId(clientID)
                    .setEnum(service.login.StatusOuterClass.Status.Enum.SUCCESS)
                    .build();

            responseObserver.onNext(status);
        } else {
            service.login.StatusOuterClass.Status status = service.login.StatusOuterClass.Status.newBuilder()
                    .setEnum(service.login.StatusOuterClass.Status.Enum.ERROR)
                    .build();

            responseObserver.onNext(status);
        }

        responseObserver.onCompleted();
    }

    @Override
    public void changeInfo(service.login.ClientInfoOuterClass.ClientInfo request, StreamObserver<service.login.StatusOuterClass.Status> responseObserver) {
        super.changeInfo(request, responseObserver);
    }

}