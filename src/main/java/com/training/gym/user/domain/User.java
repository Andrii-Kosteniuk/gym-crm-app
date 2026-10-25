package com.training.gym.user.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public abstract class User {

    private Long id;
    private String firsName;
    private String lastName;
    private String userName;
    private String password;
    private boolean active;
}
