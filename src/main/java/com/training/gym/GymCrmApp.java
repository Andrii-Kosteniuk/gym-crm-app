package com.training.gym;

import com.training.gym.trainee.domain.Address;
import com.training.gym.trainee.domain.Trainee;
import com.training.gym.trainee.dto.CreateTraineeDto;
import com.training.gym.trainee.infrastructure.InMemoryTraineeRepository;
import com.training.gym.trainee.service.TraineeService;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDate;
import java.util.Map;

@SpringBootApplication
@AllArgsConstructor
public class GymCrmApp implements CommandLineRunner {

    private TraineeService service;
    private InMemoryTraineeRepository repository;
    public static void main(String[] args) {
        SpringApplication.run(GymCrmApp.class, args);
    }


    @Override
    public void run(String @NonNull ... args)  {

        Address address = Address.builder()
                .city("Wroclaw")
                .street("Some street")
                .buildNumber(12345)
                .zipcode("12345")
                .build();
        CreateTraineeDto trainee = new CreateTraineeDto(
                "Andrii",
        "Kosteniuk",
                LocalDate.of(1990, 1, 1),
        address
        );


        service.createTrainee(trainee);
        Map<Long, Trainee> storage = repository.getStorage();
        System.out.println(storage);
    }
}
