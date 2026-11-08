package ec.edu.espe.pa.taskgotchi.service;

import ec.edu.espe.pa.taskgotchi.dto.UpdateUserRequest;
import ec.edu.espe.pa.taskgotchi.dto.AvatarDto;
import ec.edu.espe.pa.taskgotchi.dto.UserResponse;

public interface UserService {
    UserResponse getCurrentUser();
    UserResponse updateAvatar(AvatarDto avatar);
    UserResponse updateCurrentUser(UpdateUserRequest request);
}
