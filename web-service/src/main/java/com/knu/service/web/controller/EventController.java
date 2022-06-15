package com.knu.service.web.controller;

import com.knu.service.web.manager.ChatManager;
import com.knu.service.web.model.Answer;
import com.knu.service.web.model.ChatId;
import com.knu.service.web.model.ChatInfo;
import com.knu.service.web.tools.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class EventController {

    @Autowired
    private SimpMessagingTemplate template;

    @Value("${chat.service.host}")
    private String chatHost;

    @Value("${chat.service.port}")
    private String chatPort;

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
            manager = new ChatManager(chatHost, chatPort, clientInfo, template);
            manager.setUsername(auth.getName());
            manager.login();
            chatManagerList.put(auth.getName(), manager);
        }

        ChatInfoOuterClass.ChatInfoList chatInfoList = manager.getAllChats();

        List<ChatInfo> list = new ArrayList<>();
        int chatsCount = chatInfoList.getListList().size();
        for (int i = 1; i < chatsCount + 1; i++) {
            ChatInfoOuterClass.ChatInfo chatInfo = chatInfoList.getList(chatsCount - i);

            ChatMessage.ChatResponseList chatResponseList = manager.getChatMessages(chatInfo);
            int responseListSize = chatResponseList.getListList().size();

            List<com.knu.service.web.model.ChatMessage> messageList = new ArrayList<>();

            if (chatInfo.getChatId().equals(chatInfoList.getList(chatsCount - 1).getChatId())) {
                for (ChatMessage.ChatResponse response : chatResponseList.getListList()) {
                    com.knu.service.web.model.ChatMessage message = new com.knu.service.web.model.ChatMessage();

                    message.setChatId(response.getChatInfo().getChatId());
                    message.setSenderId(response.getChatInfo().getSenderId());
                    message.setRecipientId(response.getChatInfo().getRecipientId());
                    message.setBody(response.getBody());
                    message.setTime(Converter.convertTime(response.getTimestampInMillis()));

                    messageList.add(message);
                }
                model.addAttribute("messages", messageList);
            }

            ChatInfo temp = new ChatInfo();

            temp.setChatId(chatInfo.getChatId());
            temp.setQuestionId(chatInfo.getQuestion().getId());
            temp.setQuestion(chatInfo.getQuestion().getBody());
            temp.setRecipientId(chatInfo.getRecipientId());
            if (responseListSize == 0) {
                temp.setLastMessage("");
            } else {
                temp.setLastMessage(chatResponseList.getList(responseListSize - 1).getBody());
            }

            list.add(temp);
        }

        model.addAttribute("UserName", auth.getName());
        model.addAttribute("UserId", manager.getClientInfo().getClientId());
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
            manager = new ChatManager(chatHost, chatPort, clientInfo, template);
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

    @MessageMapping("/private-getAllResponses")
    public void getAllResponses(ChatId chatId) throws InterruptedException {

        ChatManager manager = chatManagerList.get(chatId.getName());

        ChatInfoOuterClass.ChatInfo chatInfo = ChatInfoOuterClass.ChatInfo.newBuilder()
                .setChatId(chatId.getChatId().replace("chat", ""))
                .setSenderId(manager.getClientInfo().getClientId())
                .build();

        ChatMessage.ChatResponseList list = manager.getChatMessages(chatInfo);

        for (ChatMessage.ChatResponse response : list.getListList()) {
            com.knu.service.web.model.ChatMessage message = Converter.convertChatResponse(response);

            template.convertAndSendToUser(chatId.getName(), "/queue/private-chatResponse", message);
            Thread.sleep(10);
        }
    }

    @MessageMapping("/private-sendMessage")
    public void sendMessage(com.knu.service.web.model.ChatMessage chatMessage) {

        ChatManager manager = chatManagerList.get(chatMessage.getUsername());

        ChatInfoOuterClass.ChatInfo chatInfo = ChatInfoOuterClass.ChatInfo.newBuilder()
                .setChatId(chatMessage.getChatId().replace("chat", ""))
                .setSenderId(manager.getClientInfo().getClientId())
                .setRecipientId(chatMessage.getRecipientId())
                .build();

        ChatMessage.ChatRequest chatRequest = ChatMessage.ChatRequest.newBuilder()
                .setChatInfo(chatInfo)
                .setBody(chatMessage.getBody())
                .build();

        manager.sendMessage(chatRequest);

    }
}
