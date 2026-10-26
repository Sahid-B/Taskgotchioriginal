package ec.edu.espe.pa.taskgotchi.service.impl;

import ec.edu.espe.pa.taskgotchi.dto.PageResponse;
import ec.edu.espe.pa.taskgotchi.dto.PetEventResponse;
import ec.edu.espe.pa.taskgotchi.dto.PetResponse;
import ec.edu.espe.pa.taskgotchi.exception.BusinessRuleException;
import ec.edu.espe.pa.taskgotchi.exception.ResourceNotFoundException;
import ec.edu.espe.pa.taskgotchi.mapper.PetMapper;
import ec.edu.espe.pa.taskgotchi.entity.*;
import ec.edu.espe.pa.taskgotchi.repository.PetEventRepository;
import ec.edu.espe.pa.taskgotchi.repository.PetRepository;
import ec.edu.espe.pa.taskgotchi.repository.UserRepository;
import ec.edu.espe.pa.taskgotchi.security.CurrentUserProvider;
import ec.edu.espe.pa.taskgotchi.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PetServiceImpl implements PetService {

    private final PetRepository petRepository;
    private final PetEventRepository petEventRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final PetMapper petMapper;

    @Value("${taskgotchi.pet.xp-thresholds.baby:100}")
    private int babyThreshold;

    @Value("${taskgotchi.pet.xp-thresholds.young:300}")
    private int youngThreshold;

    @Value("${taskgotchi.pet.xp-thresholds.adult:700}")
    private int adultThreshold;

    @Value("${taskgotchi.pet.xp-thresholds.legendary:1500}")
    private int legendaryThreshold;

    @Override
    @Transactional
    public PetResponse getPetStatus() {
        Long userId = currentUserProvider.getCurrentUserId();
        Pet pet = getPetByUserId(userId);
        applyTimeDecay(pet);
        pet = petRepository.save(pet);
        return petMapper.toResponse(pet);
    }

    @Override
    @Transactional
    public PetResponse feedPet() {
        Long userId = currentUserProvider.getCurrentUserId();
        Pet pet = getPetByUserId(userId);
        applyTimeDecay(pet);

        int cost = 10;
        if (pet.getCoins() < cost) {
            throw new BusinessRuleException("Monedas insuficientes para alimentar a la mascota");
        }

        pet.setCoins(pet.getCoins() - cost);
        pet.setHunger(Math.min(100, pet.getHunger() + 30));
        pet.setMood(Math.min(100, pet.getMood() + 10));
        pet.setLastUpdatedAt(LocalDateTime.now());
        pet = petRepository.save(pet);

        recordPetEvent(pet, PetEventType.FED, "Alimentaste a tu mascota (+30 Hambre, +10 Ánimo)");

        return petMapper.toResponse(pet);
    }

    @Override
    @Transactional
    public PetResponse playWithPet() {
        Long userId = currentUserProvider.getCurrentUserId();
        Pet pet = getPetByUserId(userId);
        applyTimeDecay(pet);

        if (pet.getEnergy() < 15) {
            throw new BusinessRuleException("La mascota está demasiado cansada para jugar");
        }

        pet.setMood(Math.min(100, pet.getMood() + 25));
        pet.setEnergy(Math.max(0, pet.getEnergy() - 15));
        pet.setHunger(Math.max(0, pet.getHunger() - 10));
        pet.setLastUpdatedAt(LocalDateTime.now());
        pet = petRepository.save(pet);

        recordPetEvent(pet, PetEventType.PLAYED, "Jugaste con tu mascota (+25 Ánimo, -15 Energía)");

        return petMapper.toResponse(pet);
    }

    @Override
    @Transactional
    public PetResponse restPet() {
        Long userId = currentUserProvider.getCurrentUserId();
        Pet pet = getPetByUserId(userId);
        applyTimeDecay(pet);

        pet.setEnergy(Math.min(100, pet.getEnergy() + 40));
        pet.setHunger(Math.max(0, pet.getHunger() - 10));
        pet.setLastUpdatedAt(LocalDateTime.now());
        pet = petRepository.save(pet);

        recordPetEvent(pet, PetEventType.RESTED, "Tu mascota descansasó (+40 Energía)");

        return petMapper.toResponse(pet);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PetEventResponse> getPetEvents(Pageable pageable) {
        Long userId = currentUserProvider.getCurrentUserId();
        Pet pet = getPetByUserId(userId);
        return PageResponse.from(petEventRepository.findByPetIdOrderByCreatedAtDesc(pet.getId(), pageable)
                .map(petMapper::toEventResponse));
    }

    @Override
    @Transactional
    public Pet addXpAndCoins(Long userId, int xp, int coins, String eventReason) {
        Pet pet = getPetByUserId(userId);
        applyTimeDecay(pet);

        pet.setXp(pet.getXp() + xp);
        pet.setCoins(pet.getCoins() + coins);

        evaluateEvolution(pet);

        pet.setLastUpdatedAt(LocalDateTime.now());
        pet = petRepository.save(pet);

        recordPetEvent(pet, PetEventType.TASK_COMPLETED, eventReason + " (+" + xp + " XP, +" + coins + " Monedas)");

        return pet;
    }

    private Pet getPetByUserId(Long userId) {
        return petRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
                    Pet newPet = Pet.builder()
                            .user(user)
                            .name("Gotchi de " + user.getUsername())
                            .build();
                    return petRepository.save(newPet);
                });
    }

    private void applyTimeDecay(Pet pet) {
        LocalDateTime now = LocalDateTime.now();
        long hoursPassed = Duration.between(pet.getLastUpdatedAt(), now).toHours();

        if (hoursPassed > 0) {
            int decay = (int) hoursPassed * 5; // Pierde 5 puntos por hora en hambre, ánimo y energía
            pet.setHunger(Math.max(0, pet.getHunger() - decay));
            pet.setMood(Math.max(0, pet.getMood() - decay));
            pet.setEnergy(Math.max(0, pet.getEnergy() - decay));

            if (pet.getHunger() < 20 || pet.getMood() < 20) {
                recordPetEvent(pet, PetEventType.SAD, "Tu mascota se siente desanimada o con hambre");
            }

            pet.setLastUpdatedAt(now);
        }
    }

    private void evaluateEvolution(Pet pet) {
        PetStage currentStage = pet.getStage();
        PetStage newStage = currentStage;

        if (pet.getXp() >= legendaryThreshold) {
            newStage = PetStage.LEGENDARY;
        } else if (pet.getXp() >= adultThreshold) {
            newStage = PetStage.ADULT;
        } else if (pet.getXp() >= youngThreshold) {
            newStage = PetStage.YOUNG;
        } else if (pet.getXp() >= babyThreshold) {
            newStage = PetStage.BABY;
        }

        if (newStage != currentStage) {
            pet.setStage(newStage);
            recordPetEvent(pet, PetEventType.EVOLVED, "¡Felicidades! Tu mascota ha evolucionado a " + newStage.name());
        }
    }

    private void recordPetEvent(Pet pet, PetEventType type, String description) {
        PetEvent event = PetEvent.builder()
                .pet(pet)
                .type(type)
                .description(description)
                .build();
        petEventRepository.save(event);
    }
}
