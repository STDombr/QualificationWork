package com.knu.service.web;

import com.knu.service.web.manager.ChatManager;
import com.knu.service.web.model.Answer;
import com.knu.service.web.model.ChatInfo;
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
import service.chat.ChatMessage;
import service.chat.ClientInfoOuterClass;
import service.chat.QuestionOuterClass;

import javax.annotation.PostConstruct;
import java.security.Principal;
import java.sql.Array;
import java.util.*;
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
            manager.setUsername(auth.getName());
            manager.login();
            chatManagerList.put(auth.getName(), manager);
        }

        ChatInfoOuterClass.ChatInfoList chatInfoList = manager.getAllChats();

        List<ChatInfo> list = new ArrayList<>();
        for (ChatInfoOuterClass.ChatInfo chatInfo : chatInfoList.getListList()) {

            ChatMessage.ChatResponseList chatResponseList = manager.getChatMessages(chatInfo);
            int responseListSize = chatResponseList.getListList().size();

            ChatInfo temp = new ChatInfo();

            temp.setChatId(chatInfo.getChatId());
            temp.setQuestionId(chatInfo.getQuestion().getId());
            temp.setQuestion(chatInfo.getQuestion().getBody());
            if (responseListSize == 0) {
                temp.setLastMessage("");
            } else {
                temp.setLastMessage(chatResponseList.getList(responseListSize - 1).getBody());
            }

            list.add(temp);
        }

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
            manager.setUsername(auth.getName());
            manager.login();
            chatManagerList.put(auth.getName(), manager);
        }

        QuestionOuterClass.Question question = manager.getQuestion();

        model.addAttribute("UserName", auth.getName());
        model.addAttribute("Question", question);
        System.out.println(question.getTimestampInMillis());

        return "questions";

    }

    @MessageMapping("/private-answer")
    public void newAnswer(Answer answer) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        ChatManager manager = chatManagerList.get(answer.getName());

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
