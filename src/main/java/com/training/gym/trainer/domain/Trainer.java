package com.training.gym.trainer.domain;

import com.training.gym.training.domain.TrainingType;
import com.training.gym.user.domain.User;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
public class Trainer extends User {

    private TrainingType specialization;

    @Override
    public String toString() {
        return """
                Trainer {
                %s,
                specialization: %s
                """.formatted(
                super.toString().indent(4).stripTrailing(),
                specialization.getName()
        );
    }
}
