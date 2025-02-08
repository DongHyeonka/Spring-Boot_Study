package com.springboot.alarm.service;

import org.springframework.stereotype.Service;

import com.springboot.alarm.config.AWSConfig;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

@Service
@RequiredArgsConstructor
public class SnsService {
    private final AWSConfig awsConfig;

    public AwsCredentialsProvider getAWSCredentials(String accessKeyId, String secretAccessKey) {
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        return () -> awsBasicCredentials;
    }

    public SnsClient getSnsClient() {
        return SnsClient.builder()
            .credentialsProvider(
                getAWSCredentials(awsConfig.getAwsAccessKey(), awsConfig.getAwsSecretKey())
            )
            .region(Region.of(awsConfig.getAwsRegion()))
            .build();
    }
}
