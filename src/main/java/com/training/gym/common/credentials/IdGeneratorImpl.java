package com.training.gym.common.credentials;

import com.training.gym.common.credentials.generator.IdGenerator;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class IdGeneratorImpl implements IdGenerator {

    private final AtomicLong id = new AtomicLong();

    @Override
    public Long generate() {
        return id.incrementAndGet();
    }
}
