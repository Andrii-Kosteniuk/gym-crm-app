package com.training.gym.service;

import com.training.gym.dao.TraineeRepository;
import com.training.gym.dao.TrainerRepository;
import com.training.gym.dao.TrainingRepository;
import com.training.gym.dao.TrainingTypeRepository;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.dto.training.CreateTrainingDto;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.mapper.TrainingMapper;
import com.training.gym.model.Trainee;
import com.training.gym.model.Trainer;
import com.training.gym.model.Training;
import com.training.gym.model.TrainingType;
import com.training.gym.validation.TrainingDtoValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
public class TrainingServiceImpl implements TrainingService {

    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingMapper trainingMapper;
    private final TrainingTypeRepository trainingTypeRepository;
    private final TrainingDtoValidator trainingValidator;
    private final ProfileAuthorizationService authorizationService;

    public TrainingServiceImpl(TrainingRepository trainingRepository,
                               TraineeRepository traineeRepository,
                               TrainerRepository trainerRepository,
                               TrainingMapper trainingMapper,
                               TrainingTypeRepository trainingTypeRepository,
                               TrainingDtoValidator trainingValidator,
                               ProfileAuthorizationService authorizationService) {
        this.trainingRepository = trainingRepository;
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.trainingMapper = trainingMapper;
        this.trainingTypeRepository = trainingTypeRepository;
        this.trainingValidator = trainingValidator;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional
    public Training createTraining(Credentials credentials, CreateTrainingDto dto) {
        log.debug("Creating training");

        authorizationService.requireTrainer(credentials);

        trainingValidator.validateCreate(dto);

        Trainee trainee = traineeRepository.findById(dto.traineeId())
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + dto.traineeId()));

        Trainer trainer = trainerRepository.findById(dto.trainerId())
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + dto.trainerId()));

        TrainingType type = trainingTypeRepository
                .findById(dto.trainingTypeId())
                .orElseThrow(() -> new EntityNotFoundException("Training type not found"));

        Training training = trainingMapper.toTraining(dto, trainee, trainer, type);



        Training savedTraining = trainingRepository.save(training);

        log.info("Training created: id={}", savedTraining.getId());

        return savedTraining;
    }

    @Override
    @Transactional(readOnly = true)
    public Training getTrainingById(Long id) {
        log.debug("Retrieving training: id={}", id);
        return trainingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Training with id %d not found".formatted(id)));
    }

}
