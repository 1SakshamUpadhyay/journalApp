package net.saku.journalApp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.saku.journalApp.entity.JournalEntry;
import net.saku.journalApp.entity.User;
import net.saku.journalApp.service.JournalEntryService;
import net.saku.journalApp.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/journal")
@Tag(name = "Journal APIs")
public class JournalEntryControllerV2 {

    @Autowired
    private JournalEntryService journalEntryService;

    @Autowired
    private UserService userService;

    // GET all journal entries of logged-in user
    @GetMapping
    @Operation(summary = "Get all journal entries of a user")
    public ResponseEntity<?> getAllJournalEntriesOfUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        User user = userService.findByUsername(username);

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<JournalEntry> all = user.getJournalEntries();

        if (all != null && !all.isEmpty()) {
            return new ResponseEntity<>(all, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // CREATE journal entry
    @PostMapping
    public ResponseEntity<JournalEntry> createEntry(
            @RequestBody JournalEntry myentry) {

        try {
            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            String username = authentication.getName();

            journalEntryService.saveEntry(myentry, username);

            return new ResponseEntity<>(myentry, HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // GET journal entry by ID
    @GetMapping("/id/{myId}")
    public ResponseEntity<JournalEntry> getId(
            @PathVariable String myId) {

        if (myId == null || !ObjectId.isValid(myId)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        ObjectId objectId = new ObjectId(myId);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        User user = userService.findByUsername(username);

        if (user == null || user.getJournalEntries() == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<JournalEntry> collect = user.getJournalEntries()
                .stream()
                .filter(x -> x.getId() != null &&
                        x.getId().equals(objectId))
                .collect(Collectors.toList());

        if (!collect.isEmpty()) {

            Optional<JournalEntry> journalEntry =
                    journalEntryService.findbyId(objectId);

            if (journalEntry.isPresent()) {
                return new ResponseEntity<>(
                        journalEntry.get(), HttpStatus.OK);
            }
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // DELETE journal entry
    @DeleteMapping("/id/{myId}")
    public ResponseEntity<?> deleteUserEntryById(
            @PathVariable String myId) {

        if (myId == null || !ObjectId.isValid(myId)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        ObjectId objectId = new ObjectId(myId);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        boolean removed =
                journalEntryService.deletebyId(objectId, username);

        if (removed) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // UPDATE journal entry
    @PutMapping("/id/{id}")
    public ResponseEntity<?> updateje(
            @PathVariable String id,
            @RequestBody JournalEntry newentry) {

        // Validate MongoDB ID
        if (id == null || !ObjectId.isValid(id)) {
            return ResponseEntity.badRequest()
                    .body("Invalid entry ID");
        }

        ObjectId objectId = new ObjectId(id);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        User user = userService.findByUsername(username);

        if (user == null || user.getJournalEntries() == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<JournalEntry> collect = user.getJournalEntries()
                .stream()
                .filter(x -> x.getId() != null &&
                        x.getId().equals(objectId))
                .collect(Collectors.toList());

        if (!collect.isEmpty()) {

            Optional<JournalEntry> journalEntry =
                    journalEntryService.findbyId(objectId);

            if (journalEntry.isPresent()) {

                JournalEntry old = journalEntry.get();

                // Update title
                if (newentry.getTitle() != null &&
                        !newentry.getTitle().trim().isEmpty()) {

                    old.setTitle(newentry.getTitle());
                }

                // Update content
                if (newentry.getContent() != null) {
                    old.setContent(newentry.getContent());
                }

                // Update sentiment
                if (newentry.getSentiment() != null) {
                    old.setSentiment(newentry.getSentiment());
                }

                journalEntryService.saveEntry(old);

                return new ResponseEntity<>(old, HttpStatus.OK);
            }
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}