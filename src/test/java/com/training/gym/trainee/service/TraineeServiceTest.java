package com.training.gym.trainee.service;

import com.training.gym.common.credentials.generator.IdGenerator;
import com.training.gym.common.credentials.generator.PasswordGenerator;
import com.training.gym.common.credentials.generator.UsernameGenerator;
import com.training.gym.common.exception.EntityNotFoundException;
import com.training.gym.common.exception.ResourceConflictException;
import com.training.gym.trainee.domain.Address;
import com.training.gym.trainee.domain.Trainee;
import com.training.gym.trainee.dto.CreateTraineeDto;
import com.training.gym.trainee.dto.UpdateTraineeDto;
import com.training.gym.trainee.mapper.TraineeMapper;
import com.training.gym.trainee.repository.TraineeRepository;
import com.training.gym.trainer.repository.TrainerRepository;
import com.training.gym.training.repository.TrainingRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    private static final Long TRAINEE_ID = 1L;
    private static final Long TRAINING_ID = 1L;
    private static final String USERNAME = "andrii.kosteniuk";
    private static final String PASSWORD = "secure-password";

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TraineeMapper traineeMapper;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    @InjectMocks
    private TraineeServiceImpl traineeService;


    private Trainee trainee;
    private CreateTraineeDto createDto;
    private Address address;

    @BeforeEach
    void setUp() {
        address = Address.builder()
                .city("Wroclaw")
                .street("Pl. Grunwaldzki")
                .buildNumber(10)
                .zipcode("12-345")
                .build();

        trainee = new Trainee();
        trainee.setId(TRAINEE_ID);
        trainee.setFirstName("Andrii");
        trainee.setLastName("Kosteniuk");
        trainee.setUserName(USERNAME);
        trainee.setPassword(PASSWORD);
        trainee.setActive(true);
        trainee.setDateOfBirth(LocalDate.of(2000, 10, 19));
        trainee.setAddress(address);

        createDto = new CreateTraineeDto(
                "Andrii",
                "Kosteniuk",
                LocalDate.of(2000, 10, 19),
                address
        );
    }


    @Nested
    class CreateTraineeTests {

        @Test
        void shouldCreateTraineeSuccessfully() {
            // Arrange
            when(traineeMapper.toTrainee(createDto))
                    .thenReturn(trainee);

            when(idGenerator.generate())
                    .thenReturn(TRAINEE_ID);

            when(passwordGenerator.generate())
                    .thenReturn(PASSWORD);

            when(usernameGenerator.generate(
                    eq("Andrii"),
                    eq("Kosteniuk"),
                    ArgumentMatchers.any()
            )).thenReturn(USERNAME);

            when(traineeRepository.save(any(Trainee.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Trainee result = traineeService.createTrainee(createDto);

            // Assert
            assertNotNull(result);

            assertAll(
                    () -> assertEquals(TRAINEE_ID, result.getId()),
                    () -> assertEquals("Andrii", result.getFirstName()),
                    () -> assertEquals("Kosteniuk", result.getLastName()),
                    () -> assertEquals(USERNAME, result.getUserName()),
                    () -> assertEquals(PASSWORD, result.getPassword()),
                    () -> assertTrue(result.isActive())
            );

            verify(traineeMapper).toTrainee(createDto);
            verify(idGenerator).generate();
            verify(passwordGenerator).generate();
            verify(traineeRepository).save(trainee);
        }

        @Test
        void shouldThrowExceptionWhenCreateDtoIsNull() {
            // Act
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> traineeService.createTrainee(null)
            );

            // Assert
            assertEquals(
                    "Create trainee DTO must not be null",
                    exception.getMessage()
            );

            verifyNoInteractions(
                    traineeMapper,
                    traineeRepository,
                    trainerRepository,
                    idGenerator,
                    usernameGenerator,
                    passwordGenerator
            );
        }

        @Test
        void shouldCheckUsernameAgainstBothRepositories() {
            // Arrange
            when(traineeMapper.toTrainee(createDto))
                    .thenReturn(trainee);

            when(idGenerator.generate())
                    .thenReturn(TRAINEE_ID);

            when(passwordGenerator.generate())
                    .thenReturn(PASSWORD);

            when(usernameGenerator.generate(
                    anyString(),
                    anyString(),
                    ArgumentMatchers.any()
            )).thenAnswer(invocation -> {
                Predicate<String> usernameExists =
                        invocation.getArgument(2);

                boolean exists = usernameExists.test(USERNAME);

                assertFalse(exists);

                return USERNAME;
            });

            when(traineeRepository.save(any(Trainee.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            traineeService.createTrainee(createDto);

            // Assert
            verify(traineeRepository).existsByUserName(USERNAME);
            verify(trainerRepository).existsByUserName(USERNAME);
        }

        @Test
        void shouldDetectUsernameExistingInTraineeRepository() {
            // Arrange
            when(traineeMapper.toTrainee(createDto))
                    .thenReturn(trainee);

            when(idGenerator.generate())
                    .thenReturn(TRAINEE_ID);

            when(passwordGenerator.generate())
                    .thenReturn(PASSWORD);

            when(traineeRepository.existsByUserName(USERNAME))
                    .thenReturn(true);

            when(usernameGenerator.generate(
                    anyString(),
                    anyString(),
                    ArgumentMatchers.any()
            )).thenAnswer(invocation -> {
                Predicate<String> predicate =
                        invocation.getArgument(2);

                assertTrue(predicate.test(USERNAME));

                return USERNAME + "1";
            });

            when(traineeRepository.save(any(Trainee.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Trainee result = traineeService.createTrainee(createDto);

            // Assert
            assertEquals(USERNAME + "1", result.getUserName());

            verify(traineeRepository).existsByUserName(USERNAME);

            verify(trainerRepository, never())
                    .existsByUserName(USERNAME);
        }

        @Test
        void shouldDetectUsernameExistingInTrainerRepository() {
            // Arrange
            when(traineeMapper.toTrainee(createDto))
                    .thenReturn(trainee);

            when(idGenerator.generate())
                    .thenReturn(TRAINEE_ID);

            when(passwordGenerator.generate())
                    .thenReturn(PASSWORD);

            when(trainerRepository.existsByUserName(USERNAME))
                    .thenReturn(true);

            when(usernameGenerator.generate(
                    anyString(),
                    anyString(),
                    ArgumentMatchers.any()
            )).thenAnswer(invocation -> {
                Predicate<String> predicate =
                        invocation.getArgument(2);

                assertTrue(predicate.test(USERNAME));

                return USERNAME + "1";
            });

            when(traineeRepository.save(any(Trainee.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Trainee result = traineeService.createTrainee(createDto);

            // Assert
            assertEquals(USERNAME + "1", result.getUserName());

            verify(traineeRepository).existsByUserName(USERNAME);
            verify(trainerRepository).existsByUserName(USERNAME);
        }

        @Test
        void shouldSaveGeneratedCredentialsAndActiveStatus() {
            // Arrange
            Trainee newTrainee = new Trainee();

            newTrainee.setFirstName("Andrii");
            newTrainee.setLastName("Kosteniuk");

            when(traineeMapper.toTrainee(createDto))
                    .thenReturn(newTrainee);

            when(idGenerator.generate())
                    .thenReturn(TRAINEE_ID);

            when(passwordGenerator.generate())
                    .thenReturn(PASSWORD);

            when(usernameGenerator.generate(
                    anyString(),
                    anyString(),
                    ArgumentMatchers.any()
            )).thenReturn(USERNAME);

            when(traineeRepository.save(any(Trainee.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            traineeService.createTrainee(createDto);

            // Assert
            ArgumentCaptor<Trainee> captor =
                    ArgumentCaptor.forClass(Trainee.class);

            verify(traineeRepository).save(captor.capture());

            Trainee saved = captor.getValue();

            assertAll(
                    () -> assertEquals(TRAINEE_ID, saved.getId()),
                    () -> assertEquals(USERNAME, saved.getUserName()),
                    () -> assertEquals(PASSWORD, saved.getPassword()),
                    () -> assertTrue(saved.isActive())
            );
        }
    }

    @Nested
    class UpdateTraineeTests {

        @Test
        void shouldUpdateExistingTraineeSuccessfully() {
            // Arrange
            UpdateTraineeDto dto = new UpdateTraineeDto(
                    "Andrew",
                    "Kosteniuk",
                    USERNAME,
                    LocalDate.of(2000, 10, 19),
                    address
            );

            when(traineeRepository.findByUserName(USERNAME))
                    .thenReturn(Optional.of(trainee));

            doAnswer(invocation -> {
                UpdateTraineeDto source = invocation.getArgument(0);
                Trainee target = invocation.getArgument(1);

                target.setFirstName(source.firstName());
                target.setLastName(source.lastName());
                target.setDateOfBirth(source.dateOfBirth());
                target.setAddress(source.address());

                return null;
            }).when(traineeMapper).updateTrainee(dto, trainee);

            when(traineeRepository.save(trainee))
                    .thenReturn(trainee);

            // Act
            Trainee result = traineeService.updateTrainee(dto);

            // Assert
            assertNotNull(result);

            assertAll(
                    () -> assertEquals("Andrew", result.getFirstName()),
                    () -> assertEquals("Kosteniuk", result.getLastName()),
                    () -> assertEquals(USERNAME, result.getUserName()),
                    () -> assertEquals(TRAINEE_ID, result.getId())
            );

            verify(traineeMapper).updateTrainee(dto, trainee);
            verify(traineeRepository).save(trainee);
        }

        @Test
        void shouldThrowExceptionWhenUpdateDtoIsNull() {
            // Act
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> traineeService.updateTrainee(null)
            );

            // Assert
            assertEquals(
                    "Update trainee DTO must not be null",
                    exception.getMessage()
            );

            verifyNoInteractions(traineeRepository, traineeMapper);
        }

        @Test
        void shouldThrowExceptionWhenUpdatingNonexistentTrainee() {
            // Arrange
            UpdateTraineeDto dto = new UpdateTraineeDto(
                    "Andrew",
                    "Kosteniuk",
                    "unknown.user",
                    LocalDate.of(2000, 10, 19),
                    address
            );

            when(traineeRepository.findByUserName("unknown.user"))
                    .thenReturn(Optional.empty());

            // Act
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> traineeService.updateTrainee(dto)
            );

            // Assert
            assertEquals(
                    "Trainee with username unknown.user not found",
                    exception.getMessage()
            );

            verify(traineeRepository, never())
                    .save(any(Trainee.class));

            verifyNoInteractions(traineeMapper);
        }

        @Test
        void shouldPreserveUsernameAndCredentialsDuringUpdate() {
            // Arrange
            UpdateTraineeDto dto = new UpdateTraineeDto(
                    "Andrew",
                    "Updated",
                    USERNAME,
                    LocalDate.of(2000, 10, 19),
                    address
            );

            when(traineeRepository.findByUserName(USERNAME))
                    .thenReturn(Optional.of(trainee));

            when(traineeRepository.save(trainee))
                    .thenReturn(trainee);

            // Act
            traineeService.updateTrainee(dto);

            // Assert
            assertAll(
                    () -> assertEquals(TRAINEE_ID, trainee.getId()),
                    () -> assertEquals(USERNAME, trainee.getUserName()),
                    () -> assertEquals(PASSWORD, trainee.getPassword()),
                    () -> assertTrue(trainee.isActive())
            );

            verify(traineeMapper).updateTrainee(dto, trainee);
        }
    }

    @Nested
    class GetTraineeTests {

        @Test
        void shouldReturnTraineeWhenIdExists() {
            // Arrange
            when(traineeRepository.findById(TRAINEE_ID))
                    .thenReturn(Optional.of(trainee));

            // Act
            Trainee result = traineeService.getTraineeById(TRAINEE_ID);

            // Assert
            assertNotNull(result);
            assertSame(trainee, result);

            verify(traineeRepository).findById(TRAINEE_ID);
        }

        @Test
        void shouldThrowExceptionWhenTraineeIdDoesNotExist() {
            // Arrange
            when(traineeRepository.findById(999L))
                    .thenReturn(Optional.empty());

            // Act
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> traineeService.getTraineeById(999L)
            );

            // Assert
            assertEquals(
                    "Trainee with id 999 not found",
                    exception.getMessage()
            );

            verify(traineeRepository).findById(999L);
        }
    }


    @Nested
    class DeleteTraineeTests {

        @Test
        void shouldRemoveTraineeWhenNoRelatedTrainingsExist() {

            // Arrange
            when(traineeRepository.existsById(TRAINING_ID))
                    .thenReturn(true);

            when(trainingRepository.existsByTraineeOrTrainerId(TRAINING_ID))
                    .thenReturn(false);

            // Act
            traineeService.removeTrainee(TRAINING_ID);

            // Assert
            verify(traineeRepository).existsById(TRAINING_ID);

            verify(trainingRepository)
                    .existsByTraineeOrTrainerId(TRAINING_ID);

            verify(traineeRepository).deleteById(TRAINING_ID);
        }
        @Test
        void shouldThrowExceptionWhenDeletingNonexistentTrainee() {
            // Arrange
            when(traineeRepository.existsById(999L))
                    .thenReturn(false);

            // Act
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> traineeService.removeTrainee(999L)
            );

            // Assert
            assertEquals(
                    "Trainee with id 999 not found",
                    exception.getMessage()
            );

            verify(traineeRepository, never())
                    .deleteById(anyLong());
        }

        @Test
        void shouldRejectNullIdWhenDeletingTrainee() {
            // Act
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> traineeService.removeTrainee(null)
            );

            // Assert
            assertEquals(
                    "Trainee ID must not be null",
                    exception.getMessage()
            );

            verifyNoInteractions(traineeRepository);
        }

        @Test
        void shouldThrowConflictExceptionWhenTraineeHasRelatedTrainings() {
            // Arrange
            Long traineeId = 1L;

            when(traineeRepository.existsById(traineeId))
                    .thenReturn(true);

            when(trainingRepository.existsByTraineeOrTrainerId(traineeId))
                    .thenReturn(true);

            // Act
            ResourceConflictException exception = assertThrows(
                    ResourceConflictException.class,
                    () -> traineeService.removeTrainee(traineeId)
            );

            // Assert
            assertEquals(
                    "Cannot delete trainee with id 1 because related trainings exist",
                    exception.getMessage()
            );

            verify(traineeRepository).existsById(traineeId);

            verify(trainingRepository)
                    .existsByTraineeOrTrainerId(traineeId);

            verify(traineeRepository, never())
                    .deleteById(anyLong());
        }
    }
}