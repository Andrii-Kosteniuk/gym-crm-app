package com.training.gym.training.mapper;

import com.training.gym.training.domain.Training;
import com.training.gym.training.dto.CreateTrainingDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainingMapper {

    @Mapping(target = "id", ignore = true)
    Training toTraining(CreateTrainingDto dto);
}
