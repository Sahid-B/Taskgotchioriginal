package ec.edu.espe.pa.taskgotchi.async;

import ec.edu.espe.pa.taskgotchi.entity.AsyncProcessedEffect;
import ec.edu.espe.pa.taskgotchi.event.HabitCheckedDomainEvent;
import ec.edu.espe.pa.taskgotchi.event.TaskCompletedDomainEvent;
import ec.edu.espe.pa.taskgotchi.repository.AsyncProcessedEffectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AsyncRetryScheduler {

    private final AsyncProcessedEffectRepository repository;
    private final ObjectMapper objectMapper;
    private final SecondaryEffectProcessor processor;

    @Scheduled(fixedDelayString = "${taskgotchi.async.retry-scan-ms:5000}")
    public void retryFailed() {
        List<AsyncProcessedEffect> due = repository.findTop50ByStatusAndNextAttemptAtBeforeOrderByNextAttemptAtAsc(
                AsyncProcessedEffect.Status.FAILED, LocalDateTime.now());
        for (AsyncProcessedEffect effect : due) {
            try {
                Object payload = readPayload(effect);
                processor.reprocess(effect.getEventId(), effect.getEffectType(), effect.getUserId(), effect.getRequestId(), payload);
            } catch (Exception ex) {
                log.error("Retry scheduling error eventId={} effectType={}", effect.getEventId(), effect.getEffectType(), ex);
            }
        }
    }

    private Object readPayload(AsyncProcessedEffect effect) throws Exception {
        if (effect.getPayloadJson() == null) return null;
        if (TaskCompletedDomainEvent.class.getName().equals(effect.getPayloadType())) {
            return objectMapper.readValue(effect.getPayloadJson(), TaskCompletedDomainEvent.class);
        }
        if (HabitCheckedDomainEvent.class.getName().equals(effect.getPayloadType())) {
            return objectMapper.readValue(effect.getPayloadJson(), HabitCheckedDomainEvent.class);
        }
        if (AuditSideEffect.class.getName().equals(effect.getPayloadType())) {
            return objectMapper.readValue(effect.getPayloadJson(), AuditSideEffect.class);
        }
        return effect.getPayloadJson();
    }
}
