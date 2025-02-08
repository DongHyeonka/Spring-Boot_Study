package com.springboot.alarm.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SlackService {
    @Value(value = "${slack.token}")
    private String slackToken;

    public void sendMessage(String message, String channel) {
        String channelAddress = "";

        if(channel.equals("error"))
            channelAddress = "#모니터링";

        try {
            MethodsClient methodsClient = Slack.getInstance().methods(slackToken);

            ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                    .channel(channelAddress)
                    .text(message)
                    .build();
            
            methodsClient.chatPostMessage(request);
            log.info(channel);
        } catch (SlackApiException | IOException e) {
            log.error(e.getMessage());
        }
    }
}
