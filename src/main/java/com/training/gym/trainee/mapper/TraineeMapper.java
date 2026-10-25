package com.training.gym.trainee.mapper;

import com.training.gym.trainee.domain.Trainee;
import com.training.gym.trainee.dto.CreateTraineeDto;
import com.training.gym.trainee.dto.UpdateTraineeDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TraineeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "active", ignore = true)
    Trainee toTrainee(CreateTraineeDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateTrainee(UpdateTraineeDto dto, @MappingTarget Trainee trainee);
}
