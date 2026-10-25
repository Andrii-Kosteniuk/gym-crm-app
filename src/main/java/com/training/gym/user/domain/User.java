package com.training.gym.user.domain;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public abstract class User {

    private Long id;
    private String firsName;
    private String lastName;
    private String userName;
    private String password;
    private boolean active;


    @Override
    public String toString() {
        return """
                id: '%d'
                firstName: '%s',
                lastName: '%s',
                username: '%s',
                active: %s
                """.formatted(
                id,
                firsName,
                lastName,
                userName,
                active
        );
    }
}
