package net.saku.journalApp.controller;

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
public class JournalEntryControllerV2 {

    @Autowired
    private JournalEntryService journalEntryService;

    @Autowired
    private UserService userService;

    @GetMapping                  // localhost:8080/journal GET
    public ResponseEntity<?> getAllJournalEntriesOfUser() {
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        String username=authentication.getName();
        User user=userService.findByUsername(username);
        List<JournalEntry> all=user.getJournalEntries();
        if(all!=null && !all.isEmpty()){
            return new ResponseEntity<>(all, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping                // localhost:8080/journal POST
    public ResponseEntity<JournalEntry> createEntry(@RequestBody JournalEntry myentry) {
        try{
            Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
            String username=authentication.getName();
            journalEntryService.saveEntry(myentry,username);
            return new ResponseEntity<>(myentry, HttpStatus.CREATED);
        }
        catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/id/{myId}")      // localhost:8080/journal/id/ GET
    public ResponseEntity<JournalEntry> getId(@PathVariable ObjectId myId) {
        // Without ResponseEntity we have to return journalEntryService.findbyId(myId)..orElse(null);
        // With that we have to use method as class Name itself
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        String username=authentication.getName();
        User user=userService.findByUsername(username);
        List<JournalEntry> collect=user.getJournalEntries().stream().filter(x->x.getId().equals(myId)).collect(Collectors.toList());
        if (!collect.isEmpty()){
            Optional<JournalEntry> journalEntry=journalEntryService.findbyId(myId);
            if(journalEntry.isPresent()){
                return new ResponseEntity<>(journalEntry.get(), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/id/{myId}")    // localhost:8080/journal DELETE
    public ResponseEntity<?> deleteUserEntryById(@PathVariable ObjectId myId) {
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        String username=authentication.getName();
        boolean removed=journalEntryService.deletebyId(myId,username);
        if (removed)
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/id/{id}")        // localhost:8080/journal PUT
    public ResponseEntity<?> updateje(@PathVariable ObjectId id, @RequestBody JournalEntry newentry) {
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        String username=authentication.getName();
        User user=userService.findByUsername(username);
        List<JournalEntry> collect=user.getJournalEntries().stream().filter(x->x.getId().equals(id)).collect(Collectors.toList());
        if (!collect.isEmpty()){
            Optional<JournalEntry> journalEntry=journalEntryService.findbyId(id);
            if(journalEntry.isPresent()){
                JournalEntry old=journalEntry.get();
                old.setTitle(newentry.getTitle()!=null && !newentry.getTitle().isEmpty() ? newentry.getTitle(): old.getTitle());
                old.setContent(newentry.getContent()!=null && !newentry.getContent().isEmpty() ? newentry.getContent(): old.getContent());
                journalEntryService.saveEntry(old);
                return new ResponseEntity<>(old, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}