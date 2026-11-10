package com.training.gym.service;

import com.training.gym.dao.TraineeRepository;
import com.training.gym.dao.TrainingRepository;
import com.training.gym.dao.TrainingTypeRepository;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.dto.training.CreateTrainingDto;
import com.training.gym.exception.AccessDeniedException;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.mapper.TrainingMapper;
import com.training.gym.model.Trainee;
import com.training.gym.model.Trainer;
import com.training.gym.model.Training;
import com.training.gym.model.TrainingType;
import com.training.gym.model.User;
import com.training.gym.validation.TrainingDtoValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Slf4j
public class TrainingServiceImpl implements TrainingService {

    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final TrainingMapper trainingMapper;
    private final TrainingDtoValidator trainingValidator;
    private final ProfileAuthorizationService authorizationService;
    private final CredentialService credentialService;

    public TrainingServiceImpl(TrainingRepository trainingRepository,
                               TraineeRepository traineeRepository,
                               TrainingTypeRepository trainingTypeRepository,
                               TrainingMapper trainingMapper,
                               TrainingDtoValidator trainingValidator,
                               ProfileAuthorizationService authorizationService,
                               CredentialService credentialService) {
        this.trainingRepository = trainingRepository;
        this.traineeRepository = traineeRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.trainingMapper = trainingMapper;
        this.trainingValidator = trainingValidator;
        this.authorizationService = authorizationService;
        this.credentialService = credentialService;
    }

    @Override
    @Transactional
    public Training createTraining(Credentials credentials, CreateTrainingDto dto) {
        log.debug("Creating training");

        Trainer trainer = authorizationService.requireTrainer(credentials);

        trainingValidator.validateCreate(dto);

        if (!Objects.equals(trainer.getId(), dto.trainerId())) {
            throw new AccessDeniedException("Trainers can only create trainings for themselves");
        }

        Trainee trainee = traineeRepository.findById(dto.traineeId())
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + dto.traineeId()));

        TrainingType type = trainingTypeRepository.findById(dto.trainingTypeId())
                .orElseThrow(() -> new EntityNotFoundException("Training type not found: " + dto.trainingTypeId()));

        Training training = trainingMapper.toTraining(dto, trainee, trainer, type);

        Training savedTraining = trainingRepository.save(training);

        log.info("Training created: id={}", savedTraining.getId());
        return savedTraining;
    }

    @Override
    @Transactional(readOnly = true)
    public Training getTrainingById(Credentials credentials, Long id) {
        log.debug("Retrieving training: id={}", id);

        User user = credentialService.authenticate(credentials);

        Training training = trainingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Training not found: " + id));

        String username = user.getUserName();
        boolean participant = username.equals(training.getTrainee().getUser().getUserName())
                || username.equals(training.getTrainer().getUser().getUserName());

        if (!participant) {
            throw notFound(id);
        }
        return training;
    }

    private EntityNotFoundException notFound(Long id) {
        return new EntityNotFoundException("Training with id %d not found".formatted(id));
    }
}