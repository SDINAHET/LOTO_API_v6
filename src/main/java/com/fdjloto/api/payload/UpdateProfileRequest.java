package com.fdjloto.api.payload;

import jakarta.validation.constraints.NotBlank;

public class UpdateProfileRequest {
    private String firstName;
    private String lastName;

    @NotBlank
    private String currentPassword;

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
}
