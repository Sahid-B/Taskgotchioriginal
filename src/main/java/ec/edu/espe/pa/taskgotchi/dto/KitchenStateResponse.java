package ec.edu.espe.pa.taskgotchi.dto;

import java.util.List;

public record KitchenStateResponse(PetResponse pet, List<PantryItemResponse> pantry) {}
