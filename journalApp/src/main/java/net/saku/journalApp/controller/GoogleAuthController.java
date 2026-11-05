package net.saku.journalApp.controller;

import lombok.extern.slf4j.Slf4j;
import net.saku.journalApp.entity.OAuthLoginTicket;
import net.saku.journalApp.entity.User;
import net.saku.journalApp.repository.OAuthLoginTicketRepository;
import net.saku.journalApp.repository.UserRepository;
import net.saku.journalApp.utils.JWTUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/auth/google")
@Slf4j
public class GoogleAuthController {

    private static final String REDIRECT_URI =
            "http://localhost:8080/journal/auth/google/callback";

    private static final String FRONTEND_CALLBACK =
            "http://localhost:5173/oauth/callback";

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OAuthLoginTicketRepository ticketRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTUtil jwtUtil;

    // Start Google login
    @GetMapping("/start")
    public ResponseEntity<Void> startGoogleLogin() {

        String state = UUID.randomUUID().toString();

        OAuthLoginTicket stateTicket = new OAuthLoginTicket();
        stateTicket.setId(state);
        stateTicket.setExpiresAt(Instant.now().plusSeconds(300));

        ticketRepository.save(stateTicket);

        String googleUrl = UriComponentsBuilder
                .fromUriString(
                        "https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", REDIRECT_URI)
                .queryParam("response_type", "code")
                .queryParam("scope", "openid email profile")
                .queryParam("state", state)
                .build()
                .encode()
                .toUriString();

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(googleUrl))
                .build();
    }

    // Google callback
    @GetMapping("/callback")
    public ResponseEntity<Void> handleGoogleCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error) {

        try {

            if (error != null || code == null || state == null) {
                return frontendError();
            }

            // Atomically consume and validate OAuth state
            OAuthLoginTicket stateRecord = consumeTicket(state);

            if (stateRecord == null
                    || stateRecord.getExpiresAt() == null
                    || !stateRecord.getExpiresAt().isAfter(Instant.now())) {
                return frontendError();
            }

            // Exchange authorization code
            MultiValueMap<String, String> params =
                    new LinkedMultiValueMap<>();

            params.add("code", code);
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("redirect_uri", REDIRECT_URI);
            params.add("grant_type", "authorization_code");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(
                    MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request =
                    new HttpEntity<>(params, headers);

            ResponseEntity<Map> tokenResponse =
                    restTemplate.postForEntity(
                            "https://oauth2.googleapis.com/token",
                            request,
                            Map.class
                    );

            if (tokenResponse.getBody() == null) {
                return frontendError();
            }

            String idToken =
                    (String) tokenResponse.getBody().get("id_token");

            if (idToken == null || idToken.isEmpty()) {
                return frontendError();
            }

            // Verify Google ID token
            String userInfoUrl = UriComponentsBuilder
                    .fromUriString(
                            "https://oauth2.googleapis.com/tokeninfo")
                    .queryParam("id_token", idToken)
                    .build()
                    .encode()
                    .toUriString();

            ResponseEntity<Map> userInfoResponse =
                    restTemplate.getForEntity(userInfoUrl, Map.class);

            Map<String, Object> userInfo =
                    userInfoResponse.getBody();

            if (!userInfoResponse.getStatusCode().is2xxSuccessful()
                    || userInfo == null) {
                return frontendError();
            }

            String audience = (String) userInfo.get("aud");
            String email = (String) userInfo.get("email");
            String verified =
                    String.valueOf(userInfo.get("email_verified"));

            if (!clientId.equals(audience)
                    || !"true".equalsIgnoreCase(verified)
                    || email == null
                    || email.trim().isEmpty()) {
                return frontendError();
            }

            // Find existing account
            User user = userRepository.findByEmail(email);

            if (user == null) {

                if (userRepository.findByUsername(email) != null) {
                    return frontendError();
                }

                // Create a new Google account
                user = new User();
                user.setUsername(email);
                user.setEmail(email);
                user.setPassword(
                        passwordEncoder.encode(
                                UUID.randomUUID().toString()));
                user.setRoles(Collections.singletonList("USER"));
                user.setJournalEntries(new ArrayList<>());

                user = userRepository.save(user);

            } else if (!email.equalsIgnoreCase(user.getUsername())) {

                // Do not silently link an existing password account
                return frontendError();
            }

            // Create temporary login ticket
            String loginTicket = UUID.randomUUID().toString();

            OAuthLoginTicket ticket = new OAuthLoginTicket();
            ticket.setId(loginTicket);
            ticket.setUsername(user.getUsername());
            ticket.setExpiresAt(Instant.now().plusSeconds(120));

            ticketRepository.save(ticket);

            URI frontendUri = UriComponentsBuilder
                    .fromUriString(FRONTEND_CALLBACK)
                    .queryParam("ticket", loginTicket)
                    .build()
                    .encode()
                    .toUri();

            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(frontendUri)
                    .build();

        } catch (Exception e) {
            log.error("Google login failed", e);
            return frontendError();
        }
    }

    // Exchange single-use ticket for JWT
    @PostMapping("/exchange")
    public ResponseEntity<?> exchangeTicket(
            @RequestBody Map<String, String> body) {

        try {

            String ticket = body.get("ticket");

            if (ticket == null || ticket.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Collections.singletonMap(
                                "error", "Ticket required"));
            }

            OAuthLoginTicket loginTicket = consumeTicket(ticket);

            if (loginTicket == null
                    || loginTicket.getUsername() == null
                    || loginTicket.getExpiresAt() == null
                    || !loginTicket.getExpiresAt().isAfter(Instant.now())) {

                return ResponseEntity.status(
                        HttpStatus.UNAUTHORIZED).build();
            }

            String jwtToken =
                    jwtUtil.generateToken(loginTicket.getUsername());

            return ResponseEntity.ok(
                    Collections.singletonMap("token", jwtToken));

        } catch (Exception e) {
            log.error("Google ticket exchange failed", e);
            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Atomically find and remove a ticket
    private OAuthLoginTicket consumeTicket(String id) {

        Query query = new Query(
                Criteria.where("_id").is(id)
                        .and("expiresAt").gt(Instant.now())
        );

        return mongoTemplate.findAndRemove(
                query,
                OAuthLoginTicket.class
        );
    }

    private ResponseEntity<Void> frontendError() {

        URI errorUri = URI.create(
                FRONTEND_CALLBACK + "?error=google_login_failed");

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(errorUri)
                .build();
    }
}