package com.training.gym;

import com.training.gym.facade.GymCrmFacade;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class GymCrmApp {

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext()) {

            // Register configuration and scan application components
            context.scan("com.training.gym");

            // Initialize Spring container
            context.refresh();

            // Retrieve Facade bean from Spring IoC container
            GymCrmFacade facade = context.getBean(GymCrmFacade.class);

            System.out.println("\n========== GYM CRM DEMO ==========\n");



            System.out.println("========== DEMO COMPLETED ==========");
        }
    }

}
