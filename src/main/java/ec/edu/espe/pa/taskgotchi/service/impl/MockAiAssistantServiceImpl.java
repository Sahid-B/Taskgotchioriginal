package ec.edu.espe.pa.taskgotchi.service.impl;

import ec.edu.espe.pa.taskgotchi.dto.AssistantMessageResponse;
import ec.edu.espe.pa.taskgotchi.dto.DailyMissionResponse;
import ec.edu.espe.pa.taskgotchi.dto.PetMessageRequest;
import ec.edu.espe.pa.taskgotchi.dto.SplitTaskRequest;
import ec.edu.espe.pa.taskgotchi.dto.SplitTaskResponse;
import ec.edu.espe.pa.taskgotchi.entity.Pet;
import ec.edu.espe.pa.taskgotchi.repository.PetRepository;
import ec.edu.espe.pa.taskgotchi.security.CurrentUserProvider;
import ec.edu.espe.pa.taskgotchi.service.AiAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MockAiAssistantServiceImpl implements AiAssistantService {

    private final PetRepository petRepository;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public AssistantMessageResponse getPetMessage(PetMessageRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        Pet pet = petRepository.findByUserId(userId).orElse(null);

        if (pet == null) {
            return new AssistantMessageResponse("¡Hola! Estoy listo para acompañarte en tus tareas.", "HAPPY");
        }

        if (pet.getHunger() < 30) {
            return new AssistantMessageResponse("¡Tengo mucha hambre! Por favor alimentame antes de seguir estudiando.", "HUNGRY");
        } else if (pet.getMood() < 30) {
            return new AssistantMessageResponse("Me siento un poco triste... ¿Jugamos un rato o completamos una tarea?", "SAD");
        } else if (pet.getEnergy() < 30) {
            return new AssistantMessageResponse("¡Uff! Estoy exhausto. Necesito tomar una siesta.", "TIRED");
        } else {
            return new AssistantMessageResponse("¡Eres el mejor estudiante! ¡Vamos a cumplir todos nuestros objetivos de hoy!", "ECSTATIC");
        }
    }

    @Override
    public SplitTaskResponse splitTask(SplitTaskRequest request) {
        String taskName = request.taskDescription();
        List<String> suggestedSubtasks = List.of(
                "Investigar y recopilar información inicial para " + taskName,
                "Estructurar el borrador o esquema de " + taskName,
                "Desarrollar y revisar la solución de " + taskName,
                "Realizar la entrega final o verificación de " + taskName
        );

        return new SplitTaskResponse(taskName, suggestedSubtasks);
    }

    @Override
    public List<DailyMissionResponse> getDailyMissions() {
        return List.of(
                new DailyMissionResponse("m1", "Completar 3 Tareas", "Completa 3 tareas cualesquiera hoy para mantener alegre a tu mascota", 50, 30, false),
                new DailyMissionResponse("m2", "Registrar un Hábito", "Cumple al menos 1 hábito saludable en el día", 30, 20, false),
                new DailyMissionResponse("m3", "Alimentar a la Mascota", "Alimenta a tu mascota para recuperar su hambre", 20, 10, false)
        );
    }
}
