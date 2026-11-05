package net.saku.journalApp.service;

import lombok.extern.slf4j.Slf4j;
import net.saku.journalApp.entity.User;
import net.saku.journalApp.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

// Business Logic
@Service
@Slf4j
@SuppressWarnings("deprecation")
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private static final PasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public boolean saveNewUser(User user) {
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles(Arrays.asList("USER"));
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            log.error("Error occurred for " + user.getUsername() + ": " + e);
            return false;
        }
    }

    // Existing method: creates a new admin account
    public void saveAdmin(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Arrays.asList("USER", "ADMIN"));
        userRepository.save(user);
    }

    // Promote an existing user to ADMIN
    public boolean promoteExistingUser(String username, String email) {

        User user = userRepository.findByUsername(username);

        if (user == null) {
            return false;
        }

        // Verify email
        if (user.getEmail() == null ||
                email == null ||
                !user.getEmail().equalsIgnoreCase(email.trim())) {
            return false;
        }

        // Preserve existing roles
        List<String> roles = user.getRoles() == null
                ? new ArrayList<>()
                : new ArrayList<>(user.getRoles());

        if (!roles.contains("ADMIN")) {
            roles.add("ADMIN");
        }

        user.setRoles(roles);

        // Save without modifying password or journal entries
        userRepository.save(user);

        return true;
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public Optional<User> findbyId(ObjectId id) {
        return userRepository.findById(id);
    }

    public void deletebyId(ObjectId id) {
        userRepository.deleteById(id);
    }

    public User findByUsername(String userName) {
        return userRepository.findByUsername(userName);
    }
}