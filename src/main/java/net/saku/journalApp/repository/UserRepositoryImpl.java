package net.saku.journalApp.repository;

import net.saku.journalApp.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

public class UserRepositoryImpl {

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<User> getUserForSA(){
        Query query=new Query();
        query.addCriteria(Criteria.where("username").is("ram"));
        query.addCriteria(Criteria.where("email").regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"));
        query.addCriteria(Criteria.where("sentimentAnalysis").is(true));
        /* Criteria cr=new Criteria();
           query.addCriteria(criteria.orOperator(
            Criteria.where("email").exists(true),
            Criteria.where("sentimentAnalysis").is(true))
            );
            -->Chaining
            query.addCriteria(Criteria.where("email").ne(null).ne(""));
         */
        List<User> users=mongoTemplate.find(query, User.class);
        return users;
    }
}
