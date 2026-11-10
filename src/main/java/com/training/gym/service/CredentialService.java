package com.training.gym.service;

import com.training.gym.dto.auth.ChangePasswordDto;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.model.User;

public interface CredentialService {

    User authenticate(Credentials credentials);

    void changePassword(Credentials credentials, ChangePasswordDto dto
    );
}
