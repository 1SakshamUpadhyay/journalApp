package net.saku.journalApp.service;

import lombok.extern.slf4j.Slf4j;
import net.saku.journalApp.entity.User;
import net.saku.journalApp.repository.UserRepository;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

//Business Logic
@Component
@Slf4j
@SuppressWarnings("deprecation")
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // private static final Logger logger= LoggerFactory.getLogger(UserService.class);

    private static final PasswordEncoder passwordEncoder=new BCryptPasswordEncoder();

    public boolean saveNewUser(User user){
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles(Arrays.asList("USER"));
            userRepository.save(user);
            return true;
        }
        catch (Exception e){
            log.error("hahaha");
            log.info("hahaha");
            log.warn("hahaha");
            log.debug("hahaha");
            log.trace("hahaha");
            return false;
        }
    }

    public void saveAdmin(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Arrays.asList("USER","ADMIN"));
        userRepository.save(user);
    }

    public void saveUser(User user){
        userRepository.save(user);
    }

    public List<User> getAll(){
        return userRepository.findAll();
    }

    public Optional<User> findbyId(ObjectId id){
        return userRepository.findById(id);
    }

    public void deletebyId(ObjectId id){
        userRepository.deleteById(id);
    }

    public User findByUsername(String userName){
        return userRepository.findByUsername(userName);
    }
}

// Controller will call --> Service will call --> Repository