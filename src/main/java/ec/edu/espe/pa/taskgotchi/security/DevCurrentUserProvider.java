package ec.edu.espe.pa.taskgotchi.security;

import org.springframework.stereotype.Component;

@Component
public class DevCurrentUserProvider implements CurrentUserProvider {

    @Override
    public Long getCurrentUserId() {
        return 1L;
    }
}
