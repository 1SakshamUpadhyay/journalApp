package net.saku.journalApp.service;

import net.saku.journalApp.entity.User;
import net.saku.journalApp.repository.UserRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserServiceTests {

    @Autowired
    private UserRepository userRepository;

    @Disabled
    @Test
    public void testhai(){
        assertEquals(4,2+2);
        User user=userRepository.findByUsername("ram");
        assertTrue(!user.getJournalEntries().isEmpty());
        assertTrue(5>3);
    }

    @ParameterizedTest
    @ValueSource(strings ={
            "ram",
            "saksham",
            "anjali"
    })
    public void testfindByUsername(String name){
        assertNotNull(userRepository.findByUsername(name),"failed for:"+name);
    }

    @ParameterizedTest
    @CsvSource({
            "1,1,2",
            "2,10,12",
            "3,3,6"
    })
    public void test(int a,int b,int expected){
        assertEquals(expected,a+b);
    }

    @Test
    public void printMongoConnection() {
        System.out.println("Mongo URI: " +
                System.getProperty("spring.data.mongodb.uri"));
    }
}