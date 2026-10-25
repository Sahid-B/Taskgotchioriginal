package ec.edu.espe.pa.taskgotchi.mapper;

import ec.edu.espe.pa.taskgotchi.dto.UserResponse;
import ec.edu.espe.pa.taskgotchi.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) return null;
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}
