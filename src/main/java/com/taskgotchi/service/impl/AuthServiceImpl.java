package com.taskgotchi.service.impl;

import com.taskgotchi.dto.AuthResponse;
import com.taskgotchi.dto.LoginRequest;
import com.taskgotchi.dto.RegisterRequest;
import com.taskgotchi.exception.BusinessRuleException;
import com.taskgotchi.mapper.UserMapper;
import com.taskgotchi.model.Pet;
import com.taskgotchi.model.User;
import com.taskgotchi.repository.PetRepository;
import com.taskgotchi.repository.UserRepository;
import com.taskgotchi.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessRuleException("El nombre de usuario ya está registrado");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessRuleException("El correo electrónico ya está registrado");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();

        user = userRepository.save(user);

        Pet pet = Pet.builder()
                .user(user)
                .name("Gotchi de " + user.getUsername())
                .build();
        petRepository.save(pet);

        String placeholderToken = "TODO_JWT_TOKEN_FOR_USER_" + user.getId();

        return new AuthResponse(placeholderToken, userMapper.toResponse(user));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .or(() -> userRepository.findByEmail(request.username()))
                .orElseThrow(() -> new BusinessRuleException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessRuleException("Credenciales inválidas");
        }

        String placeholderToken = "TODO_JWT_TOKEN_FOR_USER_" + user.getId();

        return new AuthResponse(placeholderToken, userMapper.toResponse(user));
    }
}
