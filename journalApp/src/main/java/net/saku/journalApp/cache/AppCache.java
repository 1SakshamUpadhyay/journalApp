package net.saku.journalApp.cache;

import net.saku.journalApp.entity.ConfigJournalApp;
import net.saku.journalApp.repository.ConfigJournalAppRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AppCache {

    public enum keys{
        WEATHER_API;
    }

    @Autowired
    private ConfigJournalAppRepository configJournalAppRepository;

    public Map<String,String> app_Cache;

    @PostConstruct
    public void init(){
        app_Cache=new HashMap<>();
        List<ConfigJournalApp> all=configJournalAppRepository.findAll();
        for (ConfigJournalApp cja:all){
            app_Cache.put(cja.getKey(),cja.getValue());
        }
    }
}
