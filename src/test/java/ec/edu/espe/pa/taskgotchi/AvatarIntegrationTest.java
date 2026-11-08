package ec.edu.espe.pa.taskgotchi;

import ec.edu.espe.pa.taskgotchi.dto.AvatarDto;
import ec.edu.espe.pa.taskgotchi.entity.User;
import ec.edu.espe.pa.taskgotchi.mapper.UserMapper;
import ec.edu.espe.pa.taskgotchi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.*;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "taskgotchi.security.auth-rate-limit.max-attempts=50"})
@ActiveProfiles("h2")
class AvatarIntegrationTest {
    @LocalServerPort int port;
    @Autowired ObjectMapper json;
    @Autowired UserRepository users;
    @Autowired UserMapper mapper;
    final HttpClient client = HttpClient.newHttpClient();

    HttpResponse<String> call(String method, String path, Object body, String token) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .header("Content-Type", "application/json")
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
        if (token != null) request.header("Authorization", "Bearer " + token);
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    HttpResponse<String> register(AvatarDto avatar) throws Exception {
        String name = "avatar" + System.nanoTime();
        var body = new HashMap<String, Object>(Map.of("username", name,
                "email", name + "@espe.edu.ec", "password", "Sintetica_2026"));
        if (avatar != null) body.put("avatar", avatar);
        return call("POST", "/api/v1/auth/register", body, null);
    }

    @Test
    void registerWithAvatarReturnsAndPersistsLowercaseColors() throws Exception {
        var response = register(new AvatarDto("FEMALE", "#ABCDEF", "#123ABC", "#FEDCBA"));
        assertEquals(201, response.statusCode());
        var user = json.readTree(response.body()).get("user");
        var expected = new AvatarDto("FEMALE", "#abcdef", "#123abc", "#fedcba");
        assertEquals(json.valueToTree(expected), user.get("avatar"));
        assertEquals(expected, mapper.toResponse(users.findById(user.get("id").asLong()).orElseThrow()).avatar());
    }

    @Test
    void registerWithoutAvatarReturnsAndPersistsDefaults() throws Exception {
        var response = register(null);
        assertEquals(201, response.statusCode());
        var user = json.readTree(response.body()).get("user");
        assertEquals(json.valueToTree(AvatarDto.defaults()), user.get("avatar"));
        var stored = users.findById(user.get("id").asLong()).orElseThrow();
        assertEquals("MALE", stored.getAvatarGender());
        assertEquals("#e3a35c", stored.getShirtColor());
        assertEquals("#203441", stored.getPantsColor());
        assertEquals("#283f3c", stored.getHairColor());
    }

    @Test
    void updateAvatarPersistsForCurrentUserOnly() throws Exception {
        var registered = json.readTree(register(null).body());
        var other = json.readTree(register(null).body());
        String token = registered.get("token").asString();
        var avatar = new AvatarDto("FEMALE", "#AABBCC", "#DDEEFF", "#ABC123");
        var expected = new AvatarDto("FEMALE", "#aabbcc", "#ddeeff", "#abc123");
        var updated = call("PUT", "/api/v1/users/me/avatar", avatar, token);
        assertEquals(200, updated.statusCode());
        assertEquals(json.valueToTree(expected), json.readTree(updated.body()).get("avatar"));
        var profile = call("GET", "/api/v1/users/me", null, token);
        assertEquals(200, profile.statusCode());
        assertEquals(json.valueToTree(expected), json.readTree(profile.body()).get("avatar"));
        assertEquals(AvatarDto.defaults(), mapper.toResponse(users.findById(
                other.get("user").get("id").asLong()).orElseThrow()).avatar());
    }

    @Test
    void invalidColorsAndGenderReturn400() throws Exception {
        String token = json.readTree(register(null).body()).get("token").asString();
        for (var invalid : new AvatarDto[] {
                new AvatarDto("MALE", "red", "#203441", "#283f3c"),
                new AvatarDto("MALE", "#e3a35c", "#12345", "#283f3c"),
                new AvatarDto("MALE", "#e3a35c", "#203441", "#GGGGGG"),
                new AvatarDto("OTHER", "#e3a35c", "#203441", "#283f3c"),
                new AvatarDto("", "", "", "")}) {
            assertEquals(400, call("PUT", "/api/v1/users/me/avatar", invalid, token).statusCode());
            assertEquals(400, register(invalid).statusCode());
        }
    }

    @Test
    void legacyNullColumnsUseDefaultsIndividually() {
        User user = User.builder().build();
        assertEquals(AvatarDto.defaults(), mapper.toResponse(user).avatar());
        user.setShirtColor("#112233");
        assertEquals(new AvatarDto("MALE", "#112233", "#203441", "#283f3c"), mapper.toResponse(user).avatar());
    }
}
