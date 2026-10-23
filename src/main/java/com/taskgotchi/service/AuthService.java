package com.taskgotchi.service;

import com.taskgotchi.dto.AuthResponse;
import com.taskgotchi.dto.LoginRequest;
import com.taskgotchi.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
