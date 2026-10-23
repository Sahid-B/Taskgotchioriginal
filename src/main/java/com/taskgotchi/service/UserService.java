package com.taskgotchi.service;

import com.taskgotchi.dto.UpdateUserRequest;
import com.taskgotchi.dto.UserResponse;

public interface UserService {
    UserResponse getCurrentUser();
    UserResponse updateCurrentUser(UpdateUserRequest request);
}
