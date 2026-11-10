package com.training.gym.mapper;

import com.training.gym.model.Trainer;
import com.training.gym.dto.trainer.CreateTrainerDto;
import com.training.gym.dto.trainer.UpdateTrainerDto;
import com.training.gym.model.TrainingType;
import com.training.gym.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TrainerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "specialization", source = "specialization")
    @Mapping(target = "trainees", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    Trainer toTrainer( CreateTrainerDto dto,User user,TrainingType specialization);

    void updateTrainer(UpdateTrainerDto dto, @MappingTarget Trainer trainer);
}
