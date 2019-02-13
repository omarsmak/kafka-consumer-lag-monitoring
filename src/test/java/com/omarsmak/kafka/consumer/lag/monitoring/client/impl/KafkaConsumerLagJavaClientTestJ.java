package com.omarsmak.kafka.consumer.lag.monitoring.client.impl;

import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClient;
import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClientFactory;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Properties;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KafkaConsumerLagJavaClientTestJ {

    private KafkaConsumerLagClient kafkaConsumerLagClient;

    @BeforeAll
    public void initialize(){
        final Properties properties = new Properties();
        properties.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "test:9092");

        kafkaConsumerLagClient = KafkaConsumerLagClientFactory.getClient("java", properties);
    }

    @Test
    public void getTopicList(){
        Assertions.assertNotEquals(0, kafkaConsumerLagClient.getTopicsList());
        Assertions.assertNotNull(kafkaConsumerLagClient.getTopicsList());
    }
}
