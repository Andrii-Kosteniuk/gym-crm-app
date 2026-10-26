package com.training.gym.trainer.mapper;

import com.training.gym.trainee.domain.Trainee;
import com.training.gym.trainee.dto.CreateTraineeDto;
import com.training.gym.trainee.dto.UpdateTraineeDto;
import com.training.gym.trainer.domain.Trainer;
import com.training.gym.trainer.dto.CreateTrainerDto;
import com.training.gym.trainer.dto.UpdateTrainerDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TrainerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "active", ignore = true)
    Trainer toTrainer(CreateTrainerDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateTrainer(UpdateTrainerDto dto, @MappingTarget Trainer trainer);
}
