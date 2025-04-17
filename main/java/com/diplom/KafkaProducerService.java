package com.diplom;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, Map<String, Object>> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendUserLoginEvent(String username) {
        Map<String, Object> event = new HashMap<>();
        event.put("username", username);
        event.put("type", "login");
        kafkaTemplate.send("user-events-topic", event);
    }

    public void sendFileUploadEvent(String username, String filePath, String filename) {
        Map<String, Object> event = new HashMap<>();
        event.put("username", username);
        event.put("filePath", filePath); // Передаем путь к файлу
        event.put("filename", filename);
        event.put("type", "upload");
        kafkaTemplate.send("user-events-topic", event);
    }
}