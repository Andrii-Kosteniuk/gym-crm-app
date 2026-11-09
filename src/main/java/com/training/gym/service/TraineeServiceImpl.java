package com.training.gym.service;

import com.training.gym.dao.TraineeRepository;
import com.training.gym.dao.TrainerRepository;
import com.training.gym.dao.UserRepository;
import com.training.gym.dto.auth.ChangePasswordDto;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.dto.trainee.CreateTraineeDto;
import com.training.gym.dto.trainee.TraineeTrainingCriteria;
import com.training.gym.dto.trainee.UpdateTraineeDto;
import com.training.gym.dto.trainee.UpdateTraineeTrainersDto;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.mapper.TraineeMapper;
import com.training.gym.model.Trainee;
import com.training.gym.model.Trainer;
import com.training.gym.model.Training;
import com.training.gym.model.User;
import com.training.gym.util.PasswordGenerator;
import com.training.gym.util.UsernameGenerator;
import com.training.gym.validation.TraineeDtoValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Slf4j
public class TraineeServiceImpl implements TraineeService {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final TraineeMapper traineeMapper;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;
    private final TraineeDtoValidator traineeValidator;
    private final PasswordEncoder passwordEncoder;
    private final ProfileAuthorizationService authorizationService;
    private final CredentialService credentialService;

    public TraineeServiceImpl(TraineeRepository traineeRepository, TrainerRepository trainerRepository,
                              UserRepository userRepository,
                              TraineeMapper traineeMapper,
                              UsernameGenerator usernameGenerator,
                              PasswordGenerator passwordGenerator,
                              TraineeDtoValidator traineeValidator,
                              PasswordEncoder passwordEncoder,
                              ProfileAuthorizationService authorizationService, CredentialService credentialService) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.userRepository = userRepository;
        this.traineeMapper = traineeMapper;
        this.usernameGenerator = usernameGenerator;
        this.passwordGenerator = passwordGenerator;
        this.traineeValidator = traineeValidator;
        this.passwordEncoder = passwordEncoder;
        this.authorizationService = authorizationService;
        this.credentialService = credentialService;
    }

    @Override
    @Transactional
    public Trainee createTrainee(CreateTraineeDto traineeDto) {
        log.debug("Creating trainee profile");

        traineeValidator.validateCreate(traineeDto);
        String username = usernameGenerator.generate(traineeDto.firstName(), traineeDto.lastName(), userRepository::existsByUsername);
        String password = passwordGenerator.generate();

        User user = User.builder()
                .firstName(traineeDto.firstName())
                .lastName(traineeDto.lastName())
                .userName(username)
                .password(passwordEncoder.encode(password))
                .active(true)
                .build();

        Trainee trainee = traineeMapper.toTrainee(traineeDto, user);

        Trainee savedTrainee = traineeRepository.save(trainee);

        log.info("Trainee profile created successfully: username={}", username);
        return savedTrainee;

    }

    @Override
    @Transactional
    public Trainee updateTrainee(Credentials credentials, UpdateTraineeDto traineeDto) {

        traineeValidator.validateUpdate(traineeDto);

        String userName = traineeDto.userName();

        log.debug("Updating trainee profile: username={}", userName);

        Trainee trainee = authorizationService.requireTraineeOwner(
                credentials,
                traineeDto.userName()
        );

        User user = trainee.getUser();

        user.setFirstName(traineeDto.firstName());
        user.setLastName(traineeDto.lastName());

        trainee.updateProfile(traineeDto.dateOfBirth(), traineeDto.address());

        log.info("Trainee updated: username={}", userName);

        return trainee;

    }

    @Override
    public Trainee getTraineeById(Credentials credentials, Long id) {
        log.debug("Retrieving trainee profile: id={}", id);

        authorizationService.requireTrainee(credentials);
        return traineeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainee with id %d not found".formatted(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public Trainee findByUserName(Credentials credentials, String username) {

        authorizationService.requireTrainee(credentials);

        return traineeRepository.findByUserName(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee with name %s not found".formatted(username)));
    }


    @Override
    @Transactional
    public void removeTraineeByUsername(Credentials credentials, String username) {
        log.debug("Removing trainee profile");

        Trainee trainee = authorizationService.requireTraineeOwner(credentials, username);

        for (Trainer trainer : new HashSet<>(trainee.getTrainers())) {
            trainee.removeTrainer(trainer);
        }
        traineeRepository.delete(trainee);
        log.info("Trainee profile removed successfully: username={}", username);
    }


    @Override
    @Transactional
    public void activateTrainee(Credentials credentials, Trainee trainee) {
        Objects.requireNonNull(trainee, "Trainee is required");

        authorizationService.requireTrainee(credentials);

        Trainee managed = traineeRepository.findById(trainee.getId())
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + trainee.getId()));

        managed.getUser().activate();
    }

    @Override
    @Transactional
    public void deactivateTrainee(Credentials credentials, Trainee trainee) {
        Objects.requireNonNull(trainee, "Trainee is required");

        authorizationService.requireTrainee(credentials);

        Trainee managed = traineeRepository.findById(trainee.getId())
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + trainee.getId()));

        managed.getUser().deactivate();
    }

    @Override
    public List<Training> findTrainings(Credentials credentials, String username, TraineeTrainingCriteria criteria) {

        authorizationService.requireTrainee(credentials);
        return traineeRepository.findTrainings(username, criteria);
    }

    @Override
    @Transactional
    public void changePassword(Credentials credentials, ChangePasswordDto dto) {
        authorizationService.requireTrainee(credentials);
        credentialService.changePassword(credentials, dto);
    }

    @Override
    @Transactional
    public Set<Trainer> replaceTrainers(Credentials credentials, UpdateTraineeTrainersDto dto) {
        Objects.requireNonNull(dto, "Update DTO is required");

        if (dto.traineeUsername() == null
                || dto.traineeUsername().isBlank()
                || dto.trainerIds() == null
                || dto.trainerIds().stream()
                .anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException("Invalid trainer assignment request");
        }

        Trainee trainee = authorizationService.requireTraineeOwner(credentials, dto.traineeUsername());

        Set<Trainer> trainers = new HashSet<>();

        for (Long trainerId : dto.trainerIds()) {
            Trainer trainer = trainerRepository.findById(trainerId)
                    .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + trainerId));

            trainers.add(trainer);
        }

        Set<Trainer> currentTrainers = new HashSet<>(trainee.getTrainers());

        for (Trainer trainer : currentTrainers) {
            if (!trainers.contains(trainer)) {
                trainee.removeTrainer(trainer);
            }
        }

        for (Trainer trainer : trainers) {
            if (!currentTrainers.contains(trainer)) {
                trainee.assignTrainer(trainer);
            }
        }

        log.info("Updated trainer assignments for trainee={}", dto.traineeUsername());

        return Set.copyOf(trainee.getTrainers());
    }

}
