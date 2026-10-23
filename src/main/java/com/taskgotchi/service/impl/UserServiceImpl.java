package com.taskgotchi.service.impl;

import com.taskgotchi.dto.UpdateUserRequest;
import com.taskgotchi.dto.UserResponse;
import com.taskgotchi.exception.BusinessRuleException;
import com.taskgotchi.exception.ResourceNotFoundException;
import com.taskgotchi.mapper.UserMapper;
import com.taskgotchi.model.User;
import com.taskgotchi.repository.UserRepository;
import com.taskgotchi.security.CurrentUserProvider;
import com.taskgotchi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        Long userId = currentUserProvider.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateCurrentUser(UpdateUserRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!user.getUsername().equals(request.username()) && userRepository.existsByUsername(request.username())) {
            throw new BusinessRuleException("El nombre de usuario ya está en uso");
        }
        if (!user.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new BusinessRuleException("El correo electrónico ya está en uso");
        }

        user.setUsername(request.username());
        user.setEmail(request.email());
        user = userRepository.save(user);

        return userMapper.toResponse(user);
    }
}
