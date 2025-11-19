package net.saku.journalApp.controller;

import net.saku.journalApp.entity.User;
import net.saku.journalApp.repository.UserRepository;
import net.saku.journalApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @PutMapping
    public ResponseEntity<?> updateUser(@RequestBody User user){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        String username=authentication.getName();
        User userInDb=userService.findByUsername(username);
        userInDb.setUsername(user.getUsername());
        userInDb.setPassword(user.getPassword());
        userService.saveNewUser(userInDb);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUserById(){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        userRepository.deleteByUsername(authentication.getName());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}