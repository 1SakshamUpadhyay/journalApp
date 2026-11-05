package net.saku.journalApp.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import net.saku.journalApp.cache.AppCache;
import net.saku.journalApp.entity.User;
import net.saku.journalApp.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin APIs")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private AppCache appCache;

    // Get all registered users
    @GetMapping("/all-users")
    public ResponseEntity<List<User>> getAllUsers() {

        List<User> all = userService.getAll();

        if (all != null && !all.isEmpty()) {
            return new ResponseEntity<>(all, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Promote an existing user to ADMIN
    @PostMapping("/create-admin-users")
    public ResponseEntity<?> promoteUser(
            @RequestBody Map<String, String> request) {

        String username = request.get("username");
        String email = request.get("email");

        if (username == null || username.trim().isEmpty()
                || email == null || email.trim().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body("Username and email are required.");
        }

        boolean promoted = userService.promoteExistingUser(
                username.trim(),
                email.trim()
        );

        if (!promoted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found or email does not match.");
        }

        return ResponseEntity.ok(
                "User successfully promoted to ADMIN."
        );
    }

    // Clear application cache
    @GetMapping("/clear-app-cache")
    public void clearappCache() {
        appCache.init();
    }
}