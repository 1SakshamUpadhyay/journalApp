package net.saku.journalApp.repository;

import net.saku.journalApp.entity.OAuthLoginTicket;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OAuthLoginTicketRepository
        extends MongoRepository<OAuthLoginTicket, String> {
}