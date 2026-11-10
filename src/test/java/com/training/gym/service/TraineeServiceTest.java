package com.training.gym.service;

import com.training.gym.dao.TraineeRepository;
import com.training.gym.dao.TrainerRepository;
import com.training.gym.dao.UserRepository;
import com.training.gym.dto.auth.ChangePasswordDto;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.dto.trainee.CreateTraineeDto;
import com.training.gym.dto.trainee.UpdateTraineeDto;
import com.training.gym.dto.trainee.UpdateTraineeTrainersDto;
import com.training.gym.exception.AccessDeniedException;
import com.training.gym.exception.AuthenticationFailedException;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.mapper.TraineeMapper;
import com.training.gym.model.Address;
import com.training.gym.model.Trainee;
import com.training.gym.model.Trainer;
import com.training.gym.model.User;
import com.training.gym.util.PasswordGenerator;
import com.training.gym.util.UsernameGenerator;
import com.training.gym.validation.TraineeDtoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {

    private static final long ID = 1L;
    private static final String USERNAME = "james.bond";
    private static final String RAW_PASSWORD = "secure-password";
    private static final String NEW_PASSWORD = "new-secure-password";
    private static final String HASH = "encoded-password";

    @Mock
    TraineeRepository traineeRepository;
    @Mock
    TrainerRepository trainerRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    TraineeMapper traineeMapper;
    @Mock
    UsernameGenerator usernameGenerator;
    @Mock
    PasswordGenerator passwordGenerator;
    @Mock
    TraineeDtoValidator traineeValidator;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    ProfileAuthorizationService authorizationService;
    @Mock
    CredentialService credentialService;
    @InjectMocks
    TraineeServiceImpl service;

    @Captor
    ArgumentCaptor<User> userCaptor;
    @Captor
    ArgumentCaptor<Predicate<String>> existsPredicateCaptor;

    private Credentials credentials;
    private Address address;
    private Trainee trainee;
    private CreateTraineeDto createDto;

    @BeforeEach
    void setUp() {
        credentials = new Credentials(USERNAME, RAW_PASSWORD);
        address = Address.builder().city("Wroclaw").street("Grunwaldzki")
                .buildingNumber(10).zipCode("12-345").build();
        User user = User.builder()
                .id(ID)
                .firstName("James")
                .lastName("Bond")
                .userName(USERNAME)
                .password(HASH)
                .active(true)
                .build();
        trainee = Trainee.builder().id(ID).user(user)
                .dateOfBirth(LocalDate.of(2000, 10, 19)).address(address).build();
        createDto = new CreateTraineeDto("James", "Bond",
                LocalDate.of(2000, 10, 19), address);
    }

    private void stubCredentialGeneration() {
        when(usernameGenerator.generate(eq("James"), eq("Bond"), any())).thenReturn(USERNAME);
        when(passwordGenerator.generate()).thenReturn(RAW_PASSWORD);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(HASH);
    }

    private void stubMapper() {
        when(traineeMapper.toTrainee(eq(createDto), any(User.class)))
                .thenAnswer(invocation -> Trainee.builder()
                        .user(invocation.getArgument(1))
                        .dateOfBirth(createDto.dateOfBirth())
                        .address(createDto.address())
                        .build());
    }

    private void stubSaveEchoesArgument() {
        when(traineeRepository.save(any(Trainee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Trainer trainerWithId(long id) {
        return Trainer.builder().id(id).build();
    }

    @Nested
    class Registration {

        @Test
        void persistsActiveUserWithHashedPassword() {
            // Arrange
            stubCredentialGeneration();
            stubMapper();
            stubSaveEchoesArgument();

            // Act
            Trainee result = service.createTrainee(createDto);

            // Assert
            verify(traineeMapper).toTrainee(eq(createDto), userCaptor.capture());
            User persistedUser = userCaptor.getValue();
            assertEquals("James", persistedUser.getFirstName());
            assertEquals("Bond", persistedUser.getLastName());
            assertEquals(USERNAME, persistedUser.getUserName());
            assertEquals(HASH, persistedUser.getPassword());
            assertNotEquals(RAW_PASSWORD, persistedUser.getPassword());
            assertTrue(persistedUser.getActive());
            assertSame(persistedUser, result.getUser());
            verify(passwordEncoder, times(1)).encode(RAW_PASSWORD);
        }

        @Test
        void validatesDtoBeforeSaving() {
            // Arrange
            stubCredentialGeneration();
            stubMapper();
            stubSaveEchoesArgument();

            // Act
            service.createTrainee(createDto);

            // Assert
            InOrder order = inOrder(traineeValidator, traineeRepository);
            order.verify(traineeValidator).validateCreate(createDto);
            order.verify(traineeRepository).save(any(Trainee.class));
        }

        @Test
        void returnsEntityReturnedByRepository() {
            // Arrange
            stubCredentialGeneration();
            stubMapper();
            Trainee persisted = Trainee.builder().id(42L).user(trainee.getUser()).build();
            when(traineeRepository.save(any(Trainee.class))).thenReturn(persisted);

            // Act
            Trainee result = service.createTrainee(createDto);

            // Assert
            assertSame(persisted, result);
        }

        @Test
        void checksUsernameUniquenessAgainstAllUsers() {
            // Arrange
            stubCredentialGeneration();
            stubMapper();
            stubSaveEchoesArgument();
            when(userRepository.existsByUsername(USERNAME)).thenReturn(true);

            // Act
            service.createTrainee(createDto);

            // Assert
            verify(usernameGenerator).generate(eq("James"), eq("Bond"), existsPredicateCaptor.capture());
            assertTrue(existsPredicateCaptor.getValue().test(USERNAME));
            verify(userRepository).existsByUsername(USERNAME);
        }

        @Test
        void rejectsNullDtoBeforeAnyWork() {
            // Arrange
            doThrow(new IllegalArgumentException("CreateTraineeDto must not be null"))
                    .when(traineeValidator).validateCreate(null);

            // Act
            Executable act = () -> service.createTrainee(null);

            // Assert
            assertThrows(IllegalArgumentException.class, act);
            verifyNoInteractions(traineeRepository, traineeMapper, passwordGenerator,
                    usernameGenerator, passwordEncoder);
        }

        @Test
        void rejectsInvalidDtoWithoutSaving() {
            // Arrange
            doThrow(new IllegalArgumentException("First name must not be blank"))
                    .when(traineeValidator).validateCreate(createDto);

            // Act
            Executable act = () -> service.createTrainee(createDto);

            // Assert
            assertThrows(IllegalArgumentException.class, act);
            verifyNoInteractions(traineeRepository, traineeMapper, passwordGenerator,
                    usernameGenerator, passwordEncoder);
        }

        @Test
        void propagatesPersistenceFailure() {
            // Arrange
            stubCredentialGeneration();
            stubMapper();
            when(traineeRepository.save(any(Trainee.class)))
                    .thenThrow(new IllegalStateException("database failure"));

            // Act
            Executable act = () -> service.createTrainee(createDto);

            // Assert
            IllegalStateException ex = assertThrows(IllegalStateException.class, act);
            assertEquals("database failure", ex.getMessage());
        }
    }

    @Nested
    class Updates {

        private UpdateTraineeDto updateDto() {
            return new UpdateTraineeDto("Andrew", "Updated", USERNAME,
                    LocalDate.of(1999, 1, 1), address);
        }

        @Test
        void modifiesManagedEntityInPlace() {
            // Arrange
            UpdateTraineeDto dto = updateDto();
            when(authorizationService.requireTraineeOwner(credentials, USERNAME)).thenReturn(trainee);

            // Act
            Trainee result = service.updateTrainee(credentials, dto);

            // Assert
            assertSame(trainee, result);
            assertEquals("Andrew", result.getUser().getFirstName());
            assertEquals("Updated", result.getUser().getLastName());
            assertEquals(LocalDate.of(1999, 1, 1), result.getDateOfBirth());
            verify(traineeValidator).validateUpdate(dto);
            verifyNoInteractions(traineeMapper, traineeRepository);
        }

        @Test
        void keepsIdentityAndCredentialsUntouched() {
            // Arrange
            UpdateTraineeDto dto = updateDto();
            when(authorizationService.requireTraineeOwner(credentials, USERNAME)).thenReturn(trainee);

            // Act
            Trainee result = service.updateTrainee(credentials, dto);

            // Assert
            assertEquals(ID, result.getId());
            assertEquals(USERNAME, result.getUser().getUserName());
            assertEquals(HASH, result.getUser().getPassword());
            assertTrue(result.getUser().getActive());
        }

        @Test
        void rejectsNullDtoBeforeAuthorization() {
            // Arrange
            doThrow(new IllegalArgumentException("UpdateTraineeDto must not be null"))
                    .when(traineeValidator).validateUpdate(null);

            // Act
            Executable act = () -> service.updateTrainee(credentials, null);

            // Assert
            assertThrows(IllegalArgumentException.class, act);
            verifyNoInteractions(authorizationService, traineeRepository);
        }

        @Test
        void doesNotModifyProfileWhenNotOwner() {
            // Arrange
            UpdateTraineeDto dto = updateDto();
            when(authorizationService.requireTraineeOwner(credentials, USERNAME))
                    .thenThrow(new AccessDeniedException("Not authorized to modify this trainee"));

            // Act
            Executable act = () -> service.updateTrainee(credentials, dto);

            // Assert
            assertThrows(AccessDeniedException.class, act);
            assertEquals("James", trainee.getUser().getFirstName());
            verifyNoInteractions(traineeMapper, traineeRepository);
        }
    }

    @Nested
    class Lookups {

        @Test
        void returnsTraineeById() {
            // Arrange
            when(traineeRepository.findById(ID)).thenReturn(Optional.of(trainee));

            // Act
            Trainee result = service.getTraineeById(credentials, ID);

            // Assert
            assertSame(trainee, result);
            verify(authorizationService).requireTrainee(credentials);
        }

        @Test
        void throwsWhenIdUnknown() {
            // Arrange
            when(traineeRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Executable act = () -> service.getTraineeById(credentials, 999L);

            // Assert
            assertThrows(EntityNotFoundException.class, act);
        }

        @Test
        void authenticatesBeforeUsernameLookup() {
            // Arrange
            when(traineeRepository.findByUserName(USERNAME)).thenReturn(Optional.of(trainee));

            // Act
            Trainee result = service.findByUserName(credentials, USERNAME);

            // Assert
            assertSame(trainee, result);
            InOrder order = inOrder(authorizationService, traineeRepository);
            order.verify(authorizationService).requireTrainee(credentials);
            order.verify(traineeRepository).findByUserName(USERNAME);
        }

        @Test
        void blocksUsernameLookupWhenAuthenticationFails() {
            // Arrange
            when(authorizationService.requireTrainee(credentials))
                    .thenThrow(new AuthenticationFailedException());

            // Act
            Executable act = () -> service.findByUserName(credentials, USERNAME);

            // Assert
            assertThrows(AuthenticationFailedException.class, act);
            verifyNoInteractions(traineeRepository);
        }

        @Test
        void throwsWhenUsernameUnknown() {
            // Arrange
            when(traineeRepository.findByUserName(USERNAME)).thenReturn(Optional.empty());

            // Act
            Executable act = () -> service.findByUserName(credentials, USERNAME);

            // Assert
            assertThrows(EntityNotFoundException.class, act);
        }
    }

    @Nested
    class Deletion {

        @Test
        void deletesAuthorizedTrainee() {
            // Arrange
            when(authorizationService.requireTraineeOwner(credentials, USERNAME)).thenReturn(trainee);

            // Act
            service.removeTraineeByUsername(credentials, USERNAME);

            // Assert
            verify(traineeRepository).delete(trainee);
        }

        @Test
        void rejectsUnauthorizedDeletion() {
            // Arrange
            when(authorizationService.requireTraineeOwner(credentials, USERNAME))
                    .thenThrow(new AccessDeniedException("Not authorized to modify this trainee"));

            // Act
            Executable act = () -> service.removeTraineeByUsername(credentials, USERNAME);

            // Assert
            assertThrows(AccessDeniedException.class, act);
            verifyNoInteractions(traineeRepository);
        }

        @Test
        void detachesTrainersOnBothSidesBeforeDeleting() {
            // Arrange
            Trainer trainer = trainerWithId(2L);
            trainee.assignTrainer(trainer);
            when(authorizationService.requireTraineeOwner(credentials, USERNAME)).thenReturn(trainee);

            // Act
            service.removeTraineeByUsername(credentials, USERNAME);

            // Assert
            assertTrue(trainee.getTrainers().isEmpty());
            assertTrue(trainer.getTrainees().isEmpty());
            verify(traineeRepository).delete(trainee);
        }
    }

    @Nested
    class StateChanges {

        private Trainee detachedReference() {
            return Trainee.builder().id(ID).build();
        }

        @Test
        void activatesInactiveTrainee() {
            // Arrange
            trainee.getUser().setActive(false);
            when(traineeRepository.findById(ID)).thenReturn(Optional.of(trainee));

            // Act
            service.activateTrainee(credentials, detachedReference());

            // Assert

            InOrder order = inOrder(authorizationService, traineeRepository);
            order.verify(authorizationService).requireTrainee(credentials);
            order.verify(traineeRepository).findById(ID);
            order.verify(traineeRepository).activateTrainee(trainee);
        }

        @Test
        void rejectsActivationWhenAlreadyActive() {
            // Arrange
            when(traineeRepository.findById(ID)).thenReturn(Optional.of(trainee));

            // Act
            Executable act = () -> service.activateTrainee(credentials, detachedReference());

            // Assert
            assertThrows(IllegalStateException.class, act);
        }

        @Test
        void deactivatesActiveTrainee() {
            // Arrange
            when(traineeRepository.findById(ID)).thenReturn(Optional.of(trainee));

            // Act
            service.deactivateTrainee(credentials, detachedReference());

            // Assert
            InOrder order = inOrder(authorizationService, traineeRepository);
            order.verify(authorizationService).requireTrainee(credentials);
            order.verify(traineeRepository).findById(ID);
            order.verify(traineeRepository).deactivateTrainee(trainee);
        }

        @Test
        void rejectsDeactivationWhenAlreadyInactive() {
            // Arrange
            trainee.getUser().setActive(false);
            when(traineeRepository.findById(ID)).thenReturn(Optional.of(trainee));

            // Act
            Executable act = () -> service.deactivateTrainee(credentials, detachedReference());

            // Assert
            assertThrows(IllegalStateException.class, act);
        }

        @Test
        void rejectsNullTraineeOnActivation() {
            // Arrange

            // Act
            Executable act = () -> service.activateTrainee(credentials, null);

            // Assert
            assertThrows(NullPointerException.class, act);
            verifyNoInteractions(authorizationService, traineeRepository);
        }

        @Test
        void rejectsNullTraineeOnDeactivation() {
            // Arrange

            // Act
            Executable act = () -> service.deactivateTrainee(credentials, null);

            // Assert
            assertThrows(NullPointerException.class, act);
            verifyNoInteractions(authorizationService, traineeRepository);
        }

        @Test
        void rejectsUnknownTrainee() {
            // Arrange
            when(traineeRepository.findById(ID)).thenReturn(Optional.empty());

            // Act
            Executable act = () -> service.activateTrainee(credentials, detachedReference());

            // Assert
            assertThrows(EntityNotFoundException.class, act);
        }

        @Test
        void doesNotTouchRepositoryWhenAuthenticationFails() {
            // Arrange
            when(authorizationService.requireTrainee(credentials))
                    .thenThrow(new AuthenticationFailedException());

            // Act
            Executable act = () -> service.deactivateTrainee(credentials, detachedReference());

            // Assert
            assertThrows(AuthenticationFailedException.class, act);
            verifyNoInteractions(traineeRepository);
        }
    }

    @Nested
    class PasswordChanges {

        @Test
        void delegatesToCredentialServiceAfterAuthorization() {
            // Arrange
            ChangePasswordDto dto = new ChangePasswordDto(RAW_PASSWORD, NEW_PASSWORD);

            // Act
            service.changePassword(credentials, dto);

            // Assert
            InOrder order = inOrder(authorizationService, credentialService);
            order.verify(authorizationService).requireTrainee(credentials);
            order.verify(credentialService).changePassword(credentials, dto);
        }

        @Test
        void doesNotChangePasswordIfAuthorizationFails() {
            // Arrange
            ChangePasswordDto dto = new ChangePasswordDto(RAW_PASSWORD, NEW_PASSWORD);
            when(authorizationService.requireTrainee(credentials))
                    .thenThrow(new AuthenticationFailedException());

            // Act
            Executable act = () -> service.changePassword(credentials, dto);

            // Assert
            assertThrows(AuthenticationFailedException.class, act);
            verifyNoInteractions(credentialService);
        }
    }

    @Nested
    class TrainerAssignments {

        @Test
        void addsRequestedTrainer() {
            // Arrange
            Trainer trainer = trainerWithId(2L);
            when(authorizationService.requireTraineeOwner(credentials, USERNAME)).thenReturn(trainee);
            when(trainerRepository.findById(2L)).thenReturn(Optional.of(trainer));

            // Act
            Set<Trainer> result = service.replaceTrainers(credentials,
                    new UpdateTraineeTrainersDto(USERNAME, Set.of(2L)));

            // Assert
            assertEquals(Set.of(trainer), result);
            assertTrue(trainer.getTrainees().contains(trainee));
        }

        @Test
        void removesExistingTrainerWhenRequestedSetEmpty() {
            // Arrange
            Trainer trainer = trainerWithId(2L);
            trainee.assignTrainer(trainer);
            when(authorizationService.requireTraineeOwner(credentials, USERNAME)).thenReturn(trainee);

            // Act
            Set<Trainer> result = service.replaceTrainers(credentials,
                    new UpdateTraineeTrainersDto(USERNAME, Set.of()));

            // Assert
            assertTrue(result.isEmpty());
            assertFalse(trainer.getTrainees().contains(trainee));
        }

        @Test
        void keepsTrainerThatRemainsAssigned() {
            // Arrange
            Trainer trainer = trainerWithId(2L);
            trainee.assignTrainer(trainer);
            when(authorizationService.requireTraineeOwner(credentials, USERNAME)).thenReturn(trainee);
            when(trainerRepository.findById(2L)).thenReturn(Optional.of(trainer));

            // Act
            Set<Trainer> result = service.replaceTrainers(credentials,
                    new UpdateTraineeTrainersDto(USERNAME, Set.of(2L)));

            // Assert
            assertEquals(Set.of(trainer), result);
            assertTrue(trainer.getTrainees().contains(trainee));
        }

        @Test
        void swapsOneTrainerForAnother() {
            // Arrange
            Trainer previous = trainerWithId(2L);
            Trainer replacement = trainerWithId(3L);
            trainee.assignTrainer(previous);
            when(authorizationService.requireTraineeOwner(credentials, USERNAME)).thenReturn(trainee);
            when(trainerRepository.findById(3L)).thenReturn(Optional.of(replacement));

            // Act
            Set<Trainer> result = service.replaceTrainers(credentials,
                    new UpdateTraineeTrainersDto(USERNAME, Set.of(3L)));

            // Assert
            assertEquals(Set.of(replacement), result);
            assertFalse(previous.getTrainees().contains(trainee));
            assertTrue(replacement.getTrainees().contains(trainee));
        }

        @Test
        void rejectsNullDto() {
            // Arrange

            // Act
            Executable act = () -> service.replaceTrainers(credentials, null);

            // Assert
            assertThrows(NullPointerException.class, act);
            verifyNoInteractions(authorizationService, trainerRepository);
        }

        @Test
        void rejectsNullTrainerIds() {
            // Arrange
            UpdateTraineeTrainersDto dto = new UpdateTraineeTrainersDto(USERNAME, null);

            // Act
            Executable act = () -> service.replaceTrainers(credentials, dto);

            // Assert
            assertThrows(IllegalArgumentException.class, act);
            verifyNoInteractions(authorizationService, trainerRepository);
        }

        @Test
        void rejectsBlankUsername() {
            // Arrange
            UpdateTraineeTrainersDto dto = new UpdateTraineeTrainersDto(" ", Set.of(2L));

            // Act
            Executable act = () -> service.replaceTrainers(credentials, dto);

            // Assert
            assertThrows(IllegalArgumentException.class, act);
            verifyNoInteractions(authorizationService, trainerRepository);
        }

        @Test
        void rejectsNonPositiveTrainerId() {
            // Arrange
            UpdateTraineeTrainersDto dto = new UpdateTraineeTrainersDto(USERNAME, Set.of(0L));

            // Act
            Executable act = () -> service.replaceTrainers(credentials, dto);

            // Assert
            assertThrows(IllegalArgumentException.class, act);
            verifyNoInteractions(authorizationService, trainerRepository);
        }

        @Test
        void keepsExistingAssignmentsWhenRequestedTrainerUnknown() {
            // Arrange
            Trainer existing = trainerWithId(3L);
            trainee.assignTrainer(existing);
            when(authorizationService.requireTraineeOwner(credentials, USERNAME)).thenReturn(trainee);
            when(trainerRepository.findById(99L)).thenReturn(Optional.empty());

            // Act
            Executable act = () -> service.replaceTrainers(credentials,
                    new UpdateTraineeTrainersDto(USERNAME, Set.of(99L)));

            // Assert
            assertThrows(EntityNotFoundException.class, act);
            assertEquals(Set.of(existing), trainee.getTrainers());
        }

        @Test
        void rejectsUnauthorizedAssignment() {
            // Arrange
            when(authorizationService.requireTraineeOwner(credentials, USERNAME))
                    .thenThrow(new AccessDeniedException("Not authorized to modify this trainee"));

            // Act
            Executable act = () -> service.replaceTrainers(credentials,
                    new UpdateTraineeTrainersDto(USERNAME, Set.of(2L)));

            // Assert
            assertThrows(AccessDeniedException.class, act);
            verifyNoInteractions(trainerRepository);
        }
    }
}