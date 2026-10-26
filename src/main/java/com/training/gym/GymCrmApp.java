package com.training.gym;

import com.training.gym.fasade.GymCrmFacade;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@AllArgsConstructor
public class GymCrmApp implements CommandLineRunner {

    private final GymCrmFacade crmFacade;

    public static void main(String[] args) {
        SpringApplication.run(GymCrmApp.class, args);
    }


    @Override
    public void run(String @NonNull ... args)  {

        // TODO: The application is going to use fasade

    }
}
