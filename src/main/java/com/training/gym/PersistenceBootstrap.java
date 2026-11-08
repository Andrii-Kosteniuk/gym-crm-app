
package com.training.gym;

import com.training.gym.config.PersistenceConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public final class PersistenceBootstrap {

    private PersistenceBootstrap() {
    }

    public static void main(String[] args) {

        try (var context = new AnnotationConfigApplicationContext()) {

            context.getEnvironment().setActiveProfiles("dev");
            context.register(PersistenceConfig.class);
            context.refresh();

            System.out.println("JPA infrastructure initialized");
        }
    }
}
