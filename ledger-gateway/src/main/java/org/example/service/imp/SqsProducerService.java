package org.example.service.imp;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.SmsNotificationTask;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.UUID;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class SqsProducerService {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private static final Logger log = getLogger(SqsProducerService.class);

    @Value("${aws.sqs.queue-url}")
    private String queueUrl;

    public SqsProducerService(SqsClient sqsClient, ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
    }

    public void queueSmsTask(SmsNotificationTask task) {
        try {

            if(task == null) {
                log.error("Task is null");
                return;
            }

            // Convert DTO to JSON String
            String messageJson = objectMapper.writeValueAsString(task);

            SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageJson)
                    .messageGroupId(task.getPhoneNumber())  // per user ordering
                    .messageDeduplicationId(UUID.randomUUID().toString()) // unique
                    .build();

            log.info("SMS queued successfully: {}", messageJson);
            log.info("SMS queued successfully: {}", sendMsgRequest);

            sqsClient.sendMessage(sendMsgRequest);
        } catch (Exception e) {
            // We don't throw exception here because the payment is ALREADY successful.
            // We just log it for manual retry.
            System.err.println("Failed to queue SMS: " + e.getMessage());
        }
    }
}