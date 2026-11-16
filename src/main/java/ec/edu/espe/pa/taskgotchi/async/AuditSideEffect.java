package ec.edu.espe.pa.taskgotchi.async;

import java.util.UUID;

public record AuditSideEffect(
        UUID eventId,
        Long userId,
        String requestId,
        String eventName,
        String argsSummary,
        String resultSummary,
        boolean failed,
        String failureReason
) {}
