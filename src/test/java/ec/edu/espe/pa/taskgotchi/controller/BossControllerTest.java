package ec.edu.espe.pa.taskgotchi.controller;

import tools.jackson.databind.ObjectMapper;
import ec.edu.espe.pa.taskgotchi.config.SecurityConfig;
import ec.edu.espe.pa.taskgotchi.dto.BossResponse;
import ec.edu.espe.pa.taskgotchi.dto.CreateBossRequest;
import ec.edu.espe.pa.taskgotchi.entity.BossStatus;
import ec.edu.espe.pa.taskgotchi.service.BossService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BossController.class)
@Import(SecurityConfig.class)
class BossControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BossService bossService;

    @Test
    void getAllBosses_Returns200Ok() throws Exception {
        when(bossService.getAllBosses()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/bosses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void createBoss_ValidRequest_Returns201Created() throws Exception {
        CreateBossRequest request = new CreateBossRequest("Examen Final", 200, "100 coins", List.of());
        BossResponse response = new BossResponse(1L, "Examen Final", 200, 200, BossStatus.ACTIVE, "100 coins", List.of());

        when(bossService.createBoss(any(CreateBossRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/bosses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.title").value("Examen Final"))
                .andExpect(jsonPath("$.maxHp").value(200));
    }
}
