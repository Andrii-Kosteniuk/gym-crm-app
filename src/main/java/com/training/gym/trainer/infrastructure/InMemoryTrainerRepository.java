package com.training.gym.trainer.infrastructure;

import com.training.gym.trainer.domain.Trainer;
import com.training.gym.trainer.repository.TrainerRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryTrainerRepository implements TrainerRepository {

    @Getter
    private Map<Long, Trainer> storage;

    @Autowired
    public void setStorage(@Qualifier("trainerStorage") Map<Long, Trainer> storage) {
        this.storage = storage;
    }

    @Override
    public Trainer save(Trainer entity) {
        storage.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Trainer> findAll() {
        return List.copyOf(storage.values());
    }

    @Override
    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);
    }

    @Override
    public boolean existsByUserName(String userName) {
        return storage.values()
                .stream()
                .anyMatch(entity -> entity.getUserName().equals(userName));
    }

    @Override
    public Optional<Trainer> findByUserName(String username) {
        return storage.values()
                .stream()
                .filter(entity -> entity.getUserName().equals(username))
                .findFirst();
    }
}
