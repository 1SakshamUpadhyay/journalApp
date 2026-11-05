
package net.saku.journalApp.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "oauth_login_tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuthLoginTicket {

    @Id
    private String id;

    private String username;

    @Indexed(expireAfterSeconds = 0)
    private Instant expiresAt;
}