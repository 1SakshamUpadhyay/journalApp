
package net.saku.journalApp.service;

import net.saku.journalApp.entity.User;
import net.saku.journalApp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserServiceTests {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindByUsername() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setPassword("testpassword");

        userRepository.save(user);

        User foundUser = userRepository.findByUsername("testuser");

        assertNotNull(foundUser);
        assertEquals("testuser", foundUser.getUsername());

        userRepository.delete(user);
    }

    @Test
    public void testAddition() {
        assertEquals(4, 2 + 2);
    }
}