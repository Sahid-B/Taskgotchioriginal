package ec.edu.espe.pa.taskgotchi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.*;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("h2")
class TaskFlowIntegrationTest {
    @LocalServerPort int port;
    @Autowired ObjectMapper json;
    final HttpClient client = HttpClient.newHttpClient();

    HttpResponse<String> call(String method, String path, String body) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .header("Content-Type", "application/json")
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void realDatabaseFlowHonorsContractAndDoesNotRewardTwice() throws Exception {
        String path = "/api/v1/tasks";
        String body = json.writeValueAsString(Map.of("code", "IT-" + System.nanoTime(), "title", "Mision de integracion",
                "estimatedHours", 1.25, "priority", "HIGH", "difficulty", "MEDIUM", "active", true));
        var before = json.readTree(call("GET", "/api/v1/pet", null).body());
        var created = call("POST", path, body);
        assertEquals(201, created.statusCode());
        assertTrue(created.headers().firstValue("Location").isPresent());
        long id = json.readTree(created.body()).get("id").asLong();
        assertEquals(409, call("POST", path, body).statusCode());
        assertEquals(200, call("GET", path + "/buscar?title=integracion", null).statusCode());
        var updated = call("PUT", path + "/" + id, body);
        assertEquals(200, updated.statusCode());
        assertFalse(json.readTree(updated.body()).get("updatedAt").isNull());
        assertEquals(200, call("PATCH", path + "/" + id + "/complete", null).statusCode());
        assertEquals(422, call("PATCH", path + "/" + id + "/complete", null).statusCode());
        assertEquals(422, call("PUT", path + "/" + id, body).statusCode());
        var after = json.readTree(call("GET", "/api/v1/pet", null).body());
        assertEquals(before.get("xp").asInt() + 50, after.get("xp").asInt());
        assertEquals(before.get("coins").asInt() + 25, after.get("coins").asInt());
        assertEquals(400, call("POST", path, "{}").statusCode());
        var invalidLogin = call("POST", "/api/v1/auth/login", "{\"username\":\"devstudent\",\"password\":\"incorrecta\"}");
        assertEquals(401, invalidLogin.statusCode());
        assertEquals(6, json.readTree(invalidLogin.body()).size());
        var duplicateUser = call("POST", "/api/v1/auth/register",
                "{\"username\":\"devstudent\",\"email\":\"otro@espe.edu.ec\",\"password\":\"Sintetica_2026\"}");
        assertEquals(409, duplicateUser.statusCode());
        assertEquals(400, call("GET", path + "/abc", null).statusCode());
        assertEquals(400, call("GET", path + "/buscar", null).statusCode());
        assertEquals(405, call("PATCH", path + "/" + id, "{}").statusCode());
        assertEquals(204, call("DELETE", path + "/" + id, null).statusCode());
        var missing = call("GET", path + "/" + id, null);
        assertEquals(404, missing.statusCode());
        assertEquals(6, json.readTree(missing.body()).size());
        assertEquals(404, call("GET", "/api/v1/inexistente", null).statusCode());
    }
}
