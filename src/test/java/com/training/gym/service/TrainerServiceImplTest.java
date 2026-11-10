package com.training.gym.service;

import com.training.gym.dao.TraineeRepository;
import com.training.gym.dao.TrainerRepository;
import com.training.gym.dao.TrainingTypeRepository;
import com.training.gym.dao.UserRepository;
import com.training.gym.dto.auth.ChangePasswordDto;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.dto.trainer.CreateTrainerDto;
import com.training.gym.dto.trainer.UpdateTrainerDto;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.mapper.TrainerMapper;
import com.training.gym.model.Trainer;
import com.training.gym.model.TrainingType;
import com.training.gym.model.User;
import com.training.gym.util.PasswordGenerator;
import com.training.gym.util.UsernameGenerator;
import com.training.gym.validation.TrainerDtoValidator;
import com.training.gym.dao.TrainingRepository;
import com.training.gym.dto.trainer.TrainerTrainingCriteria;
import com.training.gym.exception.AccessDeniedException;
import com.training.gym.model.Training;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {
    static final long ID = 1L, TYPE_ID = 7L;
    static final String USERNAME = "james.bond", RAW = "raw-password", HASH = "hashed-password";
    @Mock
    TrainerRepository trainerRepository;
    @Mock
    TraineeRepository traineeRepository;
    @Mock
    ProfileAuthorizationService authorizationService;
    @Mock
    CredentialService credentialService;
    @Mock
    TrainerMapper trainerMapper;
    @Mock
    UsernameGenerator usernameGenerator;
    @Mock
    PasswordGenerator passwordGenerator;
    @Mock
    TrainerDtoValidator trainerDtoValidator;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    UserRepository userRepository;
    @Mock
    TrainingTypeRepository trainingTypeRepository;
    @Mock
    TrainingRepository trainingRepository;
    @InjectMocks
    TrainerServiceImpl service;
    Credentials credentials;
    Trainer trainer;
    TrainingType type;
    CreateTrainerDto createDto;
    UpdateTrainerDto updateDto;

    @BeforeEach
    void setUp() {
        credentials = new Credentials(USERNAME, RAW);

        type = TrainingType.builder().id(TYPE_ID).name("Fitness").build();

        trainer = Trainer.builder().

                id(ID)
                .user(User.builder().id(ID)
                        .firstName("James")
                        .lastName("Bond").userName(USERNAME)
                        .password(HASH)
                        .active(Boolean.TRUE)
                        .build())
                .specialization(type)
                .build();
        createDto = new CreateTrainerDto("James", "Bond", TYPE_ID);
        updateDto = new UpdateTrainerDto("Andrew", "Bond", USERNAME, TYPE_ID);
    }

    void stubCreation() {
        // Arrange // Act // Assert
        when(usernameGenerator.generate(eq("James"), eq("Bond"), any())).thenReturn(USERNAME);
        when(passwordGenerator.generate()).thenReturn(RAW);
        when(passwordEncoder.encode(RAW)).thenReturn(HASH);
        when(trainingTypeRepository.findById(TYPE_ID)).thenReturn(Optional.of(type));
        when(trainerMapper.toTrainer(eq(createDto), any(User.class), same(type)))
                .thenAnswer(inv -> Trainer.builder().user(inv.getArgument(1)).specialization(type).build());
    }

    @Nested
    class Registration {
        @Test
        void createsTrainerWithEncodedPasswordAndSpecialization() {
            // Arrange
            stubCreation();
            when(trainerRepository.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Trainer result = service.createTrainer(createDto);

            // Assert
            assertEquals(USERNAME, result.getUser().getUserName());
            assertEquals(HASH, result.getUser().getPassword());
            assertTrue(result.getUser().getActive());
            assertSame(type, result.getSpecialization());
            verify(passwordEncoder, times(1)).encode(RAW);
            verify(trainerDtoValidator).validateCreate(createDto);
            verify(trainerRepository).save(result);
        }

        @Test
        void rejectsInvalidDtoBeforePersistence() {
            // Arrange
            doThrow(new IllegalArgumentException("Invalid")).when(trainerDtoValidator).validateCreate(createDto);

            // Act // Assert
            assertThrows(IllegalArgumentException.class, () -> service.createTrainer(createDto));
            verifyNoInteractions(trainerRepository, trainingTypeRepository, passwordGenerator);
        }

        @Test
        void rejectsUnknownSpecializationBeforeMapping() {
            // Arrange
            when(usernameGenerator.generate(eq("James"), eq("Bond"), any())).thenReturn(USERNAME);
            when(passwordGenerator.generate()).thenReturn(RAW);
            when(trainingTypeRepository.findById(TYPE_ID)).thenReturn(Optional.empty());

            // Act // Assert
            assertThrows(EntityNotFoundException.class, () -> service.createTrainer(createDto));
            verifyNoInteractions(trainerMapper, trainerRepository, passwordEncoder);
        }

        @Test
        void propagatesPersistenceFailure() {
            // Arrange
            stubCreation();
            when(trainerRepository.save(any(Trainer.class))).thenThrow(new IllegalStateException("Database failure"));

            // Act // Assert
            assertThrows(IllegalStateException.class, () -> service.createTrainer(createDto));
        }
    }

    @Nested
    class Updates {
        @Test
        void validatesAuthorizesAndDelegatesToMapper() {
            // Arrange
            when(authorizationService.requireTrainerOwner(credentials, USERNAME)).thenReturn(trainer);

            // Act // Assert
            assertSame(trainer, service.updateTrainer(credentials, updateDto));
            InOrder order = inOrder(trainerDtoValidator, authorizationService, trainerMapper, trainerRepository);
            order.verify(trainerDtoValidator).validateUpdate(updateDto);
            order.verify(authorizationService).requireTrainerOwner(credentials, USERNAME);
            order.verify(trainerMapper).updateTrainer(updateDto, trainer);

        }

        @Test
        void rejectsInvalidUpdateBeforeAuthorization() {
            // Arrange
            doThrow(new IllegalArgumentException("Invalid")).when(trainerDtoValidator).validateUpdate(updateDto);

            // Act // Assert
            assertThrows(IllegalArgumentException.class, () -> service.updateTrainer(credentials, updateDto));
            verifyNoInteractions(authorizationService, trainerMapper, trainerRepository);
        }

        @Test
        void doesNotModifyWhenAuthorizationFails() {
            // Arrange
            when(authorizationService.requireTrainerOwner(credentials, USERNAME)).thenThrow(new SecurityException("Denied"));

            // Act // Assert
            assertThrows(SecurityException.class, () -> service.updateTrainer(credentials, updateDto));
            verifyNoInteractions(trainerMapper, trainerRepository);
        }
    }

    @Nested
    class Lookups {
        @Test
        void findsByIdAfterAuthorization() {
            // Arrange
            when(trainerRepository.findById(ID)).thenReturn(Optional.of(trainer));

            // Act // Assert
            assertSame(trainer, service.getTrainerById(credentials, ID));
            InOrder order = inOrder(authorizationService, trainerRepository);
            order.verify(authorizationService).requireTrainer(credentials);
            order.verify(trainerRepository).findById(ID);
        }

        @Test
        void rejectsUnknownId() {
            // Arrange
            when(trainerRepository.findById(ID)).thenReturn(Optional.empty());

            // Act // Assert
            assertThrows(EntityNotFoundException.class, () -> service.getTrainerById(credentials, ID));
        }

        @Test
        void findsByUsernameAfterAuthorization() {
            // Arrange
            when(trainerRepository.findByUserName(USERNAME)).thenReturn(Optional.of(trainer));

            // Act // Assert
            assertSame(trainer, service.findByUsername(credentials, USERNAME));
            verify(authorizationService).requireTrainer(credentials);
        }

        @Test
        void blocksLookupWhenAuthenticationFails() {
            // Arrange
            when(authorizationService.requireTrainer(credentials)).thenThrow(new SecurityException("Denied"));

            // Act // Assert
            assertThrows(SecurityException.class, () -> service.findByUsername(credentials, USERNAME));
            verifyNoInteractions(trainerRepository);
        }
    }

    @Nested
    class StateChanges {
        @Test
        void activatesInactiveTrainer() {
            // Arrange
            trainer.getUser().setActive(false);

            when(trainerRepository.findById(ID)).thenReturn(Optional.of(trainer));

            // Act
            service.activateTrainer(credentials, trainer);

            // Assert
            verify(authorizationService).requireTrainer(credentials);

            verify(trainerRepository).activateTrainer(trainer);
        }

        @Test
        void rejectsAlreadyActiveTrainer() {
            // Arrange
            when(trainerRepository.findById(ID)).thenReturn(Optional.of(trainer));

            // Act // Assert
            assertThrows(IllegalStateException.class, () -> service.activateTrainer(credentials, trainer));
        }

        @Test
        void deactivatesActiveTrainer() {
            // Arrange
            trainer.getUser().setActive(true);
            when(trainerRepository.findById(ID)).thenReturn(Optional.of(trainer));

            // Act
            service.deactivateTrainer(credentials, trainer);

            // Assert
            verify(authorizationService).requireTrainer(credentials);

            verify(trainerRepository).deactivateTrainer(trainer);
        }

        @Test
        void rejectsAlreadyInactiveTrainer() {
            // Arrange
            trainer.getUser().setActive(false);
            when(trainerRepository.findById(ID)).thenReturn(Optional.of(trainer));

            // Act

            // Assert
            assertThrows(IllegalStateException.class, () -> service.deactivateTrainer(credentials, trainer));
        }

        @Test
        void rejectsNullTrainer() {
            // Arrange

            // Act // Assert
            assertThrows(NullPointerException.class, () -> service.activateTrainer(credentials, null));
            assertThrows(NullPointerException.class, () -> service.deactivateTrainer(credentials, null));
            verifyNoInteractions(trainerRepository, authorizationService);
        }
    }


    @Nested
    class FindTrainingsTests {

        @Test
        void shouldReturnTrainingsForAuthenticatedTrainer() {
            // Arrange
            TrainerTrainingCriteria criteria =
                    mock(TrainerTrainingCriteria.class);

            Training training = new Training();
            List<Training> expectedTrainings = List.of(training);

            when(authorizationService.requireTrainerOwner(
                    credentials, USERNAME
            )).thenReturn(trainer);

            when(trainingRepository.findByTrainerUsername(
                    USERNAME, criteria
            )).thenReturn(expectedTrainings);

            // Act
            List<Training> result = service.findTrainings(
                    credentials, USERNAME, criteria
            );

            // Assert
            assertSame(expectedTrainings, result);

            verify(authorizationService)
                    .requireTrainerOwner(credentials, USERNAME);

            verify(trainingRepository)
                    .findByTrainerUsername(USERNAME, criteria);
        }

        @Test
        void shouldReturnEmptyListWhenTrainerHasNoTrainings() {
            // Arrange
            TrainerTrainingCriteria criteria =
                    mock(TrainerTrainingCriteria.class);

            when(trainingRepository.findByTrainerUsername(
                    USERNAME, criteria
            )).thenReturn(List.of());

            // Act
            List<Training> result = service.findTrainings(
                    credentials, USERNAME, criteria
            );

            // Assert
            assertTrue(result.isEmpty());

            verify(authorizationService)
                    .requireTrainerOwner(credentials, USERNAME);

            verify(trainingRepository)
                    .findByTrainerUsername(USERNAME, criteria);
        }

        @Test
        void shouldRejectNullCriteria() {
            // Act
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> service.findTrainings(
                            credentials, USERNAME, null
                    )
            );

            // Assert
            assertEquals("Criteria must not be null",
                    exception.getMessage());

            verifyNoInteractions(
                    authorizationService,
                    trainingRepository
            );
        }

        @Test
        void shouldRejectBlankTrainerUsername() {
            // Arrange
            TrainerTrainingCriteria criteria =
                    mock(TrainerTrainingCriteria.class);

            // Act
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> service.findTrainings(
                            credentials, " ", criteria
                    )
            );

            // Assert
            assertEquals("Trainer username must not be blank",
                    exception.getMessage());

            verifyNoInteractions(
                    authorizationService,
                    trainingRepository
            );
        }

        @Test
        void shouldRejectUnauthorizedTrainer() {
            // Arrange
            TrainerTrainingCriteria criteria =
                    mock(TrainerTrainingCriteria.class);

            AccessDeniedException expectedException =
                    new AccessDeniedException("Access denied");

            when(authorizationService.requireTrainerOwner(
                    credentials, USERNAME
            )).thenThrow(expectedException);

            // Act
            AccessDeniedException exception = assertThrows(
                    AccessDeniedException.class,
                    () -> service.findTrainings(
                            credentials, USERNAME, criteria
                    )
            );

            // Assert
            assertSame(expectedException, exception);

            verifyNoInteractions(trainingRepository);
        }
    }

    @Nested
    class UnassignedTrainersTests {

        @Test
        void shouldReturnUnassignedTrainersForTraineeOwner() {
            // Arrange
            String traineeUsername = "James.Bond";

            Trainer availableTrainer = new Trainer();
            availableTrainer.setId(2L);

            List<Trainer> expectedTrainers =
                    List.of(availableTrainer);

            when(trainerRepository.findUnassignedTrainers(
                    traineeUsername
            )).thenReturn(expectedTrainers);

            // Act
            List<Trainer> result = service.getUnassignedTrainers(
                    credentials, traineeUsername
            );

            // Assert
            assertSame(expectedTrainers, result);

            verify(authorizationService)
                    .requireTraineeOwner(
                            credentials, traineeUsername
                    );

            verify(trainerRepository)
                    .findUnassignedTrainers(traineeUsername);
        }

        @Test
        void shouldReturnEmptyListWhenAllTrainersAreAssigned() {
            // Arrange
            String traineeUsername = "James.Bond";

            when(trainerRepository.findUnassignedTrainers(
                    traineeUsername
            )).thenReturn(List.of());

            // Act
            List<Trainer> result = service.getUnassignedTrainers(
                    credentials, traineeUsername
            );

            // Assert
            assertTrue(result.isEmpty());

            verify(authorizationService)
                    .requireTraineeOwner(
                            credentials, traineeUsername
                    );

            verify(trainerRepository)
                    .findUnassignedTrainers(traineeUsername);
        }

        @Test
        void shouldRejectUnauthorizedTrainee() {
            // Arrange
            String traineeUsername = "James.Bond";

            AccessDeniedException expectedException =
                    new AccessDeniedException("Access denied");

            when(authorizationService.requireTraineeOwner(
                    credentials, traineeUsername
            )).thenThrow(expectedException);

            // Act
            AccessDeniedException exception = assertThrows(
                    AccessDeniedException.class,
                    () -> service.getUnassignedTrainers(
                            credentials, traineeUsername
                    )
            );

            // Assert
            assertSame(expectedException, exception);

            verifyNoInteractions(trainerRepository);
        }

        @Test
        void shouldRejectNullTraineeUsername() {
            // Act
            assertThrows(
                    NullPointerException.class,
                    () -> service.getUnassignedTrainers(
                            credentials, null
                    )
            );

            // Assert
            verifyNoInteractions(
                    authorizationService,
                    trainerRepository
            );
        }
    }


    @Nested
    class OtherOperations {
        @Test
        void deletesAuthorizedTrainer() {
            // Arrange
            when(authorizationService.requireTrainerOwner(credentials, USERNAME)).thenReturn(trainer);

            // Act
            service.removeTrainerByUsername(credentials, USERNAME);

            // Assert
            verify(trainerRepository).delete(trainer);
        }

        @Test
        void blocksUnauthorizedDeletion() {
            // Arrange
            when(authorizationService.requireTrainerOwner(credentials, USERNAME)).thenThrow(new SecurityException("Denied"));

            // Act

            // Assert
            assertThrows(SecurityException.class, () -> service.removeTrainerByUsername(credentials, USERNAME));
            verifyNoInteractions(trainerRepository);
        }

        @Test
        void delegatesPasswordChangeAfterAuthorization() {
            // Arrange
            ChangePasswordDto dto = new ChangePasswordDto(RAW, "new-password");
            service.changePassword(credentials, dto);
            InOrder order = inOrder(authorizationService, credentialService);
            order.verify(authorizationService).requireTrainer(credentials);
            order.verify(credentialService).changePassword(credentials, dto);
        }

        @Test
        void blocksPasswordChangeWhenAuthorizationFails() {
            // Arrange
            ChangePasswordDto dto = new ChangePasswordDto(RAW, "new-password");

            // Act

            // Assert
            when(authorizationService.requireTrainer(credentials)).thenThrow(new SecurityException("Denied"));
            assertThrows(SecurityException.class, () -> service.changePassword(credentials, dto));
            verifyNoInteractions(credentialService);
        }
    }
}