package net.saku.journalApp.controller;

import net.saku.journalApp.entity.User;
import net.saku.journalApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping("/all-users")
    public ResponseEntity<List<User>> getAllUsers(){
        List<User> all=userService.getAll();
        if (all!=null && !all.isEmpty()){
            return new ResponseEntity<>(all, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/create-admin-users")
    public void createuser(@RequestBody User user){
        userService.saveAdmin(user);
    }
}
