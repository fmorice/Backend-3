package com.bancoxyz.banco;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TransferenciaProducer {

    private final KafkaTemplate<String, TransferenciaEvent> kafkaTemplate;

    @Value("${app.kafka.topic.transferencias}")
    private String topicName;

    public TransferenciaProducer(KafkaTemplate<String, TransferenciaEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendTransferencia(TransferenciaEvent evento) {
        kafkaTemplate.send(topicName, evento.getEventId(), evento);
    }
}
