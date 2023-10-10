package jake.pin.service;

import jake.pin.repository.UserRepository;
import jake.pin.repository.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    public User getUserById(long id) {
        return repository.getUserById(id);
    }
}
