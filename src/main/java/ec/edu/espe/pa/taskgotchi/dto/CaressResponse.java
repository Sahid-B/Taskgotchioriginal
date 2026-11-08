package ec.edu.espe.pa.taskgotchi.dto;

/** Resultado de acariciar a la mascota: ánimo ganado y segundos hasta la próxima caricia con efecto. */
public record CaressResponse(PetResponse pet, int moodGained, int cooldownSeconds) {}
