package org.swarmcom.jsynapse.service.registration.password;

import org.springframework.stereotype.Component;
import org.swarmcom.jsynapse.dao.UserRepository;
import org.swarmcom.jsynapse.domain.Registration.*;
import org.swarmcom.jsynapse.domain.User;
import org.swarmcom.jsynapse.service.registration.RegistrationProvider;

import javax.inject.Inject;

import static org.swarmcom.jsynapse.service.registration.password.RegistrationPasswordInfo.PASSWORD;

@Component(PASSWORD)
public class PasswordRegistrationProvider implements RegistrationProvider {
    final static RegistrationInfo schema = new RegistrationPasswordInfo();
    private final UserRepository repository;

    @Inject
    public PasswordRegistrationProvider(final UserRepository repository) {
        // TODO create and inject UserService - access to user repository through it
        this.repository = repository;
    }

    @Override
    public RegistrationInfo getSchema() {
        return schema;
    }

    @Override
    public RegistrationResult register(RegistrationSubmission registration) {
        String userId = registration.get("user");
        String password = registration.get("password");
        // TODO create and inject Password encoder, use it to hash password
        // TODO verify if user name already exists, compose it with domain
        User user = new User(userId, password);
        repository.save(user);
        return new RegistrationResult(userId);
    }
}
