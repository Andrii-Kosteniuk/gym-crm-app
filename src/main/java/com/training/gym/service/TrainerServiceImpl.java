package com.training.gym.service;

import com.training.gym.dao.TrainerRepository;
import com.training.gym.dao.TrainingRepository;
import com.training.gym.dao.TrainingTypeRepository;
import com.training.gym.dao.UserRepository;
import com.training.gym.dto.auth.ChangePasswordDto;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.dto.trainer.CreateTrainerDto;
import com.training.gym.dto.trainer.TrainerTrainingCriteria;
import com.training.gym.dto.trainer.UpdateTrainerDto;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.mapper.TrainerMapper;
import com.training.gym.model.*;
import com.training.gym.util.PasswordGenerator;
import com.training.gym.util.UsernameGenerator;
import com.training.gym.validation.TrainerDtoValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class TrainerServiceImpl implements TrainerService {

    private final TrainerRepository trainerRepository;
    private final ProfileAuthorizationService authorizationService;
    private final TrainingRepository trainingRepository;
    private final CredentialService credentialService;
    private final TrainerMapper trainerMapper;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;
    private final TrainerDtoValidator trainerDtoValidator;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TrainingTypeRepository trainingTypeRepository;


    public TrainerServiceImpl(
            TrainerRepository trainerRepository,
            ProfileAuthorizationService authorizationService,
            TrainingRepository trainingRepository,
            CredentialService credentialService,
            TrainerMapper trainerMapper,
            UsernameGenerator usernameGenerator,
            PasswordGenerator passwordGenerator,
            TrainerDtoValidator trainerDtoValidator,
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            TrainingTypeRepository trainingTypeRepository
    ) {
        this.trainerRepository = trainerRepository;
        this.authorizationService = authorizationService;
        this.trainingRepository = trainingRepository;
        this.credentialService = credentialService;
        this.trainerMapper = trainerMapper;
        this.usernameGenerator = usernameGenerator;
        this.passwordGenerator = passwordGenerator;
        this.trainerDtoValidator = trainerDtoValidator;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Override
    @Transactional
    public Trainer createTrainer(CreateTrainerDto trainerDto) {
        log.debug("Creating trainer profile");

        trainerDtoValidator.validateCreate(trainerDto);

        String uniqUserName = usernameGenerator.generate(
                trainerDto.firstName(),
                trainerDto.lastName(),
                userRepository::existsByUsername);

        String password = passwordGenerator.generate();

        TrainingType specialization = trainingTypeRepository
                .findById(trainerDto.specializationId())
                .orElseThrow(() -> new EntityNotFoundException("Training type not found: " + trainerDto.specializationId()));


        User user = User.builder()
                .firstName(trainerDto.firstName())
                .lastName(trainerDto.lastName())
                .userName(uniqUserName)
                .password(passwordEncoder.encode(password))
                .active(true)
                .build();

        Trainer trainer = trainerMapper.toTrainer(trainerDto, user, specialization);

        Trainer savedTrainer = trainerRepository.save(trainer);

        log.info("Trainer profile created successfully: username={}", uniqUserName);
        return savedTrainer;
    }

    @Override
    @Transactional
    public Trainer updateTrainer(Credentials credentials, UpdateTrainerDto dto) {
        trainerDtoValidator.validateUpdate(dto);
        log.debug("Updating trainer profile: username={}", dto.userName());
        Trainer trainer = authorizationService.requireTrainerOwner(credentials, dto.userName());

        trainerMapper.updateTrainer(dto, trainer);

        log.info("Trainer profile updated successfully: username={}", dto.userName());

        return trainer;
    }

    @Override
    public Trainer getTrainerById(Credentials credentials, Long id) {
        log.debug("Retrieving trainer profile: id={}", id);
        authorizationService.requireTrainer(credentials);

        return trainerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainer with id %d not found".formatted(id)));
    }

    @Override
    public Trainer findByUsername(Credentials credentials, String username) {

        authorizationService.requireTrainer(credentials);

        return trainerRepository.findByUserName(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer with username %s not found".formatted(username)));
    }

    @Override
    @Transactional
    public void removeTrainerByUsername(Credentials credentials, String username) {
        log.debug("Removing trainer profile");

        Trainer trainer = authorizationService.requireTrainerOwner(credentials, username);

        for (Trainee trainee : new HashSet<>(trainer.getTrainees())) {
            trainee.removeTrainer(trainer);
        }
        trainerRepository.delete(trainer);
        log.info("Trainee profile removed successfully: username={}", username);

    }


    @Transactional
    public void activateTrainer(Credentials credentials, Trainer trainer) {
        Objects.requireNonNull(trainer, "Trainer is required");

        authorizationService.requireTrainer(credentials);

        Trainer managedTrainer = trainerRepository.findById(trainer.getId())
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + trainer.getId()));

        if (Boolean.TRUE == managedTrainer.getUser().getActive())
            throw new IllegalStateException("Trainer is already active.");

        trainerRepository.activateTrainer(managedTrainer);
        log.info("Trainer profile has been activated");
    }

    @Override
    @Transactional
    public void deactivateTrainer(Credentials credentials, Trainer trainer) {
        Objects.requireNonNull(trainer, "Trainer is required");

        authorizationService.requireTrainer(credentials);

        Trainer managedTrainer = trainerRepository.findById(trainer.getId())
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + trainer.getId()));

        if (Boolean.FALSE == managedTrainer.getUser().getActive())
            throw new IllegalStateException("Trainer is already deactivated.");

        trainerRepository.deactivateTrainer(managedTrainer);
        log.info("Trainer profile has been deactivated");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> findTrainings(Credentials credentials, String username, TrainerTrainingCriteria criteria) {
        Objects.requireNonNull(criteria, "Criteria must not be null");
        log.debug("Finding training by specified criteria");
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Trainer username must not be blank");
        }

        authorizationService.requireTrainerOwner(credentials, username);

        return trainingRepository.findByTrainerUsername(username, criteria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainer> getUnassignedTrainers(Credentials credentials, String username) {
        Objects.requireNonNull(username, "Trainee username must not be null");

        authorizationService.requireTraineeOwner(credentials, username);

        return trainerRepository.findUnassignedTrainers(username);
    }


    @Override
    @Transactional
    public void changePassword(Credentials credentials, ChangePasswordDto dto) {
        authorizationService.requireTrainer(credentials);
        credentialService.changePassword(credentials, dto);
    }
}
