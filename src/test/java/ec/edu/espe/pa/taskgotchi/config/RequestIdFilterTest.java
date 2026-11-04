package ec.edu.espe.pa.taskgotchi.config;

import ec.edu.espe.pa.taskgotchi.controller.PetController;
import ec.edu.espe.pa.taskgotchi.service.PetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PetController.class)
@Import(SecurityConfig.class)
@ExtendWith(OutputCaptureExtension.class)
class RequestIdFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PetService petService;

    @Test
    void generatesRequestIdWhenClientSendsNone() throws Exception {
        String id = mockMvc.perform(get("/api/v1/pet"))
                .andExpect(header().exists(RequestIdFilter.HEADER))
                .andReturn().getResponse().getHeader(RequestIdFilter.HEADER);

        assertThat(id).matches("[0-9a-f-]{36}");
    }

    @Test
    void reusesValidClientRequestId() throws Exception {
        mockMvc.perform(get("/api/v1/pet").header(RequestIdFilter.HEADER, "abc-123"))
                .andExpect(header().string(RequestIdFilter.HEADER, "abc-123"));
    }

    @Test
    void monitorsRequestsRejectedBeforeReachingAController(CapturedOutput output) throws Exception {
        mockMvc.perform(delete("/api/v1/pet").header(RequestIdFilter.HEADER, "trace-405"))
                .andExpect(status().isMethodNotAllowed());

        assertThat(output).contains("[trace-405]").contains("DELETE /api/v1/pet -> 405");
    }

    @Test
    void replacesUnsafeClientRequestId() {
        assertThat(RequestIdFilter.resolve("bad id\nINJECTED")).isNotEqualTo("bad id\nINJECTED").hasSize(36);
        assertThat(RequestIdFilter.resolve("x".repeat(65))).hasSize(36);
    }
}
