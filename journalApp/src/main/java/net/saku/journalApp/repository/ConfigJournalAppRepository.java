package net.saku.journalApp.repository;

import net.saku.journalApp.entity.ConfigJournalApp;
import net.saku.journalApp.entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ConfigJournalAppRepository extends MongoRepository<ConfigJournalApp, ObjectId> {
}
