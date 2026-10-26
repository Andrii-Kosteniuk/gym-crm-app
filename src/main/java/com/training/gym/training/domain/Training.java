package com.training.gym.training.domain;

import lombok.*;

import java.time.Duration;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class Training {

    private Long id;
    private Long traineeId;
    private Long trainerId;
    private String name;
    private TrainingType type;
    private LocalDate date;
    private Duration duration;

    @Override
    public String toString() {
        return """
                Training {
                %d,
                %d,
                %d,
                    name: %s,
                    type: %s
                """.formatted(
                id,
                traineeId,
                trainerId,
                name,
                type.getName()
        );
    }
}


