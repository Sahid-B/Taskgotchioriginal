package ec.edu.espe.pa.taskgotchi.service;

import ec.edu.espe.pa.taskgotchi.entity.TaskStatus;
import ec.edu.espe.pa.taskgotchi.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Mantiene persistido el estado OVERDUE de las tareas cuya fecha limite ya paso.
 * Se ejecuta al arrancar la aplicacion y luego a intervalos configurables.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OverdueTaskScheduler {

    private final TaskRepository taskRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void marcarVencidasAlArrancar() {
        actualizarVencidas();
    }

    @Scheduled(fixedDelayString = "${taskgotchi.tasks.overdue-check-ms:60000}")
    @Transactional
    public void marcarVencidasPeriodicamente() {
        actualizarVencidas();
    }

    void actualizarVencidas() {
        int actualizadas = taskRepository.markOverdueTasks(
                TaskStatus.PENDING,
                TaskStatus.OVERDUE,
                LocalDateTime.now()
        );
        if (actualizadas > 0) {
            log.info("Se marcaron {} tarea(s) como OVERDUE", actualizadas);
        }
    }
}
