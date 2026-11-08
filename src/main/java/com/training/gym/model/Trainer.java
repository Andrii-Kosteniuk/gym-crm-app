package com.training.gym.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "trainers")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Trainer {

    @Id
    private Long id;

    @NotNull
    @Valid
    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "specialization_id", nullable = false)
    private TrainingType specialization;

    @ManyToMany(mappedBy = "trainers", fetch = FetchType.LAZY)
    private Set<Trainee> trainees = new HashSet<>();

    @OneToMany(mappedBy = "trainer", fetch = FetchType.LAZY)
    private Set<Training> trainings = new HashSet<>();


    public Set<Trainee> getTrainees() {
        return Collections.unmodifiableSet(trainees);
    }

    public Set<Training> getTrainings() {
        return Collections.unmodifiableSet(trainings);
    }

    void addTrainee(Trainee trainee) {
        trainees.add(trainee);
    }

    void removeTrainee(Trainee trainee) {
        trainees.remove(trainee);
    }
}