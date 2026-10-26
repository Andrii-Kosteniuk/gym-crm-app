package com.training.gym;

import com.training.gym.fasade.GymCrmFacade;
import com.training.gym.trainee.domain.Address;
import com.training.gym.trainee.domain.Trainee;
import com.training.gym.trainee.dto.CreateTraineeDto;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.time.Month;

public class GymCrmApp {

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext()) {

            // Register configuration and scan application components
            context.scan("com.training.gym");

            // Load external configuration
            context.getEnvironment()
                    .getPropertySources()
                    .addFirst(
                            new org.springframework.core.env.MapPropertySource(
                                    "gymConfig",
                                    java.util.Map.of(
                                            "gym.storage.initial-data-file",
                                            "file:./gym-data.json"
                                    )
                            )
                    );

            // Initialize Spring container
            context.refresh();

            // Retrieve Facade bean from Spring IoC container
            GymCrmFacade facade = context.getBean(GymCrmFacade.class);

            System.out.println("\n========== GYM CRM DEMO ==========\n");

            // Retrieve trainee initialized from JSON
            var trainee = facade.getTraineeById(1L);
            System.out.println("Loaded trainee:\n" + trainee);

            // Retrieve second trainee initialized from JSON
            var trainee2 = facade.getTraineeById(2L);
            System.out.println("Loaded second trainee:\n" + trainee2);

            // Retrieve trainer initialized from JSON
            var trainer = facade.getTrainerById(1L);
            System.out.println("Loaded trainer:\n" + trainer);

            // Retrieve training initialized from JSON
            var training = facade.getTrainingById(1L);
            System.out.println("Loaded training:\n" + training);

            // Creating new trainee
            var createdTrainee = facade.createTrainee(
                    new CreateTraineeDto(
                            "Andrii",
                            "Kosteniuk",
                            LocalDate.of(2026, Month.OCTOBER,26),
                            createAddress()
                    )
            );

            System.out.println("Created trainee:\n" + createdTrainee);

            // Check whether trainee storage contains new trainee.
            Trainee traineeById = facade.getTraineeById(createdTrainee.getId());

            System.out.println("Loaded trainee:\n" + traineeById);

            System.out.println("========== DEMO COMPLETED ==========");
        }
    }

    private static Address createAddress() {
        return new Address("Wroclaw", "Main Street", 7, "12-345");
    }
}
