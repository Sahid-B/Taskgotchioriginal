package ec.edu.espe.pa.taskgotchi.async;

import ec.edu.espe.pa.taskgotchi.entity.AsyncProcessedEffect;
import ec.edu.espe.pa.taskgotchi.repository.AsyncProcessedEffectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncEffectCoordinator {

    public static final int MAX_ATTEMPTS = 3;
    private final AsyncProcessedEffectRepository repository;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    public <T> boolean execute(UUID eventId, String effectType, Long userId, String requestId, T payload,
                               String payloadType, boolean allowRetry, ThrowingConsumer<T> action) {
        AsyncProcessedEffect effect = inNewTxReturning(() -> claimOrGetInternal(eventId, effectType, userId, requestId, payload, payloadType));
        if (effect.getStatus() == AsyncProcessedEffect.Status.COMPLETED || effect.getStatus() == AsyncProcessedEffect.Status.DEAD) {
            return false;
        }
        if (effect.getStatus() == AsyncProcessedEffect.Status.PROCESSING && effect.getAttempts() > 0
                && effect.getNextAttemptAt() != null && effect.getNextAttemptAt().isAfter(LocalDateTime.now())) {
            return false;
        }
        String thread = Thread.currentThread().getName();
        int startingAttempt = effect.getAttempts();
        for (int attempt = startingAttempt + 1; attempt <= MAX_ATTEMPTS; attempt++) {
            long start = System.nanoTime();
            try {
                final int currentAttempt = attempt;
                inNewTx(() -> {
                    try { action.accept(payload); } catch (Exception e) { throw new RuntimeException(e); }
                    markCompletedInTx(effect.getId(), currentAttempt);
                });
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                log.info("Procesamiento secundario: eventId={} effectType={} requestId={} userId={} thread={} attempt={} elapsedMs={} result=OK",
                        eventId, effectType, requestId, userId, thread, attempt, elapsedMs);
                return true;
            } catch (Exception ex) {
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                Throwable cause = ex.getCause() instanceof Exception c ? c : ex;
                Exception exToHandle = cause instanceof Exception c ? c : new RuntimeException(cause);
                boolean retryable = allowRetry && isRetryable(exToHandle);
                boolean willRetry = retryable && attempt < MAX_ATTEMPTS;
                final int failedAttempt = attempt;
                log.warn("Procesamiento secundario: eventId={} effectType={} requestId={} userId={} thread={} attempt={} elapsedMs={} result=ERROR retryable={} willRetry={} error={}",
                        eventId, effectType, requestId, userId, thread, failedAttempt, elapsedMs, retryable, willRetry, cause.toString());
                inNewTx(() -> recordFailureInTx(effect.getId(), eventId, requestId, userId, exToHandle, failedAttempt, willRetry, retryable));
                if (!willRetry) {
                    return false;
                }
                sleepBackoff(failedAttempt);
            }
        }
        return false;
    }

    private boolean isRetryable(Exception ex) {
        String name = ex.getClass().getName();
        if (name.startsWith("ec.edu.espe.pa.taskgotchi.exception.")) return false;
        if (ex instanceof IllegalArgumentException || ex instanceof jakarta.validation.ValidationException) return false;
        return true;
    }

    private void sleepBackoff(int failedAttempt) {
        long delayMs = 200L * (1L << (failedAttempt - 1));
        try { Thread.sleep(delayMs); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    public AsyncProcessedEffect claimOrGet(UUID eventId, String effectType, Long userId, String requestId,
                                           Object payload, String payloadType) {
        return inNewTxReturning(() -> claimOrGetInternal(eventId, effectType, userId, requestId, payload, payloadType));
    }

    private AsyncProcessedEffect claimOrGetInternal(UUID eventId, String effectType, Long userId, String requestId,
                                                    Object payload, String payloadType) {
        Optional<AsyncProcessedEffect> existing = repository.findByEventIdAndEffectType(eventId, effectType);
        if (existing.isPresent()) {
            return existing.get();
        }
        try {
            AsyncProcessedEffect created = AsyncProcessedEffect.builder()
                    .eventId(eventId)
                    .effectType(effectType)
                    .status(AsyncProcessedEffect.Status.PROCESSING)
                    .attempts(0)
                    .userId(userId)
                    .requestId(requestId)
                    .payloadType(payloadType)
                    .payloadJson(writePayload(payload))
                    .nextAttemptAt(LocalDateTime.now())
                    .build();
            return repository.saveAndFlush(created);
        } catch (DataIntegrityViolationException e) {
            return repository.findByEventIdAndEffectType(eventId, effectType).orElseThrow();
        }
    }

    private void markCompletedInTx(Long id, int attempts) {
        AsyncProcessedEffect effect = repository.findById(id).orElseThrow();
        effect.setStatus(AsyncProcessedEffect.Status.COMPLETED);
        effect.setAttempts(attempts);
        effect.setCompletedAt(LocalDateTime.now());
        effect.setNextAttemptAt(null);
        effect.setLastError(null);
        repository.save(effect);
    }

    private void recordFailureInTx(Long id, UUID eventId, String requestId, Long userId, Exception ex, int attempts, boolean willRetry, boolean retryable) {
        AsyncProcessedEffect effect = repository.findById(id).orElseThrow();
        effect.setAttempts(attempts);
        effect.setLastError(truncate(ex.getClass().getSimpleName() + ": " + String.valueOf(ex.getMessage()), 1000));
        if (!willRetry) {
            effect.setStatus(retryable ? AsyncProcessedEffect.Status.DEAD : AsyncProcessedEffect.Status.DEAD);
            effect.setNextAttemptAt(null);
            log.error("Async effect dead eventId={} requestId={} userId={} effectType={} attempts={} retryable={} error={}",
                    eventId, requestId, userId, effect.getEffectType(), attempts, retryable, ex.toString());
        } else {
            effect.setStatus(AsyncProcessedEffect.Status.FAILED);
            long delayMs = 200L * (1L << (attempts - 1));
            effect.setNextAttemptAt(LocalDateTime.now().plusNanos(delayMs * 1_000_000));
            log.warn("Async effect failed eventId={} requestId={} userId={} effectType={} attempts={} nextRetryAt={} error={}",
                    eventId, requestId, userId, effect.getEffectType(), attempts, effect.getNextAttemptAt(), ex.toString());
        }
        repository.save(effect);
    }

    public void handleFailure(Long id, UUID eventId, String requestId, Long userId, boolean allowRetry, Exception ex) {
        inNewTx(() -> {
            AsyncProcessedEffect effect = repository.findById(id).orElseThrow();
            int attempts = effect.getAttempts() + 1;
            boolean retryable = allowRetry && isRetryable(ex);
            boolean willRetry = retryable && attempts < MAX_ATTEMPTS;
            recordFailureInTx(id, eventId, requestId, userId, ex, attempts, willRetry, retryable);
        });
    }

    private void inNewTx(Runnable runnable) {
        TransactionTemplate tt = new TransactionTemplate(transactionTemplate.getTransactionManager());
        tt.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        tt.executeWithoutResult(status -> runnable.run());
    }

    private <T> T inNewTxReturning(java.util.function.Supplier<T> supplier) {
        TransactionTemplate tt = new TransactionTemplate(transactionTemplate.getTransactionManager());
        tt.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return tt.execute(status -> supplier.get());
    }

    private String writePayload(Object payload) {
        if (payload == null) return null;
        try { return objectMapper.writeValueAsString(payload); } catch (Exception e) { return String.valueOf(payload); }
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }

    @FunctionalInterface
    public interface ThrowingConsumer<T> { void accept(T t) throws Exception; }
}
