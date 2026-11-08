package ec.edu.espe.pa.taskgotchi.dto;

/** Resultado de golpear a la mascota: ánimo perdido (0 si aún está en espera). */
public record BonkResponse(PetResponse pet, int moodLost) {}
