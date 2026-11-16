package ec.edu.espe.pa.taskgotchi.async;

import ec.edu.espe.pa.taskgotchi.event.HabitCheckedDomainEvent;
import ec.edu.espe.pa.taskgotchi.event.TaskCompletedDomainEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class DomainEventAsyncListener {

    private final SecondaryEffectProcessor processor;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("taskgotchiExecutor")
    public void onTaskCompleted(TaskCompletedDomainEvent event) {
        processor.processTaskCompleted(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("taskgotchiExecutor")
    public void onHabitChecked(HabitCheckedDomainEvent event) {
        processor.processHabitChecked(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("taskgotchiExecutor")
    public void onAudit(AuditSideEffect event) {
        processor.processAudit(event);
    }
}
