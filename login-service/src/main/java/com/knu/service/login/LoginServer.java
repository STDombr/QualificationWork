package com.knu.service.login;

import com.knu.service.login.service.LoginServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class LoginServer {
    private static final Logger logger = Logger.getLogger(LoginServer.class.getName());

    Server server;

    public void start(int grpcPort) throws IOException {
        server = ServerBuilder.forPort(grpcPort)
                .addService(new LoginServiceImpl())
                .build();

        server.start();

        logger.info("****  started gRPC Service on port# " + grpcPort);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            try {
                LoginServer.this.stop();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            System.err.println("*** server shut down");
        }));
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }
}
