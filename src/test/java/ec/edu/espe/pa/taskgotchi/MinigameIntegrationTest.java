package ec.edu.espe.pa.taskgotchi;

import ec.edu.espe.pa.taskgotchi.entity.PetEventType;
import ec.edu.espe.pa.taskgotchi.repository.PetEventRepository;
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
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "taskgotchi.security.auth-rate-limit.max-attempts=50"})
@ActiveProfiles("h2")
class MinigameIntegrationTest {
    @LocalServerPort int port;
    @Autowired ObjectMapper json;
    @Autowired PetRepository pets;
    @Autowired PetEventRepository events;
    final HttpClient client = HttpClient.newHttpClient();
    String token;
    long petId;

    HttpResponse<String> call(String method, String path, String body) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .header("Content-Type", "application/json")
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(body));
        if (token != null) request.header("Authorization", "Bearer " + token);
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    @BeforeEach
    void register() throws Exception {
        String name = "minigame" + System.nanoTime();
        var response = call("POST", "/api/v1/auth/register", json.writeValueAsString(Map.of(
                "username", name, "email", name + "@espe.edu.ec", "password", "Sintetica_2026")));
        assertEquals(201, response.statusCode());
        token = json.readTree(response.body()).get("token").asString();
        petId = json.readTree(call("GET", "/api/v1/pet", null).body()).get("id").asLong();
    }

    HttpResponse<String> play(String game, int score) throws Exception {
        return call("POST", "/api/v1/pet/minigame", json.writeValueAsString(Map.of("game", game, "score", score)));
    }

    @Test
    void rewardsStatsAndSinglePlayedEventArePersisted() throws Exception {
        var before = pets.findById(petId).orElseThrow();
        var response = play("CATCH", 85);
        assertEquals(200, response.statusCode());
        var result = json.readTree(response.body());
        assertEquals(13, result.get("xpEarned").asInt());
        assertEquals(2, result.get("coinsEarned").asInt());
        assertEquals(7, result.get("rewardedPlaysLeft").asInt());
        var after = pets.findById(petId).orElseThrow();
        assertEquals(before.getEnergy() - 15, after.getEnergy());
        assertEquals(Math.min(100, before.getMood() + 25), after.getMood());
        assertEquals(before.getHunger() - 10, after.getHunger());
        assertEquals(before.getXp() + 13, after.getXp());
        assertEquals(before.getCoins() + 2, after.getCoins());
        assertEquals(after.getEnergy(), result.get("pet").get("energy").asInt());
        var played = events.findTop10ByPetIdOrderByCreatedAtDesc(petId).stream()
                .filter(event -> event.getType() == PetEventType.PLAYED).toList();
        assertEquals(1, played.size());
        assertEquals("Minijuego Lluvia de frutas: 85 puntos (+13 XP, +2 Monedas)", played.getFirst().getDescription());
    }

    @Test
    void insufficientEnergyReturns422WithoutCountingPlay() throws Exception {
        var pet = pets.findById(petId).orElseThrow();
        pet.setEnergy(14);
        pets.save(pet);
        var response = play("ECHO", 100);
        assertEquals(422, response.statusCode());
        assertEquals("Tu Gotchi está demasiado cansado para jugar. Déjalo descansar.",
                json.readTree(response.body()).get("message").asString());
        var after = pets.findById(petId).orElseThrow();
        assertEquals(14, after.getEnergy());
        assertNull(after.getMinigamePlaysToday());
    }

    @Test
    void invalidGameOrScoreReturns400() throws Exception {
        for (String body : new String[] {"{}", "{\"game\":\"\",\"score\":50}",
                "{\"game\":\"OTHER\",\"score\":50}", "{\"game\":\"CATCH\",\"score\":-1}",
                "{\"game\":\"MEMORY\",\"score\":101}", "{\"game\":\"ECHO\",\"score\":null}"}) {
            assertEquals(400, call("POST", "/api/v1/pet/minigame", body).statusCode(), body);
        }
    }

    @Test
    void ninthPlayHasNoRewardsButStillChangesStatsAndNextDayResetsCap() throws Exception {
        for (int i = 1; i <= 9; i++) {
            assertEquals(200, call("POST", "/api/v1/pet/rest", null).statusCode());
            var before = pets.findById(petId).orElseThrow();
            var response = play("MEMORY", 100);
            assertEquals(200, response.statusCode());
            var result = json.readTree(response.body());
            assertEquals(i <= 8 ? 15 : 0, result.get("xpEarned").asInt());
            assertEquals(i <= 8 ? 3 : 0, result.get("coinsEarned").asInt());
            assertEquals(Math.max(0, 8 - i), result.get("rewardedPlaysLeft").asInt());
            assertEquals(before.getEnergy() - 15, result.get("pet").get("energy").asInt());
        }
        var pet = pets.findById(petId).orElseThrow();
        assertEquals(120, pet.getXp());
        assertEquals(24, pet.getCoins());
        assertEquals(9, pet.getMinigamePlaysToday());
        pet.setMinigameDay(LocalDate.now().minusDays(1));
        pets.save(pet);
        var result = json.readTree(play("ECHO", 40).body());
        assertEquals(6, result.get("xpEarned").asInt());
        assertEquals(1, result.get("coinsEarned").asInt());
        assertEquals(7, result.get("rewardedPlaysLeft").asInt());
    }
}
