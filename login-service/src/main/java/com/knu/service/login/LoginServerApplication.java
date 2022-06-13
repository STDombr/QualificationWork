package com.knu.service.login;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class LoginServerApplication {
    public static void main(String[] args) throws IOException, InterruptedException {
        LoginServer server = new LoginServer();
        server.start(5577);
        server.blockUntilShutdown();
    }
}
