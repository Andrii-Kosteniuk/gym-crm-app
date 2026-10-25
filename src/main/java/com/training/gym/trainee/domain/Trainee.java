package com.training.gym.trainee.domain;

import com.training.gym.user.domain.User;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
public class Trainee extends User {

    private LocalDate dateOfBirth;
    private Address address;

    @Override
    public String toString() {
        return """
                Trainee {
                %s,
                    dateOfBirth: %s,
                address: %s
                """.formatted(
                super.toString().indent(4).stripTrailing(),
                dateOfBirth,
                address
        );
    }

}
