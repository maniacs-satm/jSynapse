package org.swarmcom.jsynapse.service.authentication.recaptcha;

import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.swarmcom.jsynapse.dao.UserRepository;
import org.swarmcom.jsynapse.domain.Authentication.AuthenticationInfo;
import org.swarmcom.jsynapse.domain.Authentication.AuthenticationResult;
import org.swarmcom.jsynapse.domain.Authentication.AuthenticationSubmission;
import org.swarmcom.jsynapse.domain.User;
import org.swarmcom.jsynapse.service.exception.InvalidRequestException;
import org.swarmcom.jsynapse.service.authentication.AuthenticationProvider;
import org.swarmcom.jsynapse.service.user.UserService;
import org.swarmcom.jsynapse.service.user.UserUtils;

import javax.inject.Inject;

import static org.swarmcom.jsynapse.service.authentication.recaptcha.RecaptchaInfo.*;
import static java.lang.String.format;

@Component(RECAPTCHA_TYPE)
public class RecaptchaProvider implements AuthenticationProvider {
    final static AuthenticationInfo flow = new RecaptchaInfo();
    private static final Logger LOGGER = LoggerFactory.getLogger(RecaptchaProvider.class);

    private final UserService userService;
    private final UserUtils userUtils;

    private @Value("${recaptcha.private.key:null}") String recapthaPrivateKey;

    @Inject
    public RecaptchaProvider(final UserService userService, final UserUtils userUtils) {
        this.userService = userService;
        this.userUtils = userUtils;
    }

    @Override
    public AuthenticationInfo getFlow() {
        return flow;
    }

    @Override
    public AuthenticationResult register(AuthenticationSubmission registration) {
        validateRecaptcha(registration);
        // TODO throw register error if not a valid request
        User user = new User("user", "password");
        userService.createUser(user);
        return new AuthenticationResult("userid", userUtils.generateAccessToken());
    }

    @Override
    public AuthenticationResult login(AuthenticationSubmission login) {
        validateRecaptcha(login);
        return new AuthenticationResult("userid", userUtils.generateAccessToken());
    }

    public void validateRecaptcha(AuthenticationSubmission registration) {
        String remoteAddr = registration.getRemoteAddr();
        String challenge = registration.get(CHALLENGE);
        String response = registration.get(RESPONSE);
        ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
        reCaptcha.setPrivateKey(recapthaPrivateKey);
        ReCaptchaResponse reCaptchaResponse =
                reCaptcha.checkAnswer(remoteAddr, challenge, response);
        if (!reCaptchaResponse.isValid()) {
            LOGGER.error(format("Failed recaptcha for remote addr %s with errror %s", registration.getRemoteAddr(),
                    reCaptchaResponse.getErrorMessage()));
            throw new InvalidRequestException("Bad recaptcha");
        }
    }
}
