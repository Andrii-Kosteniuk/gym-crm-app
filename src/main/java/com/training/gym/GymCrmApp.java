package com.training.gym;

import com.training.gym.config.PersistenceConfig;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.dto.trainee.TraineeTrainingCriteria;
import com.training.gym.dto.trainer.TrainerTrainingCriteria;
import com.training.gym.dto.training.CreateTrainingDto;
import com.training.gym.facade.GymCrmFacade;
import com.training.gym.model.Trainee;
import com.training.gym.model.Trainer;
import com.training.gym.model.Training;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.Clock;
import java.time.LocalDate;

public final class GymCrmApp {
    private static final String DEMO_PASSWORD = "DemoPass123!";
    private static final String TRAINEE_USERNAME = "james.bond";
    private static final String TRAINER_USERNAME = "sarah.connor";

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().setActiveProfiles("dev");
            context.register(PersistenceConfig.class);
            context.refresh();
            GymCrmFacade facade = context.getBean(GymCrmFacade.class);
            Credentials traineeCredentials = new Credentials(TRAINEE_USERNAME, DEMO_PASSWORD);
            Credentials trainerCredentials = new Credentials(TRAINER_USERNAME, DEMO_PASSWORD);

            System.out.println("\n========== GYM CRM DEMO ==========");

            Trainee trainee = facade.findTraineeByUsername(traineeCredentials, TRAINEE_USERNAME);
            Trainer trainer = facade.findTrainerByUsername(trainerCredentials, TRAINER_USERNAME);
            System.out.printf("Trainer: id=%d, username=%s%n", trainer.getId(), trainer.getUser().getUserName());
            System.out.printf("Trainee: id=%d, username=%s%n", trainee.getId(), trainee.getUser().getUserName());

            System.out.printf("Trainee by id: %d%n", facade.getTraineeById(traineeCredentials, trainee.getId()).getId());
            System.out.printf("Trainer by id: %d%n", facade.getTrainerById(trainerCredentials, trainer.getId()).getId());

            var traineeCriteria = new TraineeTrainingCriteria(null, null, null, null);
            var trainerCriteria = new TrainerTrainingCriteria(null, null, null);
            System.out.printf("Trainee trainings before creation: %d%n", facade.findTraineeTrainings(traineeCredentials, TRAINEE_USERNAME, traineeCriteria).size());
            System.out.printf("Trainer trainings before creation: %d%n", facade.findTrainerTrainings(trainerCredentials, TRAINER_USERNAME, trainerCriteria).size());

            Training created = facade.createTraining(trainerCredentials,
                    new CreateTrainingDto(trainee.getId(), trainer.getId(), "Demo session", trainer.getSpecialization().getId(),
                            LocalDate.now(Clock.systemUTC()).plusDays(1), 45L));
            System.out.printf("Created training: id=%d%n", created.getId());
            System.out.printf("Training by id: %d%n", facade.getTrainingById(trainerCredentials, created.getId()).getId());


            System.out.println("========== DEMO COMPLETED ==========");
        }
    }
}
