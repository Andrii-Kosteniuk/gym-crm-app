package com.training.gym.common.credentials;

import com.training.gym.common.credentials.generator.IdGenerator;
import java.util.concurrent.atomic.AtomicLong;

public class IdGeneratorImpl implements IdGenerator {

    private final AtomicLong id;

    public IdGeneratorImpl(long initialValue) {
        this.id = new AtomicLong(initialValue);
    }
    @Override
    public Long generate() {
        return id.incrementAndGet();
    }
}
