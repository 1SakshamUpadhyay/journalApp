package net.saku.journalApp.service;

import lombok.extern.slf4j.Slf4j;
import net.saku.journalApp.entity.JournalEntry;
import net.saku.journalApp.entity.User;
import net.saku.journalApp.repository.JournalEntryRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

//Business Logic
@Component
@Slf4j
public class JournalEntryService {

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public void saveEntry(JournalEntry journalEntry, String userName){
        try{
            User user=userService.findByUsername(userName);
            journalEntry.setDate(LocalDateTime.now());
            JournalEntry saved=journalEntryRepository.save(journalEntry);
            user.getJournalEntries().add(saved);
            userService.saveUser(user);
        }
        catch (Exception e){
            System.out.println(e);
            throw new RuntimeException("An error occurred by saving the entry.", e);
        }
    }

    public void saveEntry(JournalEntry journalEntry){
        journalEntryRepository.save(journalEntry);
    }

    public List<JournalEntry> getAll(){
        return journalEntryRepository.findAll();
    }

    public Optional<JournalEntry> findbyId(ObjectId id){
        return journalEntryRepository.findById(id);
    }

    @Transactional
    public boolean deletebyId(ObjectId id,String username){
        boolean removed=false;
        try {
            User user=userService.findByUsername(username);
            removed=user.getJournalEntries().removeIf(x->x.getId().equals(id));
            if (removed){
                userService.saveUser(user);
                journalEntryRepository.deleteById(id);
            }
        }
        catch (Exception e){
            log.error("Error",e);
            throw new RuntimeException("An error occurred while deleting the entry:"+e);
        }
        return removed;
    }
}

// Controller will call --> Service will call --> Repository