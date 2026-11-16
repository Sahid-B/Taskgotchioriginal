package ec.edu.espe.pa.taskgotchi.async;

import ec.edu.espe.pa.taskgotchi.entity.AsyncProcessedEffect;
import ec.edu.espe.pa.taskgotchi.entity.Pet;
import ec.edu.espe.pa.taskgotchi.entity.PetEvent;
import ec.edu.espe.pa.taskgotchi.entity.PetEventType;
import ec.edu.espe.pa.taskgotchi.event.HabitCheckedDomainEvent;
import ec.edu.espe.pa.taskgotchi.event.TaskCompletedDomainEvent;
import ec.edu.espe.pa.taskgotchi.repository.PetEventRepository;
import ec.edu.espe.pa.taskgotchi.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecondaryEffectProcessor {

    public static final String EFFECT_PET_EVENT = "PET_EVENT_LOG";
    public static final String EFFECT_AUDIT = "BUSINESS_AUDIT";
    public static final String EFFECT_NOTIFICATION = "NOTIFICATION";

    private static final Logger BUSINESS_LOG = LoggerFactory.getLogger("BUSINESS");
    private static final Logger NOTIFY_LOG = LoggerFactory.getLogger("NOTIFICATION");

    private final AsyncEffectCoordinator coordinator;
    private final PetRepository petRepository;
    private final PetEventRepository petEventRepository;

    public void processTaskCompleted(TaskCompletedDomainEvent event) {
        // Pet event bitácora (task completion reward)
        String petDesc = "Completó la tarea: " + event.taskTitle() + " (+" + event.xpReward() + " XP, +" + event.coinReward() + " Monedas)";
        coordinator.execute(event.eventId(), EFFECT_PET_EVENT + ":TASK", event.userId(), event.requestId(), event,
                TaskCompletedDomainEvent.class.getName(), true, e -> recordPetEvent(e.userId(), PetEventType.TASK_COMPLETED, petDesc));

        if (event.evolved()) {
            String evoDesc = "¡Felicidades! Tu mascota ha evolucionado a " + event.newStage();
            coordinator.execute(event.eventId(), EFFECT_PET_EVENT + ":EVOLVED", event.userId(), event.requestId(), event,
                    TaskCompletedDomainEvent.class.getName(), true, e -> recordPetEvent(e.userId(), PetEventType.EVOLVED, evoDesc));
            coordinator.execute(event.eventId(), EFFECT_NOTIFICATION + ":EVOLVED", event.userId(), event.requestId(), event,
                    TaskCompletedDomainEvent.class.getName(), true, e -> NOTIFY_LOG.info("eventId={} requestId={} userId={} type=PET_EVOLVED from={} to={}",
                            e.eventId(), e.requestId(), e.userId(), e.previousStage(), e.newStage()));
        }
        if (event.bossDefeated()) {
            String bossRewardDesc = "¡Derrotó al Jefe " + event.bossTitle() + "! (+" + event.bossBonusXp() + " XP, +" + event.bossBonusCoins() + " Monedas)";
            coordinator.execute(event.eventId(), EFFECT_PET_EVENT + ":BOSS_REWARD", event.userId(), event.requestId(), event,
                    TaskCompletedDomainEvent.class.getName(), true, e -> recordPetEvent(e.userId(), PetEventType.TASK_COMPLETED, bossRewardDesc));
            coordinator.execute(event.eventId(), EFFECT_NOTIFICATION + ":BOSS_DEFEATED", event.userId(), event.requestId(), event,
                    TaskCompletedDomainEvent.class.getName(), true, e -> NOTIFY_LOG.info("eventId={} requestId={} userId={} type=BOSS_DEFEATED boss={} taskId={}",
                            e.eventId(), e.requestId(), e.userId(), e.bossTitle(), e.taskId()));
            // boss defeat reward could also evolve pet - we don't have separate evolved flags for second reward in this event; main tx handles state.
        }
    }

    public void processHabitChecked(HabitCheckedDomainEvent event) {
        String desc = "Completó hábito: " + event.habitName() + " (Racha: " + event.currentStreak() + " días) (+" + event.xpReward() + " XP, +" + event.coinReward() + " Monedas)";
        coordinator.execute(event.eventId(), EFFECT_PET_EVENT + ":HABIT", event.userId(), event.requestId(), event,
                HabitCheckedDomainEvent.class.getName(), true, e -> recordPetEvent(e.userId(), PetEventType.TASK_COMPLETED, desc));
        if (event.evolved()) {
            String evoDesc = "¡Felicidades! Tu mascota ha evolucionado a " + event.newStage();
            coordinator.execute(event.eventId(), EFFECT_PET_EVENT + ":EVOLVED", event.userId(), event.requestId(), event,
                    HabitCheckedDomainEvent.class.getName(), true, e -> recordPetEvent(e.userId(), PetEventType.EVOLVED, evoDesc));
            coordinator.execute(event.eventId(), EFFECT_NOTIFICATION + ":EVOLVED", event.userId(), event.requestId(), event,
                    HabitCheckedDomainEvent.class.getName(), true, e -> NOTIFY_LOG.info("eventId={} requestId={} userId={} type=PET_EVOLVED from={} to={}",
                            e.eventId(), e.requestId(), e.userId(), e.previousStage(), e.newStage()));
        }
    }

    public void processAudit(AuditSideEffect audit) {
        coordinator.execute(audit.eventId(), EFFECT_AUDIT, audit.userId(), audit.requestId(), audit,
                AuditSideEffect.class.getName(), true, a -> {
                    if (a.failed()) {
                        BUSINESS_LOG.info("event={} user={} args={} result=FAILED reason={}", a.eventName(), a.userId(), a.argsSummary(), a.failureReason());
                    } else {
                        BUSINESS_LOG.info("event={} user={} args={} result={}", a.eventName(), a.userId(), a.argsSummary(), a.resultSummary());
                    }
                });
    }

    public void reprocess(UUID eventId, String effectType, Long userId, String requestId, Object payload) {
        // re-run through same coordinator path by delegating based on payload type; coordinator will skip if completed/dead
        if (payload instanceof TaskCompletedDomainEvent e) {
            processTaskCompleted(e);
        } else if (payload instanceof HabitCheckedDomainEvent e) {
            processHabitChecked(e);
        } else if (payload instanceof AuditSideEffect a) {
            processAudit(a);
        }
    }

    private void recordPetEvent(Long userId, PetEventType type, String description) {
        Pet pet = petRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Pet not found for async event userId=" + userId));
        PetEvent event = PetEvent.builder()
                .pet(pet)
                .type(type)
                .description(description)
                .build();
        petEventRepository.save(event);
    }
}
