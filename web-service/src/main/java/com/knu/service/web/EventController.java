package com.knu.service.web;

import com.knu.service.web.manager.ChatManager;
import com.knu.service.web.model.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import service.chat.ChatInfoOuterClass;
import service.chat.ClientInfoOuterClass;
import service.chat.QuestionOuterClass;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class EventController {

    @Autowired
    private SimpMessagingTemplate template;

    private Map<String, ChatManager> chatManagerList;

    @PostConstruct
    private void init() {

        chatManagerList = new HashMap<>();

    }

    @RequestMapping("/chats")
    public String showChatsPage(Model model) {


        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Set<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toSet());

        ClientInfoOuterClass.ClientInfo clientInfo = ClientInfoOuterClass.ClientInfo.newBuilder()
                .setClientId(roles.iterator().next())
                .build();


        ChatManager manager = null;

        if (chatManagerList.containsKey(auth.getName())) {
            manager = chatManagerList.get(auth.getName());
        } else {
            manager = new ChatManager("localhost", "5566", clientInfo, template);
            manager.login();
            chatManagerList.put(auth.getName(), manager);
        }

        ChatInfoOuterClass.ChatInfoList list = manager.getAllChats();

        model.addAttribute("UserName", auth.getName());
        model.addAttribute("ChatInfoList", list);

        return "chats";

    }

    @RequestMapping("/questions")
    public String showQuestionsPage(Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Set<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toSet());

        ClientInfoOuterClass.ClientInfo clientInfo = ClientInfoOuterClass.ClientInfo.newBuilder()
                .setClientId(roles.iterator().next())
                .build();

        ChatManager manager = null;

        if (chatManagerList.containsKey(auth.getName())) {
            manager = chatManagerList.get(auth.getName());
        } else {
            manager = new ChatManager("localhost", "5566", clientInfo, template);
            manager.login();
            chatManagerList.put(auth.getName(), manager);
        }

        QuestionOuterClass.Question question = manager.getQuestion();

        model.addAttribute("UserName", auth.getName());
        model.addAttribute("Question", question);
        System.out.println(question.getTimestampInMillis());

        return "questions";

    }

    @MessageMapping("/answer")
    public void newAnswer(Answer answer) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        ChatManager manager = chatManagerList.get(auth.getName());

        if (answer.getOption().equals("No")) {

            QuestionOuterClass.Answer protoAnswer = QuestionOuterClass.Answer.newBuilder()
                    .setClientInfo(manager.getClientInfo())
                    .setTimestampInMillis(System.currentTimeMillis())
                    .setQuestionId(answer.getId())
                    .setOption(QuestionOuterClass.Option.newBuilder()
                            .setEnum(QuestionOuterClass.Option.Enum.ENUM_NO)
                            .build())
                    .build();

            manager.sendAnswer(protoAnswer);

        } else if (answer.getOption().equals("Yes")) {

            QuestionOuterClass.Answer protoAnswer = QuestionOuterClass.Answer.newBuilder()
                    .setClientInfo(manager.getClientInfo())
                    .setTimestampInMillis(System.currentTimeMillis())
                    .setQuestionId(answer.getId())
                    .setOption(QuestionOuterClass.Option.newBuilder()
                            .setEnum(QuestionOuterClass.Option.Enum.ENUM_YES)
                            .build())
                    .build();

            manager.sendAnswer(protoAnswer);

        } else {
            System.out.println("Wrong option!");
        }

    }
}
