package ec.edu.espe.pa.taskgotchi.async;

import ec.edu.espe.pa.taskgotchi.entity.*;
import ec.edu.espe.pa.taskgotchi.repository.*;
import ec.edu.espe.pa.taskgotchi.service.PetInternalService;
import ec.edu.espe.pa.taskgotchi.service.RewardOutcome;
import ec.edu.espe.pa.taskgotchi.service.TaskService;
import ec.edu.espe.pa.taskgotchi.security.AuthenticatedUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;

@SpringBootTest
@ActiveProfiles("h2")
class AsyncProcessingIntegrationTest {

    @Autowired TaskService taskService;
    @Autowired PetInternalService petInternalService;
    @Autowired UserRepository userRepository;
    @Autowired PetRepository petRepository;
    @Autowired TaskRepository taskRepository;
    @Autowired PetEventRepository petEventRepository;
    @Autowired AsyncProcessedEffectRepository effectRepository;
    @Autowired SecondaryEffectProcessor secondaryEffectProcessor;
    @Autowired AsyncEffectCoordinator coordinator;
    @Autowired PlatformTransactionManager txManager;

    @MockitoSpyBean PetEventRepository petEventRepositorySpy;

    private TransactionTemplate tx;
    private User user;
    private Pet pet;

    @BeforeEach
    void setUp() {
        tx = new TransactionTemplate(txManager);
        MDC.put("requestId", "req-async-test");
        String suffix = String.valueOf(System.nanoTime());
        user = userRepository.save(User.builder()
                .username("async_" + suffix)
                .email("async_" + suffix + "@test.com")
                .passwordHash("x")
                .build());
        pet = petRepository.save(Pet.builder()
                .user(user)
                .name("Gotchi async")
                .xp(0).coins(0).stage(PetStage.EGG)
                .hunger(80).mood(80).energy(80)
                .lastUpdatedAt(LocalDateTime.now())
                .build());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new AuthenticatedUser(user.getId(), user.getUsername()), null, List.of()));
    }

    @AfterEach
    void tearDown() {
        reset(petEventRepositorySpy);
        SecurityContextHolder.clearContext();
        MDC.clear();
    }

    @Test
    void completeTask_commitsRewards_andProcessesSecondaryEffectsAfterCommitInOtherThread() {
        Task task = taskRepository.save(Task.builder()
                .user(user).code("ASYNC-1").title("Misión async")
                .estimatedHours(new BigDecimal("1.00")).active(true)
                .priority(TaskPriority.MEDIUM).difficulty(TaskDifficulty.MEDIUM)
                .status(TaskStatus.PENDING).xpReward(50).coinReward(25)
                .build());

        taskService.completeTask(task.getId());

        Task completed = taskRepository.findById(task.getId()).orElseThrow();
        Pet updatedPet = petRepository.findByUserId(user.getId()).orElseThrow();
        assertEquals(TaskStatus.COMPLETED, completed.getStatus());
        assertEquals(50, updatedPet.getXp());
        assertEquals(25, updatedPet.getCoins());

        awaitTrue(() -> !effectRepository.findAll().stream()
                .filter(e -> "PET_EVENT_LOG:TASK".equals(e.getEffectType()))
                .filter(e -> user.getId().equals(e.getUserId()))
                .filter(e -> e.getStatus() == AsyncProcessedEffect.Status.COMPLETED)
                .toList().isEmpty(), Duration.ofSeconds(5));

        List<PetEvent> events = petEventRepository.findTop10ByPetIdOrderByCreatedAtDesc(pet.getId());
        assertTrue(events.stream().anyMatch(e -> e.getDescription() != null && e.getDescription().contains("Misión async")));

        AsyncProcessedEffect effect = effectRepository.findAll().stream()
                .filter(e -> "PET_EVENT_LOG:TASK".equals(e.getEffectType()))
                .filter(e -> user.getId().equals(e.getUserId()))
                .findFirst().orElseThrow();
        assertEquals(AsyncProcessedEffect.Status.COMPLETED, effect.getStatus());
        assertEquals("req-async-test", effect.getRequestId());
        assertEquals(user.getId(), effect.getUserId());
    }

    @Test
    void rollback_doesNotGenerateSecondaryEffects() {
        Task task = taskRepository.save(Task.builder()
                .user(user).code("ASYNC-RB").title("Misión rollback")
                .estimatedHours(new BigDecimal("1.00")).active(true)
                .priority(TaskPriority.MEDIUM).difficulty(TaskDifficulty.MEDIUM)
                .status(TaskStatus.PENDING).xpReward(50).coinReward(25)
                .build());
        long beforeEffects = effectRepository.count();
        long beforeEvents = petEventRepository.count();

        assertThrows(RuntimeException.class, () -> tx.executeWithoutResult(status -> {
            taskService.completeTask(task.getId());
            // La transacción principal se revierte: no debe ejecutarse el procesamiento AFTER_COMMIT
            throw new RuntimeException("boom");
        }));

        Task reloaded = taskRepository.findById(task.getId()).orElseThrow();
        Pet reloadedPet = petRepository.findByUserId(user.getId()).orElseThrow();
        assertEquals(TaskStatus.PENDING, reloaded.getStatus());
        assertEquals(0, reloadedPet.getXp());
        assertEquals(0, reloadedPet.getCoins());
        sleepQuietly(400);
        assertEquals(beforeEffects, effectRepository.count());
        assertEquals(beforeEvents, petEventRepository.count());
    }

    @Test
    void retrySameEvent_doesNotDuplicatePetEventAndPreservesIdentity() {
        UUID eventId = UUID.randomUUID();
        var event = new ec.edu.espe.pa.taskgotchi.event.TaskCompletedDomainEvent(
                eventId, user.getId(), 999L, "Idempotente", 50, 25, "req-idem",
                false, "EGG", "EGG", false, null, 0, 0);

        secondaryEffectProcessor.processTaskCompleted(event);
        secondaryEffectProcessor.processTaskCompleted(event); // reintento del mismo evento

        long taskLogs = petEventRepository.findTop10ByPetIdOrderByCreatedAtDesc(pet.getId()).stream()
                .filter(e -> e.getDescription() != null && e.getDescription().contains("Idempotente"))
                .count();
        assertEquals(1, taskLogs, "La bitácora no debe duplicarse al reintentar el mismo eventId");

        AsyncProcessedEffect effect = effectRepository.findByEventIdAndEffectType(eventId, "PET_EVENT_LOG:TASK").orElseThrow();
        assertEquals(AsyncProcessedEffect.Status.COMPLETED, effect.getStatus());
        assertEquals("req-idem", effect.getRequestId());
        assertEquals(user.getId(), effect.getUserId());
    }

    @Test
    void secondaryFailure_doesNotLoseConfirmedRewards_andRegistersFailedEffect() {
        Task task = taskRepository.save(Task.builder()
                .user(user).code("ASYNC-FAIL").title("Misión falla secundaria")
                .estimatedHours(new BigDecimal("1.00")).active(true)
                .priority(TaskPriority.MEDIUM).difficulty(TaskDifficulty.MEDIUM)
                .status(TaskStatus.PENDING).xpReward(50).coinReward(25)
                .build());

        // Forzar fallo transitorio solo en la bitácora de esta misión (3 intentos)
        java.util.concurrent.atomic.AtomicInteger calls = new java.util.concurrent.atomic.AtomicInteger();
        doAnswer(invocation -> {
            PetEvent ev = invocation.getArgument(0);
            if (ev.getDescription() != null && ev.getDescription().contains("Misión falla secundaria")) {
                if (calls.incrementAndGet() <= 3) {
                    throw new RuntimeException("transient");
                }
            }
            return invocation.callRealMethod();
        }).when(petEventRepositorySpy).save(any(PetEvent.class));

        taskService.completeTask(task.getId());

        // Recompensas confirmadas en transacción principal (no dependen del efecto secundario)
        Pet updatedPet = petRepository.findByUserId(user.getId()).orElseThrow();
        assertEquals(50, updatedPet.getXp());
        assertEquals(25, updatedPet.getCoins());
        assertEquals(TaskStatus.COMPLETED, taskRepository.findById(task.getId()).orElseThrow().getStatus());

        awaitTrue(() -> effectRepository.findAll().stream()
                .anyMatch(e -> e.getStatus() == AsyncProcessedEffect.Status.DEAD
                        && "PET_EVENT_LOG:TASK".equals(e.getEffectType())), Duration.ofSeconds(8));

        AsyncProcessedEffect dead = effectRepository.findAll().stream()
                .filter(e -> e.getStatus() == AsyncProcessedEffect.Status.DEAD
                        && "PET_EVENT_LOG:TASK".equals(e.getEffectType()))
                .findFirst().orElseThrow();
        assertEquals("req-async-test", dead.getRequestId());
        assertEquals(user.getId(), dead.getUserId());
        assertEquals(3, dead.getAttempts());
        assertNotNull(dead.getLastError());
    }

    private static void awaitTrue(BooleanSupplier condition, Duration timeout) {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) return;
            sleepQuietly(50);
        }
        fail("Condition not met within " + timeout);
    }

    private static void sleepQuietly(long ms) {
        try { TimeUnit.MILLISECONDS.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
