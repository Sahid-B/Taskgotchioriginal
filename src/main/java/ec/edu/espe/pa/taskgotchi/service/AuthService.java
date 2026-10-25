package ec.edu.espe.pa.taskgotchi.service;

import ec.edu.espe.pa.taskgotchi.dto.AuthResponse;
import ec.edu.espe.pa.taskgotchi.dto.LoginRequest;
import ec.edu.espe.pa.taskgotchi.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
