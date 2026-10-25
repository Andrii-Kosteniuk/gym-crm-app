package com.training.gym.trainer.domain;

import com.training.gym.training.domain.TrainingType;
import com.training.gym.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Trainer extends User {

    private TrainingType specialization;

}
