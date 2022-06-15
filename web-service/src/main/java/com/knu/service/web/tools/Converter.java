package com.knu.service.web.tools;

import service.chat.ChatMessage;

import java.sql.Time;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Converter {

    public static String convertTime(long timestampInMillis) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a | MMM dd").withZone(ZoneId.systemDefault()).withLocale(Locale.ENGLISH);

        LocalDateTime localDateTime = Instant.ofEpochMilli(timestampInMillis).atZone(ZoneId.systemDefault()).toLocalDateTime();
        return formatter.format(localDateTime);
    }

    public static com.knu.service.web.model.ChatMessage convertChatResponse(ChatMessage.ChatResponse response) {
        com.knu.service.web.model.ChatMessage message = new com.knu.service.web.model.ChatMessage();

        message.setChatId(response.getChatInfo().getChatId());
        message.setSenderId(response.getChatInfo().getSenderId());
        message.setRecipientId(response.getChatInfo().getRecipientId());
        message.setBody(response.getBody());
        message.setTime(Converter.convertTime(response.getTimestampInMillis()));

        return message;
    }
}
