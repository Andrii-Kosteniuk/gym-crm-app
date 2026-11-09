package com.training.gym.service;

import com.training.gym.dao.UserRepository;
import com.training.gym.dto.auth.ChangePasswordDto;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.exception.AuthenticationFailedException;
import com.training.gym.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialServiceImpl implements CredentialService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User authenticate(Credentials credentials) {
        if (credentials == null
                || credentials.username() == null
                || credentials.username().isBlank()
                || credentials.password() == null
                || credentials.password().isBlank()) {
            throw new AuthenticationFailedException();
        }

        User user = userRepository
                .findByUsername(credentials.username())
                .orElseThrow(AuthenticationFailedException::new);

        if (!passwordEncoder.matches(credentials.password(), user.getPassword())) {
            throw new AuthenticationFailedException();
        }

        return user;
    }

    @Override
    @Transactional
    public void changePassword(Credentials credentials, ChangePasswordDto dto) {
        Objects.requireNonNull(dto, "Password change DTO is required");

        if (dto.currentPassword() == null
                || dto.currentPassword().isBlank()
                || dto.newPassword() == null
                || dto.newPassword().length() < 6) {
            throw new IllegalArgumentException("Invalid password change request");
        }

        User user = authenticate(credentials);

        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            throw new AuthenticationFailedException();
        }

        if (passwordEncoder.matches(dto.newPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password must differ from current password");
        }

        user.setPassword(passwordEncoder.encode(dto.newPassword()));
    }
}
