package ec.edu.espe.pa.taskgotchi.service.impl;

import ec.edu.espe.pa.taskgotchi.aspect.Auditable;
import ec.edu.espe.pa.taskgotchi.dto.*;
import ec.edu.espe.pa.taskgotchi.entity.*;
import ec.edu.espe.pa.taskgotchi.exception.BusinessRuleException;
import ec.edu.espe.pa.taskgotchi.exception.ResourceNotFoundException;
import ec.edu.espe.pa.taskgotchi.mapper.PetMapper;
import ec.edu.espe.pa.taskgotchi.repository.*;
import ec.edu.espe.pa.taskgotchi.security.CurrentUserProvider;
import ec.edu.espe.pa.taskgotchi.service.KitchenService;
import ec.edu.espe.pa.taskgotchi.service.PetInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class KitchenServiceImpl implements KitchenService {
    private final PantryItemRepository pantryItemRepository;
    private final PetRepository petRepository;
    private final PetEventRepository petEventRepository;
    private final PetInternalService petInternalService;
    private final CurrentUserProvider currentUserProvider;
    private final PetMapper petMapper;

    @Override
    public List<FoodResponse> getFoods() {
        return Arrays.stream(FoodType.values()).map(food -> new FoodResponse(food.name(),
                food.getDisplayName(), food.getPrice(), food.getHunger(), food.getMood(), food.getEnergy())).toList();
    }

    @Override
    @Transactional
    public KitchenStateResponse getPantry() {
        return state(currentPet());
    }

    @Override
    @Transactional
    @Auditable("KITCHEN_FOOD_BOUGHT")
    public KitchenStateResponse buyFood(BuyFoodRequest request) {
        FoodType food = food(request.foodId());
        Pet pet = currentPet();
        int cost = food.getPrice() * request.quantity();
        if (pet.getCoins() < cost) {
            throw new BusinessRuleException("No tienes suficientes monedas para comprar esto");
        }
        PantryItem item = pantryItemRepository.findByPetIdAndFoodType(pet.getId(), food.name())
                .orElseGet(() -> PantryItem.builder().pet(pet).foodType(food.name()).quantity(0).build());
        pet.setCoins(pet.getCoins() - cost);
        item.setQuantity(item.getQuantity() + request.quantity());
        pantryItemRepository.save(item);
        petRepository.save(pet);
        return state(pet);
    }

    @Override
    @Transactional
    @Auditable("KITCHEN_FOOD_FED")
    public KitchenStateResponse feedFood(FeedFoodRequest request) {
        FoodType food = food(request.foodId());
        Pet pet = currentPet();
        PantryItem item = pantryItemRepository.findByPetIdAndFoodType(pet.getId(), food.name())
                .filter(entry -> entry.getQuantity() > 0)
                .orElseThrow(() -> new BusinessRuleException("No tienes esa comida en tu despensa"));
        if (pet.getHunger() >= 100) {
            throw new BusinessRuleException("Tu Gotchi está lleno. Inténtalo más tarde.");
        }
        pet.setHunger(cap(pet.getHunger() + food.getHunger()));
        pet.setMood(cap(pet.getMood() + food.getMood()));
        pet.setEnergy(cap(pet.getEnergy() + food.getEnergy()));
        pet.setLastUpdatedAt(LocalDateTime.now());
        item.setQuantity(item.getQuantity() - 1);
        pantryItemRepository.save(item);
        petRepository.save(pet);
        List<String> effects = new ArrayList<>();
        if (food.getHunger() > 0) effects.add("+" + food.getHunger() + " Saciedad");
        if (food.getMood() > 0) effects.add("+" + food.getMood() + " Ánimo");
        if (food.getEnergy() > 0) effects.add("+" + food.getEnergy() + " Energía");
        petEventRepository.save(PetEvent.builder().pet(pet).type(PetEventType.FED)
                .description("Le diste " + food.getDisplayName() + " a tu mascota ("
                        + String.join(", ", effects) + ")").build());
        return state(pet);
    }

    private Pet currentPet() {
        return petInternalService.getPetWithTimeDecay(currentUserProvider.getCurrentUserId());
    }

    private FoodType food(String id) {
        try {
            return FoodType.valueOf(id);
        } catch (IllegalArgumentException exception) {
            throw new ResourceNotFoundException("Comida no encontrada");
        }
    }

    private int cap(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private KitchenStateResponse state(Pet pet) {
        List<PantryItemResponse> pantry = pantryItemRepository.findByPetId(pet.getId()).stream()
                .filter(item -> item.getQuantity() > 0)
                .sorted(Comparator.comparingInt(item -> FoodType.valueOf(item.getFoodType()).ordinal()))
                .map(item -> new PantryItemResponse(item.getFoodType(),
                        FoodType.valueOf(item.getFoodType()).getDisplayName(), item.getQuantity())).toList();
        return new KitchenStateResponse(petMapper.toResponse(pet), pantry);
    }
}
