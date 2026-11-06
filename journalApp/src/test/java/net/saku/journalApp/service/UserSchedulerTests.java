
package net.saku.journalApp.service;

import net.saku.journalApp.enums.Sentiment;
import net.saku.journalApp.model.SentimentData;
import net.saku.journalApp.scheduler.UserScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootTest
class UserSchedulerTests {

    @Autowired
    private UserScheduler userScheduler;

    @MockBean
    private EmailService emailService;

    @MockBean
    private KafkaTemplate<String, SentimentData> kafkaTemplate;

    @Test
    void testFetchUserAndSendMail() {
        userScheduler.fetchUserAndSendMail();
    }
}