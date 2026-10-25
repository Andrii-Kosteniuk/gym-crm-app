package com.training.gym.trainee.domain;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Address {

    private String city;
    private String street;
    private int buildNumber;
    private String zipcode;

    @Override
    public String toString() {
        return """
                city: '%s'
                street: '%s',
                number: '%d',
                zipCode: '%s',
                """.trim().formatted(
                city,
                street,
                buildNumber,
                zipcode
        );
    }
}
