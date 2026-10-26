package com.training.gym.training.service;

import com.training.gym.common.credentials.generator.IdGenerator;
import com.training.gym.common.exception.EntityNotFoundException;
import com.training.gym.training.domain.Training;
import com.training.gym.training.dto.CreateTrainingDto;
import com.training.gym.training.mapper.TrainingMapper;
import com.training.gym.training.repository.TrainingRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@AllArgsConstructor
@Slf4j
public class TrainingServiceImpl implements TrainingService {

    private final TrainingRepository trainingRepository;
    private final IdGenerator idGenerator;
    private final TrainingMapper trainingMapper;


    @Override
    public Training createTraining(CreateTrainingDto trainingDto) {
        log.debug("Creating training");
        Objects.requireNonNull(trainingDto, "Create trainer DTO must not be null");

        Training training = trainingMapper.toTraining(trainingDto);

        training.setId(idGenerator.generate());

        Training savedTraining = trainingRepository.save(training);

        log.info("Training created successfully: training id ={}", training.getId());
        return savedTraining;
    }

    @Override
    public Training getTrainingById(Long id) {
        log.debug("Retrieving training: id={}", id);
        return trainingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Training with id %d not found".formatted(id)));
    }

}
