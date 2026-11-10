package com.training.gym.mapper;

import com.training.gym.dto.trainee.UpdateTraineeDto;
import com.training.gym.model.Trainee;
import com.training.gym.dto.trainee.CreateTraineeDto;
import com.training.gym.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface TraineeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "dateOfBirth", source = "dto.dateOfBirth")
    @Mapping(target = "address", source = "dto.address")
    @Mapping(target = "trainers", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    Trainee toTrainee(CreateTraineeDto dto, User user);

    default Trainee updateTrainee(UpdateTraineeDto dto) {
        return Trainee.builder()
                .user(User.builder()
                        .firstName(dto.firstName())
                        .lastName(dto.lastName())
                        .userName(dto.userName())
                        .build())
                .address(dto.address())
                .dateOfBirth(dto.dateOfBirth())
                .build();
    }

}
