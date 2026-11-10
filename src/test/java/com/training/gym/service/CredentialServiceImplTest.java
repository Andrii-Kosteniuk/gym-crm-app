package com.training.gym.service;

import com.training.gym.dao.UserRepository;
import com.training.gym.dto.auth.ChangePasswordDto;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.exception.AuthenticationFailedException;
import com.training.gym.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CredentialServiceImplTest {
    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks CredentialServiceImpl service;

    private Credentials credentials;
    private User user;

    @BeforeEach
    void setUp() {
        credentials = new Credentials("james.bond", "oldPassword");
        user = User.builder().userName("james.bond").password("storedHash").active(true).build();
    }

    @Test
    void authenticatesValidCredentials() {
        when(userRepository.findByUsername("james.bond")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "storedHash")).thenReturn(true);

        assertSame(user, service.authenticate(credentials));
        verify(userRepository).findByUsername("james.bond");
    }

    @Test
    void rejectsNullCredentials() {
        assertThrows(AuthenticationFailedException.class, () -> service.authenticate(null));
        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void rejectsMissingOrBlankUsername() {
        for (String username : new String[]{"", "  "}) {
            assertThrows(AuthenticationFailedException.class,
                    () -> service.authenticate(new Credentials(username, "password")));
        }
        assertThrows(AuthenticationFailedException.class,
                () -> service.authenticate(new Credentials(null, "password")));
        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void rejectsMissingOrBlankPassword() {
        for (String password : new String[]{"", "  "}) {
            assertThrows(AuthenticationFailedException.class,
                    () -> service.authenticate(new Credentials("james.bond", password)));
        }
        assertThrows(AuthenticationFailedException.class,
                () -> service.authenticate(new Credentials("james.bond", null)));
        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void rejectsUnknownUser() {
        when(userRepository.findByUsername("james.bond")).thenReturn(Optional.empty());
        assertThrows(AuthenticationFailedException.class, () -> service.authenticate(credentials));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void rejectsWrongPassword() {
        when(userRepository.findByUsername("james.bond")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "storedHash")).thenReturn(false);
        assertThrows(AuthenticationFailedException.class, () -> service.authenticate(credentials));
    }

    @Test
    void changesPasswordAfterReauthentication() {
        ChangePasswordDto dto = new ChangePasswordDto("oldPassword", "newPassword");
        when(userRepository.findByUsername("james.bond")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "storedHash")).thenReturn(true);
        when(passwordEncoder.matches("newPassword", "storedHash")).thenReturn(false);
        when(passwordEncoder.encode("newPassword")).thenReturn("newHash");

        service.changePassword(credentials, dto);

        assertEquals("newHash", user.getPassword());
        verify(passwordEncoder).encode("newPassword");
    }

    @Test
    void rejectsNullPasswordChangeDto() {
        assertThrows(NullPointerException.class, () -> service.changePassword(credentials, null));
        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void rejectsInvalidPasswordChangeDtoBeforeAuthentication() {
        ChangePasswordDto[] invalid = {
                new ChangePasswordDto(null, "newPassword"),
                new ChangePasswordDto(" ", "newPassword"),
                new ChangePasswordDto("oldPassword", null),
                new ChangePasswordDto("oldPassword", "short")
        };
        for (ChangePasswordDto dto : invalid) {
            assertThrows(IllegalArgumentException.class,
                    () -> service.changePassword(credentials, dto));
        }
        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void rejectsIncorrectCurrentPasswordWithoutChangingHash() {
        ChangePasswordDto dto = new ChangePasswordDto("wrongPassword", "newPassword");
        when(userRepository.findByUsername("james.bond")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "storedHash")).thenReturn(true);

        assertThrows(AuthenticationFailedException.class,
                () -> service.changePassword(credentials, dto));
        assertEquals("storedHash", user.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void rejectsReuseOfCurrentPassword() {
        ChangePasswordDto dto = new ChangePasswordDto("oldPassword", "newPassword");
        when(userRepository.findByUsername("james.bond")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "storedHash")).thenReturn(true);
        when(passwordEncoder.matches("newPassword", "storedHash")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.changePassword(credentials, dto));
        assertEquals("storedHash", user.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
    }
}
