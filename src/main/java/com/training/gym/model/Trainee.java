package com.training.gym.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "trainees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trainee {

    @Id
    private Long id;

    @NotNull
    @Valid
    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "id", nullable = false, unique = true)
    private User user;

    @Past
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Embedded
    @Valid
    private Address address;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "trainee_trainers",
            joinColumns = @JoinColumn(name = "trainee_id"),
            inverseJoinColumns = @JoinColumn(name = "trainer_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_trainee_trainer",
                    columnNames = {"trainee_id", "trainer_id"}
            )
    )
    private Set<Trainer> trainers = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "trainee", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private Set<Training> trainings = new HashSet<>();


    public Set<Trainer> getTrainers() {
        return Collections.unmodifiableSet(trainers);
    }

    public Set<Training> getTrainings() {
        return Collections.unmodifiableSet(trainings);
    }

    public void assignTrainer(Trainer trainer) {
        Objects.requireNonNull(trainer);

        if (trainers.add(trainer)) {
            trainer.addTrainee(this);
        }
    }

    public void updateProfile(LocalDate dateOfBirth, Address address) {
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }

    public void removeTrainer(Trainer trainer) {
        Objects.requireNonNull(trainer);

        if (trainers.remove(trainer)) {
            trainer.removeTrainee(this);
        }
    }
}