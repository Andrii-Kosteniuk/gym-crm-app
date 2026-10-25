package com.training.gym.trainee.domain;

import com.training.gym.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Trainee extends User {

    private LocalDate dateOfBirth;
    private Address address;
}
