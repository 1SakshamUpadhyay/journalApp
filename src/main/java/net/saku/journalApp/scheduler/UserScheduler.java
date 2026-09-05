package net.saku.journalApp.scheduler;

import net.saku.journalApp.cache.AppCache;
import net.saku.journalApp.entity.JournalEntry;
import net.saku.journalApp.entity.User;
import net.saku.journalApp.enums.Sentiment;
import net.saku.journalApp.repository.UserRepositoryImpl;
import net.saku.journalApp.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UserScheduler {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepositoryImpl userRepository;

    @Autowired
    private AppCache appCache;

//    @Scheduled(cron = "0 0 9 * * SUN")
    public void fetchUserAndSendMail() {
        List<User> users = userRepository.getUserForSA();
        for (User user : users) {
            List<JournalEntry> journalEntries = user.getJournalEntries();
            List<Sentiment> sentiments = journalEntries.stream().filter(x -> x.getDate().isAfter(LocalDateTime.now().minus(7, ChronoUnit.DAYS))).map(x -> x.getSentiment()).collect(Collectors.toList());

            // Calculate the most frequent sentiment
            Map<Sentiment, Integer> sentCount = new HashMap<>();
            for (Sentiment sentiment : sentiments) {
                sentCount.put(sentiment, sentCount.getOrDefault(sentiment, 0) + 1);
            }
            Sentiment mostFreqSent = null;
            int maxCount = 0;
            for (Map.Entry<Sentiment, Integer> entry : sentCount.entrySet())
                if (entry.getValue() > maxCount) {
                    maxCount = entry.getValue();
                    mostFreqSent = entry.getKey();
                }
            if (mostFreqSent != null) {
                emailService.sendEmail(user.getEmail(), "Sentiment for last 7 days ", mostFreqSent.toString());
            }
        }
    }

    @Scheduled(cron = "0 0/10 * ? * *")
    public void clearAppCache () {
        appCache.init();
    }
}

