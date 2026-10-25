package com.training.gym.trainer.service;

import com.training.gym.common.credentials.generator.IdGenerator;
import com.training.gym.common.credentials.generator.PasswordGenerator;
import com.training.gym.common.credentials.generator.UsernameGenerator;
import com.training.gym.common.exception.EntityNotFoundException;
import com.training.gym.trainee.domain.Address;
import com.training.gym.trainee.repository.TraineeRepository;
import com.training.gym.trainer.domain.Trainer;
import com.training.gym.trainer.dto.CreateTrainerDto;
import com.training.gym.trainer.dto.UpdateTrainerDto;
import com.training.gym.trainer.mapper.TrainerMapper;
import com.training.gym.trainer.repository.TrainerRepository;
import com.training.gym.training.domain.TrainingType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {

    private static final Long ID = 1L;
    private static final String USERNAME = "andrii.kosteniuk";
    private static final String PASSWORD = "secure-password";

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerMapper trainerMapper;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    @InjectMocks
    private TrainerServiceImpl trainerService;

    private Trainer trainer;
    private CreateTrainerDto createDto;
    private Address address;
    private TrainingType specialization;

    @BeforeEach
    void setUp() {
        address = Address.builder()
                .city("Wroclaw")
                .street("Pl.Grunwaldzki")
                .buildNumber(10)
                .zipcode("12-345")
                .build();

        // Assumes TrainingType is an enum.
        specialization = new TrainingType(1L, "Health");

        trainer = new Trainer();
        trainer.setFirsName("Andrii");
        trainer.setLastName("Kosteniuk");
        trainer.setSpecialization(specialization);

        createDto = new CreateTrainerDto(
                "Andrii",
                "Kosteniuk",
                LocalDate.of(2000, 10, 19),
                address,
                specialization
        );
    }

    private void mockSuccessfulCreation() {
        when(trainerMapper.toTrainer(createDto))
                .thenReturn(trainer);

        when(idGenerator.generate())
                .thenReturn(ID);

        when(passwordGenerator.generate())
                .thenReturn(PASSWORD);

        when(usernameGenerator.generate(
                eq("Andrii"),
                eq("Kosteniuk"),
                ArgumentMatchers.any()
        )).thenReturn(TrainerServiceImplTest.USERNAME);

        when(trainerRepository.save(any(Trainer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Nested
    class CreateTests {

        @Test
        void shouldCreateTrainerSuccessfully() {
            // Arrange
            mockSuccessfulCreation();

            // Act
            Trainer result = trainerService.createTrainer(createDto);

            // Assert
            assertNotNull(result);

            assertAll(
                    () -> assertEquals(ID, result.getId()),
                    () -> assertEquals("Andrii", result.getFirsName()),
                    () -> assertEquals("Kosteniuk", result.getLastName()),
                    () -> assertEquals(USERNAME, result.getUserName()),
                    () -> assertEquals(PASSWORD, result.getPassword()),
                    () -> assertTrue(result.isActive()),
                    () -> assertEquals(
                            specialization,
                            result.getSpecialization()
                    )
            );

            verify(trainerMapper).toTrainer(createDto);
            verify(idGenerator).generate();
            verify(passwordGenerator).generate();
            verify(trainerRepository).save(trainer);
        }

        @Test
        void shouldRejectNullCreateDto() {
            // Act
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> trainerService.createTrainer(null)
            );

            // Assert
            assertEquals(
                    "Create trainer DTO must not be null",
                    exception.getMessage()
            );

            verifyNoInteractions(
                    trainerRepository,
                    traineeRepository,
                    trainerMapper,
                    idGenerator,
                    usernameGenerator,
                    passwordGenerator
            );
        }

        @Test
        void shouldCheckBothRepositoriesWhenUsernameIsAvailable() {
            // Arrange
            when(trainerMapper.toTrainer(createDto))
                    .thenReturn(trainer);

            when(idGenerator.generate())
                    .thenReturn(ID);

            when(passwordGenerator.generate())
                    .thenReturn(PASSWORD);

            when(usernameGenerator.generate(
                    anyString(),
                    anyString(),
                    ArgumentMatchers.any()
            )).thenAnswer(invocation -> {
                Predicate<String> exists = invocation.getArgument(2);

                assertFalse(exists.test(USERNAME));

                return USERNAME;
            });

            when(trainerRepository.save(any(Trainer.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Trainer result = trainerService.createTrainer(createDto);

            // Assert
            assertEquals(USERNAME, result.getUserName());

            verify(traineeRepository).existsByUserName(USERNAME);
            verify(trainerRepository).existsByUserName(USERNAME);
        }

        @Test
        void shouldDetectUsernameExistingAmongTrainees() {
            // Arrange
            when(trainerMapper.toTrainer(createDto))
                    .thenReturn(trainer);

            when(idGenerator.generate())
                    .thenReturn(ID);

            when(passwordGenerator.generate())
                    .thenReturn(PASSWORD);

            when(traineeRepository.existsByUserName(USERNAME))
                    .thenReturn(true);

            when(usernameGenerator.generate(
                    anyString(),
                    anyString(),
                    ArgumentMatchers.any()
            )).thenAnswer(invocation -> {
                Predicate<String> exists = invocation.getArgument(2);

                assertTrue(exists.test(USERNAME));

                return USERNAME + "1";
            });

            when(trainerRepository.save(any(Trainer.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Trainer result = trainerService.createTrainer(createDto);

            // Assert
            assertEquals(USERNAME + "1", result.getUserName());

            verify(traineeRepository).existsByUserName(USERNAME);

            // Short-circuit evaluation: true || ... skips the second call.
            verify(trainerRepository, never())
                    .existsByUserName(USERNAME);
        }

        @Test
        void shouldDetectUsernameExistingAmongTrainers() {
            // Arrange
            when(trainerMapper.toTrainer(createDto))
                    .thenReturn(trainer);

            when(idGenerator.generate())
                    .thenReturn(ID);

            when(passwordGenerator.generate())
                    .thenReturn(PASSWORD);

            when(trainerRepository.existsByUserName(USERNAME))
                    .thenReturn(true);

            when(usernameGenerator.generate(
                    anyString(),
                    anyString(),
                    ArgumentMatchers.any()
            )).thenAnswer(invocation -> {
                Predicate<String> exists = invocation.getArgument(2);

                assertTrue(exists.test(USERNAME));

                return USERNAME + "1";
            });

            when(trainerRepository.save(any(Trainer.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Trainer result = trainerService.createTrainer(createDto);

            // Assert
            assertEquals(USERNAME + "1", result.getUserName());

            verify(traineeRepository).existsByUserName(USERNAME);
            verify(trainerRepository).existsByUserName(USERNAME);
        }

        @Test
        void shouldSaveGeneratedCredentialsAndActiveStatus() {
            // Arrange
            mockSuccessfulCreation();

            // Act
            trainerService.createTrainer(createDto);

            // Assert
            ArgumentCaptor<Trainer> captor =
                    ArgumentCaptor.forClass(Trainer.class);

            verify(trainerRepository).save(captor.capture());

            Trainer saved = captor.getValue();

            assertAll(
                    () -> assertEquals(ID, saved.getId()),
                    () -> assertEquals(USERNAME, saved.getUserName()),
                    () -> assertEquals(PASSWORD, saved.getPassword()),
                    () -> assertTrue(saved.isActive()),
                    () -> assertEquals(
                            specialization,
                            saved.getSpecialization()
                    )
            );
        }
    }

    @Nested
    class UpdateTests {

        @Test
        void shouldUpdateExistingTrainerSuccessfully() {
            // Arrange
            trainer.setId(ID);
            trainer.setUserName(USERNAME);
            trainer.setPassword(PASSWORD);
            trainer.setActive(true);

            UpdateTrainerDto dto = new UpdateTrainerDto(
                    "Andrew",
                    "Kosteniuk",
                    USERNAME,
                    LocalDate.of(1998, 5, 15),
                    address,
                    specialization
            );

            when(trainerRepository.findByUserName(USERNAME))
                    .thenReturn(Optional.of(trainer));

            doAnswer(invocation -> {
                UpdateTrainerDto source = invocation.getArgument(0);
                Trainer target = invocation.getArgument(1);

                target.setFirsName(source.firsName());
                target.setLastName(source.lastName());
                target.setSpecialization(source.specialization());

                return null;
            }).when(trainerMapper).updateTrainer(dto, trainer);

            when(trainerRepository.save(trainer))
                    .thenReturn(trainer);

            // Act
            Trainer result = trainerService.updateTrainer(dto);

            // Assert
            assertNotNull(result);

            assertAll(
                    () -> assertEquals("Andrew", result.getFirsName()),
                    () -> assertEquals("Kosteniuk", result.getLastName()),
                    () -> assertEquals(ID, result.getId()),
                    () -> assertEquals(USERNAME, result.getUserName()),
                    () -> assertEquals(PASSWORD, result.getPassword()),
                    () -> assertTrue(result.isActive()),
                    () -> assertEquals(
                            specialization,
                            result.getSpecialization()
                    )
            );

            verify(trainerRepository).findByUserName(USERNAME);
            verify(trainerMapper).updateTrainer(dto, trainer);
            verify(trainerRepository).save(trainer);
        }

        @Test
        void shouldRejectNullUpdateDto() {
            // Act
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> trainerService.updateTrainer(null)
            );

            // Assert
            assertEquals(
                    "Update trainer DTO must not be null",
                    exception.getMessage()
            );

            verifyNoInteractions(trainerRepository, trainerMapper);
        }

        @Test
        void shouldThrowExceptionWhenUpdatingNonexistentTrainer() {
            // Arrange
            UpdateTrainerDto dto = new UpdateTrainerDto(
                    "Andrew",
                    "Kosteniuk",
                    "unknown.user",
                    LocalDate.of(2000, 10, 19),
                    address,
                    specialization
            );

            when(trainerRepository.findByUserName("unknown.user"))
                    .thenReturn(Optional.empty());

            // Act
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> trainerService.updateTrainer(dto)
            );

            // Assert
            assertEquals(
                    "Trainer with username unknown.user not found",
                    exception.getMessage()
            );

            verify(trainerRepository).findByUserName("unknown.user");

            verify(trainerRepository, never())
                    .save(any(Trainer.class));

            verifyNoInteractions(trainerMapper);
        }
    }

    @Nested
    class GetByIdTests {

        @Test
        void shouldReturnTrainerWhenIdExists() {
            // Arrange
            trainer.setId(ID);

            when(trainerRepository.findById(ID))
                    .thenReturn(Optional.of(trainer));

            // Act
            Trainer result = trainerService.getTrainerById(ID);

            // Assert
            assertSame(trainer, result);

            verify(trainerRepository).findById(ID);
        }

        @Test
        void shouldThrowExceptionWhenIdDoesNotExist() {
            // Arrange
            when(trainerRepository.findById(999L))
                    .thenReturn(Optional.empty());

            // Act
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> trainerService.getTrainerById(999L)
            );

            // Assert
            assertEquals(
                    "Trainer with id 999 not found",
                    exception.getMessage()
            );

            verify(trainerRepository).findById(999L);
        }
    }
}