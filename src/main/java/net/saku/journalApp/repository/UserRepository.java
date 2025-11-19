package net.saku.journalApp.repository;

import net.saku.journalApp.entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, ObjectId> {
    User findByUsername(String username);

    void deleteByUsername(String username);
}
