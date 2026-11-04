package ec.edu.espe.pa.taskgotchi.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class AuthRateLimitFilterTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-11-04T12:00:00Z"), ZoneOffset.UTC);

    private int status(AuthRateLimitFilter filter, String contextPath, String ip) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", contextPath + "/api/v1/auth/login");
        request.setContextPath(contextPath);
        request.setRemoteAddr(ip);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response.getStatus();
    }

    @Test
    void limitsAttemptsEvenUnderAContextPath() throws Exception {
        AuthRateLimitFilter filter = new AuthRateLimitFilter(2, 60, 100, JsonMapper.builder().build(), clock);

        assertThat(status(filter, "/taskgotchi", "1.1.1.1")).isEqualTo(200);
        assertThat(status(filter, "/taskgotchi", "1.1.1.1")).isEqualTo(200);
        assertThat(status(filter, "/taskgotchi", "1.1.1.1")).isEqualTo(429);
    }

    @Test
    void keepsMemoryBoundedAndFailsClosedWhenFull() throws Exception {
        AuthRateLimitFilter filter = new AuthRateLimitFilter(5, 60, 2, JsonMapper.builder().build(), clock);

        assertThat(status(filter, "", "1.1.1.1")).isEqualTo(200);
        assertThat(status(filter, "", "2.2.2.2")).isEqualTo(200);
        assertThat(status(filter, "", "3.3.3.3")).isEqualTo(429);
        assertThat(filter.trackedClients()).isEqualTo(2);
        assertThat(status(filter, "", "1.1.1.1")).isEqualTo(200);
    }
}
