package com.training.gym.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @NotBlank
    @Size(max = 150)
    @Column(name = "username", nullable = false,
            unique = true, length = 150)
    private String userName;

    @NotBlank
    @Column(name = "password", nullable = false)
    private String password;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;

    public void activate() {
        if (active) {
            throw new IllegalStateException("Profile is already active");
        }

        active = true;
    }

    public void deactivate() {
        if (!active) {
            throw new IllegalStateException("Profile is already inactive");
        }

        active = false;
    }

}