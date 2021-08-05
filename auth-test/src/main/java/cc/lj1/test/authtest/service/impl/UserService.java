package cc.lj1.test.authtest.service.impl;

import cc.lj1.auth.AuthenticatableUser;
import cc.lj1.auth.services.AuthUserService;
import cc.lj1.test.authtest.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserService implements AuthUserService {

    @Override
    public AuthenticatableUser getUserById(String id) {
        User u = new User();
        u.setId(id);
        u.setName("Hello");
        return u;
    }
}
