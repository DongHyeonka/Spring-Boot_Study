package com.springboot.alarm.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.springboot.alarm.config.AWSConfig;
import com.springboot.alarm.service.SlackService;
import com.springboot.alarm.service.SnsService;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsResponse;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sns.model.SubscribeResponse;

@RestController
@RequiredArgsConstructor
public class SnsController {
    private final SnsService snsService;
    private final AWSConfig awsConfig;
    private final SlackService slackService;

    @PostMapping("/create-topic")
    public ResponseEntity<String> createTopic(@RequestParam final String topicName) {
        final CreateTopicRequest createTopicRequest = CreateTopicRequest.builder()
            .name(topicName)
            .build();

        SnsClient snsClient = snsService.getSnsClient();

        final CreateTopicResponse createTopicResponse = snsClient.createTopic(createTopicRequest);

        if(!createTopicResponse.sdkHttpResponse().isSuccessful()) {
            throw getResponseStatusException(createTopicResponse);
        }

        snsClient.close();

        return ResponseEntity.ok().body("Topic created successfully");
    }

    private ResponseStatusException getResponseStatusException(final SnsResponse snsResponse) {
        return new ResponseStatusException(
            HttpStatus.INTERNAL_SERVER_ERROR, snsResponse.sdkHttpResponse().statusText().get()
        );
    }

    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribe(
        @RequestParam final String endpoint, @RequestParam final String topicArn
    ) {
        final SubscribeRequest subscribeRequest = SubscribeRequest.builder()
            .protocol("https")
            .topicArn(topicArn)
            .endpoint(endpoint)
            .build();
        SnsClient snsClient = snsService.getSnsClient();
        final SubscribeResponse subscribeResponse = snsClient.subscribe(subscribeRequest);

        if(!subscribeResponse.sdkHttpResponse().isSuccessful()) {
            throw getResponseStatusException(subscribeResponse);
        }

        snsClient.close();

        return ResponseEntity.ok().body("Subscribed successfully");
    }

    @PostMapping("/publish")
    public ResponseEntity<String> publish(
        @RequestParam final String topicArn, @RequestBody Map<String, Object> message
    ) {
        SnsClient snsClient = snsService.getSnsClient();

        final PublishRequest publishRequest = PublishRequest.builder()
            .topicArn(topicArn)
            .subject("HTTP ENDPOINT TEST MESSAGE")
            .message(message.toString())
            .build();
        
        final PublishResponse publishResponse = snsClient.publish(publishRequest);

        snsClient.close();

        return ResponseEntity.ok().body(publishResponse.messageId());
    }

    // test는 webhook을 사용해서 테스트가 가능

    @GetMapping("/slack/error")
    public void error() {
        slackService.sendMessage("", "error");
    }

    
}
