package com.training.gym.trainer.service;

import com.training.gym.common.credentials.generator.IdGenerator;
import com.training.gym.common.credentials.generator.PasswordGenerator;
import com.training.gym.common.credentials.generator.UsernameGenerator;
import com.training.gym.common.exception.EntityNotFoundException;
import com.training.gym.trainee.repository.TraineeRepository;
import com.training.gym.trainer.domain.Trainer;
import com.training.gym.trainer.dto.CreateTrainerDto;
import com.training.gym.trainer.dto.UpdateTrainerDto;
import com.training.gym.trainer.mapper.TrainerMapper;
import com.training.gym.trainer.repository.TrainerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class TrainerServiceImpl implements TrainerService {

    private final TrainerRepository trainerRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerMapper trainerMapper;
    private final IdGenerator idGenerator;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;

    public TrainerServiceImpl(
            TrainerRepository trainerRepository,
            TraineeRepository traineeRepository,
            TrainerMapper trainerMapper,
            @Qualifier("trainerIdGenerator") IdGenerator idGenerator,
            UsernameGenerator usernameGenerator,
            PasswordGenerator passwordGenerator
    ) {
        this.trainerRepository = trainerRepository;
        this.traineeRepository = traineeRepository;
        this.trainerMapper = trainerMapper;
        this.idGenerator = idGenerator;
        this.usernameGenerator = usernameGenerator;
        this.passwordGenerator = passwordGenerator;
    }

    @Override
    public Trainer createTrainer(CreateTrainerDto trainerDto) {
        log.debug("Creating trainer profile");
        Objects.requireNonNull(trainerDto, "Create trainer DTO must not be null");

        Trainer trainer = trainerMapper.toTrainer(trainerDto);

        trainer.setId(idGenerator.generate());
        trainer.setPassword(passwordGenerator.generate());

        String uniqUserName = usernameGenerator.generate(trainerDto.firstName(), trainerDto.lastName(), this::usernameExists);
        trainer.setUserName(uniqUserName);
        trainer.setActive(true);

        Trainer savedTrainer = trainerRepository.save(trainer);

        log.info("Trainer profile created successfully: username={}", uniqUserName);
        return savedTrainer;
    }

    @Override
    public Trainer updateTrainer(UpdateTrainerDto trainerDto) {
        Objects.requireNonNull(trainerDto, "Update trainer DTO must not be null");
        String username = trainerDto.userName();

        log.debug("Updating trainer profile: username={}", username);

        Trainer existingTrainer = trainerRepository
                .findByUserName(username)
                .orElseThrow(() -> {
                    log.warn("Trainer update failed: username={} not found", username);
                    return new EntityNotFoundException("Trainer with username %s not found".formatted(trainerDto.userName()));
                });

        trainerMapper.updateTrainer(trainerDto, existingTrainer);

        Trainer updatedTrainer = trainerRepository.save(existingTrainer);

        log.info("Trainer profile updated successfully: username={}", username);
        return updatedTrainer;
    }

    @Override
    public Trainer getTrainerById(Long id) {
        log.debug("Retrieving trainer profile: id={}", id);
        return trainerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainer with id %d not found".formatted(id)));
    }

    private boolean usernameExists(String username) {
        return traineeRepository.existsByUserName(username)
                || trainerRepository.existsByUserName(username);
    }
}
