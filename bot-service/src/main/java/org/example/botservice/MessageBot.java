package org.example.botservice;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class MessageBot {

    private final RestClient messageClient = RestClient.create("http://message-service:8082");
    private final RestClient authClient = RestClient.create("http://auth-service:9000");

    @RabbitListener(queues = "message.queue")
    public void handleMessage(String message) {
        System.out.println("Bot mottog: " + message);

        if (message.startsWith("Bot: ")) {
            System.out.println("Ignorerar eget meddelande");
            return;
        }

        String[] parts = message.split(": ", 2);
        if (parts.length < 2) {
            return;
        }

        String username = parts[0];
        String content = parts[1].toLowerCase().trim();

        String reply = generateReply(content);
        if (reply != null) {
            sendMessage("Bot", reply);
        }
    }

    private String generateReply(String message) {
        if (message.contains("hej") || message.contains("hallå") || message.contains("tjena")) {
            return "Hej! Välkommen till chatten!";
        } else if (message.contains("hjälp") || message.contains("help")) {
            return "Tillgängliga kommandon: hej, tid, väder, tack";
        } else if (message.contains("tid") || message.contains("klockan")) {
            return "Klockan är " + LocalTime.now().plusHours(2).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        } else if (message.contains("väder")) {
            return "Det är molnigt med chans för mikroservicear! ☁️";
        } else if (message.contains("tack")) {
            return "Varsågod! 😊";
        } else if (message.contains("bot") || message.contains("robot")) {
            return "Ja, jag är här! En enkel bot som gillar mikrotjänster!";
        } else if (message.length() < 10) {
            return "Intressant! Berätta mer... 🤔";
        }
        return null;
    }

    private void sendMessage(String username, String content) {
        try {
            String token = getAuthToken();
            if (token == null) {
                System.err.println("Bot kunde inte hämta token!");
                return;
            }

            MessageRequest request = new MessageRequest(content, username);

            String response = messageClient.post()
                    .uri("/api/messages")
                    .headers(h -> h.setBearerAuth(token))
                    .body(request)
                    .retrieve()
                    .body(String.class);

            System.out.println("Bot svarade: " + content);
        } catch (Exception e) {
            System.err.println("Bot kunde inte skicka" + e.getMessage());
        }
    }
    private String getAuthToken() {
        try {
            Map<String, String> loginRequest = Map.of(
                    "username", "demo",
                    "password", "demo"
            );

            Map response = authClient.post()
                    .uri("/api/auth/login")
                    .body(loginRequest)
                    .retrieve()
                    .body(Map.class);

            return (String) response.get("access_token");
        } catch (Exception e) {
            System.err.println("Bot kunde inte logga in: " + e.getMessage());
            return null;
        }
    }

    record MessageRequest(String content, String username) {}
}
