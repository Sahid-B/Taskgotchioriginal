package ec.edu.espe.pa.taskgotchi.controller;

import ec.edu.espe.pa.taskgotchi.config.SecurityConfig;
import ec.edu.espe.pa.taskgotchi.dto.PetResponse;
import ec.edu.espe.pa.taskgotchi.model.PetStage;
import ec.edu.espe.pa.taskgotchi.service.PetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PetController.class)
@Import(SecurityConfig.class)
class PetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PetService petService;

    @Test
    void getPetStatus_Returns200Ok() throws Exception {
        PetResponse petResponse = new PetResponse(1L, "Gotchi", PetStage.BABY, 80, 80, 80, 150, 50, "Alegre", LocalDateTime.now());
        when(petService.getPetStatus()).thenReturn(petResponse);

        mockMvc.perform(get("/api/v1/pet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Gotchi"))
                .andExpect(jsonPath("$.stage").value("BABY"));
    }

    @Test
    void feedPet_Returns200Ok() throws Exception {
        PetResponse petResponse = new PetResponse(1L, "Gotchi", PetStage.BABY, 100, 90, 80, 150, 40, "Alegre", LocalDateTime.now());
        when(petService.feedPet()).thenReturn(petResponse);

        mockMvc.perform(post("/api/v1/pet/feed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hunger").value(100));
    }
}
