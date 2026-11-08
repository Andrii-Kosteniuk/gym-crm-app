package com.training.gym.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Address {

    @NotBlank
    @Column(name = "city", length = 100)
    private String city;

    @NotBlank
    @Column(name = "street", length = 150)
    private String street;

    @Positive
    @Column(name = "building_number")
    private Integer buildingNumber;

    @NotBlank
    @Column(name = "zip_code", length = 20)
    private String zipCode;
}