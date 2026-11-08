package ec.edu.espe.pa.taskgotchi.service.impl;

import ec.edu.espe.pa.taskgotchi.aspect.Auditable;
import ec.edu.espe.pa.taskgotchi.dto.PageResponse;
import ec.edu.espe.pa.taskgotchi.dto.PetEventResponse;
import ec.edu.espe.pa.taskgotchi.dto.PetResponse;
import ec.edu.espe.pa.taskgotchi.dto.BonkResponse;
import ec.edu.espe.pa.taskgotchi.dto.CaressResponse;
import ec.edu.espe.pa.taskgotchi.dto.MinigameRequest;
import ec.edu.espe.pa.taskgotchi.dto.MinigameResultResponse;
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
import java.time.LocalDate;
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
    @Auditable("PET_FED")
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
    @Auditable("PET_PLAYED")
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

    static final int CARESS_MOOD = 5;
    static final int CARESS_COOLDOWN_SECONDS = 20;
    static final int BONK_MOOD = 5;
    static final int BONK_COOLDOWN_SECONDS = 10;

    @Override
    @Transactional
    public BonkResponse bonkPet() {
        Pet pet = getPetByUserId(currentUserProvider.getCurrentUserId());
        applyTimeDecay(pet);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last = pet.getLastBonkAt();
        int lost = 0;
        if (last == null || Duration.between(last, now).getSeconds() >= BONK_COOLDOWN_SECONDS) {
            int before = pet.getMood();
            pet.setMood(Math.max(0, before - BONK_MOOD));
            lost = before - pet.getMood();
            pet.setLastBonkAt(now);
            pet.setLastUpdatedAt(now);
            pet = petRepository.save(pet);
            if (lost > 0) {
                recordPetEvent(pet, PetEventType.SAD, "Tu mascota recibió golpecitos y se puso triste (-" + lost + " Ánimo)");
            }
        }
        return new BonkResponse(petMapper.toResponse(pet), lost);
    }

    @Override
    @Transactional
    public CaressResponse caressPet() {
        Pet pet = getPetByUserId(currentUserProvider.getCurrentUserId());
        applyTimeDecay(pet);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last = pet.getLastCaressAt();
        long elapsed = last == null ? Long.MAX_VALUE : Duration.between(last, now).getSeconds();
        int gained = 0;
        if (elapsed >= CARESS_COOLDOWN_SECONDS) {
            int before = pet.getMood();
            pet.setMood(Math.min(100, before + CARESS_MOOD));
            gained = pet.getMood() - before;
            pet.setLastCaressAt(now);
            pet.setLastUpdatedAt(now);
            elapsed = 0;
        }
        pet = petRepository.save(pet);
        int wait = (int) Math.max(0, CARESS_COOLDOWN_SECONDS - elapsed);
        return new CaressResponse(petMapper.toResponse(pet), gained, wait);
    }

    @Override
    @Transactional
    @Auditable("PET_MINIGAME")
    public MinigameResultResponse playMinigame(MinigameRequest request) {
        Pet pet = getPetByUserId(currentUserProvider.getCurrentUserId());
        applyTimeDecay(pet);
        if (pet.getEnergy() < 15) {
            throw new BusinessRuleException("Tu Gotchi está demasiado cansado para jugar. Déjalo descansar.");
        }

        pet.setEnergy(pet.getEnergy() - 15);
        pet.setMood(Math.min(100, pet.getMood() + 25));
        pet.setHunger(Math.max(0, pet.getHunger() - 10));

        LocalDate today = LocalDate.now();
        int playsToday = today.equals(pet.getMinigameDay()) && pet.getMinigamePlaysToday() != null
                ? pet.getMinigamePlaysToday() : 0;
        int xp = playsToday < 8 ? Math.round(request.score() * 0.15f) : 0;
        int coins = playsToday < 8
                ? (request.score() >= 90 ? 3 : request.score() >= 70 ? 2 : request.score() >= 40 ? 1 : 0) : 0;
        pet.setMinigameDay(today);
        pet.setMinigamePlaysToday(playsToday + 1);
        pet.setXp(pet.getXp() + xp);
        pet.setCoins(pet.getCoins() + coins);
        evaluateEvolution(pet);
        pet.setLastUpdatedAt(LocalDateTime.now());
        pet = petRepository.save(pet);

        String gameName = switch (request.game()) {
            case "CATCH" -> "Lluvia de frutas";
            case "MEMORY" -> "Memoria del bosque";
            case "ECHO" -> "Eco del Gotchi";
            default -> throw new IllegalArgumentException("Minijuego desconocido");
        };
        recordPetEvent(pet, PetEventType.PLAYED, "Minijuego " + gameName + ": " + request.score()
                + " puntos (+" + xp + " XP, +" + coins + " Monedas)");
        return new MinigameResultResponse(petMapper.toResponse(pet), xp, coins, Math.max(0, 8 - playsToday - 1));
    }

    @Override
    @Transactional
    @Auditable("PET_RESTED")
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
    @Auditable("REWARD_GRANTED")
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

    @Override
    @Transactional
    public Pet getPetWithTimeDecay(Long userId) {
        Pet pet = getPetByUserId(userId);
        applyTimeDecay(pet);
        return petRepository.save(pet);
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
