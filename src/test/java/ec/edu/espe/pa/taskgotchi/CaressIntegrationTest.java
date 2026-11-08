package ec.edu.espe.pa.taskgotchi;

import ec.edu.espe.pa.taskgotchi.repository.PetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.*;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "taskgotchi.security.auth-rate-limit.max-attempts=50"})
@ActiveProfiles("h2")
class CaressIntegrationTest {
    @LocalServerPort int port;
    @Autowired ObjectMapper json;
    @Autowired PetRepository pets;
    final HttpClient client = HttpClient.newHttpClient();
    String token;
    long petId;

    HttpResponse<String> call(String method, String path) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .header("Content-Type", "application/json")
                .method(method, HttpRequest.BodyPublishers.noBody());
        if (token != null) request.header("Authorization", "Bearer " + token);
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    @BeforeEach
    void register() throws Exception {
        String name = "caress" + System.nanoTime();
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/v1/auth/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(Map.of(
                        "username", name, "email", name + "@espe.edu.ec", "password", "Sintetica_2026"))));
        var response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        token = json.readTree(response.body()).get("token").asString();
        petId = json.readTree(call("GET", "/api/v1/pet").body()).get("id").asLong();
        var pet = pets.findById(petId).orElseThrow();
        pet.setMood(50);
        pets.save(pet);
    }

    @Test
    void caressRaisesMoodOnceThenWaitsForCooldown() throws Exception {
        var first = json.readTree(call("POST", "/api/v1/pet/caress").body());
        assertEquals(5, first.get("moodGained").asInt());
        assertEquals(55, first.get("pet").get("mood").asInt());
        assertEquals(20, first.get("cooldownSeconds").asInt());

        var second = call("POST", "/api/v1/pet/caress");
        assertEquals(200, second.statusCode());
        var body = json.readTree(second.body());
        assertEquals(0, body.get("moodGained").asInt());
        assertEquals(55, body.get("pet").get("mood").asInt());
        assertTrue(body.get("cooldownSeconds").asInt() > 0);
        assertEquals(55, pets.findById(petId).orElseThrow().getMood());
    }

    @Test
    void caressWorksAgainAfterCooldownAndCapsAt100() throws Exception {
        var pet = pets.findById(petId).orElseThrow();
        pet.setMood(98);
        pet.setLastCaressAt(LocalDateTime.now().minusSeconds(21));
        pets.save(pet);
        var body = json.readTree(call("POST", "/api/v1/pet/caress").body());
        assertEquals(2, body.get("moodGained").asInt());
        assertEquals(100, body.get("pet").get("mood").asInt());
    }

    @Test
    void caressRequiresAuthentication() throws Exception {
        token = null;
        assertEquals(401, call("POST", "/api/v1/pet/caress").statusCode());
        assertEquals(401, call("POST", "/api/v1/pet/bonk").statusCode());
    }

    @Test
    void bonkLowersMoodOnceThenWaitsForCooldown() throws Exception {
        var first = json.readTree(call("POST", "/api/v1/pet/bonk").body());
        assertEquals(5, first.get("moodLost").asInt());
        assertEquals(45, first.get("pet").get("mood").asInt());

        var second = json.readTree(call("POST", "/api/v1/pet/bonk").body());
        assertEquals(0, second.get("moodLost").asInt());
        assertEquals(45, pets.findById(petId).orElseThrow().getMood());
    }

    @Test
    void bonkNeverGoesBelowZero() throws Exception {
        var pet = pets.findById(petId).orElseThrow();
        pet.setMood(3);
        pets.save(pet);
        var body = json.readTree(call("POST", "/api/v1/pet/bonk").body());
        assertEquals(3, body.get("moodLost").asInt());
        assertEquals(0, body.get("pet").get("mood").asInt());
    }
}
