package com.knu.service.web;

import com.knu.service.web.manager.ChatManager;
import com.knu.service.web.manager.LoginManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import service.chat.ChatInfoOuterClass;
import service.chat.QuestionOuterClass;
import service.login.ClientInfoOuterClass;

import javax.annotation.PostConstruct;

@Controller
public class EventController {

    private ClientInfoOuterClass.ClientInfo loginClientInfo = null;
    private service.chat.ClientInfoOuterClass.ClientInfo chatClientInfo = null;
    private LoginManager loginManager;
    private ChatManager chatManager;

    @PostConstruct
    private void init() {

        chatManager = new ChatManager("localhost", "5566");
        loginManager = new LoginManager("localhost", "5577");

        loginClientInfo = ClientInfoOuterClass.ClientInfo.newBuilder()
                .setUsername("STDombr")
                .setPassword("0000")
                .build();

        loginManager.signIn(loginClientInfo);

        if (loginManager.isLogged()) {

            chatClientInfo = service.chat.ClientInfoOuterClass.ClientInfo.newBuilder()
                    .setClientId(loginManager.getClientId())
                    .build();

            chatManager.login(chatClientInfo);

        }

    }

    @RequestMapping("/chats")
    public String showChatsPage(Model model) {

        if (loginManager.isLogged()) {

            if (chatManager.isLogged()) {

                ChatInfoOuterClass.ChatInfoList list = chatManager.getAllChats(chatClientInfo);

                model.addAttribute("UserName", loginClientInfo.getUsername());
                model.addAttribute("ChatInfoList", list);

                return "chats";

            } else {

                model.addAttribute("UserName", loginClientInfo.getUsername());
                model.addAttribute("Status", "Not logged to Chat Service!");

                return "chats";

            }

        } else {

            return "login";

        }

    }

    @RequestMapping("/questions")
    public String showQuestionsPage(Model model) {

        if (loginManager.isLogged()) {

            if (chatManager.isLogged()) {

                QuestionOuterClass.Question question = chatManager.getQuestion();

                model.addAttribute("UserName", loginClientInfo.getUsername());
                model.addAttribute("Question", question);

                return "questions";

            } else {

                model.addAttribute("UserName", loginClientInfo.getUsername());
                model.addAttribute("Status", "Not logged to Chat Service!");

                return "questions";

            }

        } else {

            return "login";

        }

    }
}
