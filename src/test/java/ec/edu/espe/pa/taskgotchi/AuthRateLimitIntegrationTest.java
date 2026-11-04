package ec.edu.espe.pa.taskgotchi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Middleware de bloqueo por fuerza bruta: contexto propio para no agotar el cupo de otras pruebas. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "taskgotchi.security.auth-rate-limit.max-attempts=3",
        "taskgotchi.security.auth-rate-limit.window-seconds=60"})
@ActiveProfiles("h2")
class AuthRateLimitIntegrationTest {

    @LocalServerPort int port;
    @Autowired ObjectMapper json;
    final HttpClient client = HttpClient.newHttpClient();

    HttpResponse<String> call(String method, String path, String body, String token, String clientIp) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .header("Content-Type", "application/json")
                .method(method, HttpRequest.BodyPublishers.ofString(body));
        if (clientIp != null) request.header("X-Forwarded-For", clientIp);
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void rateLimitsLoginAttemptsAndIgnoresSpoofedForwardedHeader() throws Exception {
        String badLogin = "{\"username\":\"nadie\",\"password\":\"incorrecta\"}";
        HttpResponse<String> last = null;
        for (int i = 0; i < 10 && (last == null || last.statusCode() != 429); i++) {
            // Cambiar X-Forwarded-For no debe servir para saltarse el límite.
            last = call("POST", "/api/v1/auth/login", badLogin, null, "10.0.0." + i);
        }
        assertEquals(429, last.statusCode());
        assertThat(last.headers().firstValue("Retry-After")).isPresent();
        assertThat(json.readTree(last.body()).get("message").asString()).contains("Demasiados intentos");
    }
}
