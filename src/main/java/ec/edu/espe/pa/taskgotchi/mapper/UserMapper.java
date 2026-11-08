package ec.edu.espe.pa.taskgotchi.mapper;

import ec.edu.espe.pa.taskgotchi.dto.UserResponse;
import ec.edu.espe.pa.taskgotchi.dto.AvatarDto;
import ec.edu.espe.pa.taskgotchi.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) return null;
        AvatarDto defaults = AvatarDto.defaults();
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt(),
                new AvatarDto(
                        user.getAvatarGender() == null ? defaults.gender() : user.getAvatarGender(),
                        user.getShirtColor() == null ? defaults.shirtColor() : user.getShirtColor(),
                        user.getPantsColor() == null ? defaults.pantsColor() : user.getPantsColor(),
                        user.getHairColor() == null ? defaults.hairColor() : user.getHairColor())
        );
    }
}
