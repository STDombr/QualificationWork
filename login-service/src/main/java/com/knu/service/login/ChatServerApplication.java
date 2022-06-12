package com.knu.service.login;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class ChatServerApplication {
    public static void main(String[] args) throws IOException, InterruptedException {
        ChatServer server = new ChatServer();
        server.start(5577);
        server.blockUntilShutdown();
    }
}
