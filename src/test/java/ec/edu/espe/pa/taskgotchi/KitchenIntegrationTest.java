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
class KitchenIntegrationTest {
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
        String name = "kitchen" + System.nanoTime();
        var response = call("POST", "/api/v1/auth/register", json.writeValueAsString(Map.of(
                "username", name, "email", name + "@espe.edu.ec", "password", "Sintetica_2026")));
        assertEquals(201, response.statusCode());
        token = json.readTree(response.body()).get("token").asString();
        petId = json.readTree(call("GET", "/api/v1/pet", null).body()).get("id").asLong();
    }

    void preparePet(int coins, int hunger) {
        var pet = pets.findById(petId).orElseThrow();
        pet.setCoins(coins);
        pet.setHunger(hunger);
        pet.setMood(50);
        pet.setEnergy(50);
        pet.setLastUpdatedAt(java.time.LocalDateTime.now());
        pets.save(pet);
    }

    HttpResponse<String> buy(String food, int quantity) throws Exception {
        return call("POST", "/api/v1/kitchen/buy", json.writeValueAsString(Map.of("foodId", food, "quantity", quantity)));
    }

    HttpResponse<String> feed(String food) throws Exception {
        return call("POST", "/api/v1/kitchen/feed", json.writeValueAsString(Map.of("foodId", food)));
    }

    @Test
    void listsCatalogInOrderAndRequiresAuthentication() throws Exception {
        var response = call("GET", "/api/v1/kitchen/foods", null);
        assertEquals(200, response.statusCode());
        var catalog = json.readTree(response.body());
        String[] ids = {"APPLE", "CARROT", "MUSHROOM_SOUP", "HONEY_TEA", "BERRY_PIE", "STAR_CANDY"};
        int[][] values = {{5,15,2,0}, {4,12,0,3}, {12,35,5,0}, {8,5,5,25}, {20,30,20,0}, {15,5,30,5}};
        assertEquals(6, catalog.size());
        for (int i = 0; i < ids.length; i++) {
            assertEquals(ids[i], catalog.get(i).get("id").asString());
            String[] fields = {"price", "hunger", "mood", "energy"};
            for (int j = 0; j < fields.length; j++) assertEquals(values[i][j], catalog.get(i).get(fields[j]).asInt());
        }
        token = null;
        for (String path : new String[]{"foods", "pantry"}) {
            assertEquals(401, call("GET", "/api/v1/kitchen/" + path, null).statusCode());
        }
    }

    @Test
    void buysAndAccumulatesFoodAndDeductsCoins() throws Exception {
        preparePet(100, 50);
        assertEquals(200, buy("APPLE", 2).statusCode());
        var response = buy("APPLE", 1);
        assertEquals(200, response.statusCode());
        var state = json.readTree(response.body());
        assertEquals(85, state.get("pet").get("coins").asInt());
        assertEquals("APPLE", state.get("pantry").get(0).get("foodId").asString());
        assertEquals("Manzana", state.get("pantry").get(0).get("name").asString());
        assertEquals(3, state.get("pantry").get(0).get("quantity").asInt());
        assertEquals(85, pets.findById(petId).orElseThrow().getCoins());
        assertEquals(3, json.readTree(call("GET", "/api/v1/kitchen/pantry", null).body())
                .get("pantry").get(0).get("quantity").asInt());
    }

    @Test
    void insufficientCoinsReturns422() throws Exception {
        preparePet(0, 50);
        var response = buy("APPLE", 1);
        assertEquals(422, response.statusCode());
        assertEquals("No tienes suficientes monedas para comprar esto", json.readTree(response.body()).get("message").asString());
        assertEquals(0, json.readTree(call("GET", "/api/v1/kitchen/pantry", null).body()).get("pantry").size());
    }

    @Test
    void feedsConsumesStockAndRecordsFedEvent() throws Exception {
        preparePet(100, 50);
        assertEquals(200, buy("APPLE", 2).statusCode());
        var response = feed("APPLE");
        assertEquals(200, response.statusCode());
        var state = json.readTree(response.body());
        assertEquals(65, state.get("pet").get("hunger").asInt());
        assertEquals(52, state.get("pet").get("mood").asInt());
        assertEquals(1, state.get("pantry").get(0).get("quantity").asInt());
        var fed = events.findTop10ByPetIdOrderByCreatedAtDesc(petId).stream()
                .filter(event -> event.getType() == PetEventType.FED).toList();
        assertEquals(1, fed.size());
        assertEquals("Le diste Manzana a tu mascota (+15 Saciedad, +2 Ánimo)", fed.getFirst().getDescription());
        assertEquals(200, feed("APPLE").statusCode());
        assertEquals(0, json.readTree(call("GET", "/api/v1/kitchen/pantry", null).body()).get("pantry").size());
        assertEquals(422, feed("APPLE").statusCode());
    }

    @Test
    void missingFoodAndFullPetReturn422WithoutConsumingStock() throws Exception {
        preparePet(100, 100);
        var missing = feed("CARROT");
        assertEquals(422, missing.statusCode());
        assertEquals("No tienes esa comida en tu despensa", json.readTree(missing.body()).get("message").asString());
        assertEquals(200, buy("CARROT", 1).statusCode());
        var full = feed("CARROT");
        assertEquals(422, full.statusCode());
        assertEquals("Tu Gotchi está lleno. Inténtalo más tarde.", json.readTree(full.body()).get("message").asString());
        assertEquals(1, json.readTree(call("GET", "/api/v1/kitchen/pantry", null).body()).get("pantry").get(0).get("quantity").asInt());
    }

    @Test
    void invalidBodiesReturn400AndUnknownFoodReturns404() throws Exception {
        for (String body : new String[]{"{}", "{\"foodId\":\"\",\"quantity\":1}",
                "{\"foodId\":\"APPLE\",\"quantity\":0}", "{\"foodId\":\"APPLE\",\"quantity\":11}",
                "{\"foodId\":\"APPLE\",\"quantity\":null}"}) {
            assertEquals(400, call("POST", "/api/v1/kitchen/buy", body).statusCode(), body);
        }
        assertEquals(400, call("POST", "/api/v1/kitchen/feed", "{}").statusCode());
        assertEquals(400, feed("").statusCode());
        var response = buy("OTHER", 1);
        assertEquals(404, response.statusCode());
        assertEquals("Comida no encontrada", json.readTree(response.body()).get("message").asString());
    }

    @Test
    void pantryBuyAndFeedApplyTimeDecayAndEffectsAreCapped() throws Exception {
        preparePet(100, 95);
        assertEquals(200, buy("STAR_CANDY", 2).statusCode());
        var pet = pets.findById(petId).orElseThrow();
        pet.setLastUpdatedAt(java.time.LocalDateTime.now().minusHours(2));
        pets.save(pet);
        var pantry = json.readTree(call("GET", "/api/v1/kitchen/pantry", null).body());
        assertEquals(85, pantry.get("pet").get("hunger").asInt());
        pet = pets.findById(petId).orElseThrow();
        pet.setLastUpdatedAt(java.time.LocalDateTime.now().minusHours(1));
        pets.save(pet);
        assertEquals(80, json.readTree(buy("APPLE", 1).body()).get("pet").get("hunger").asInt());
        pet = pets.findById(petId).orElseThrow();
        pet.setHunger(100);
        pet.setMood(99);
        pet.setEnergy(99);
        pet.setLastUpdatedAt(java.time.LocalDateTime.now().minusHours(1));
        pets.save(pet);
        var state = json.readTree(feed("STAR_CANDY").body());
        assertEquals(100, state.get("pet").get("hunger").asInt());
        assertEquals(100, state.get("pet").get("mood").asInt());
        assertEquals(99, state.get("pet").get("energy").asInt());
    }
}