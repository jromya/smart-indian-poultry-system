package com.poultry.smartfarm.user.dto;

import com.poultry.smartfarm.user.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class UserCreateUpdateRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100)
    private String fullName;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 40)
    private String username;

    @Email(message = "Valid email required")
    private String email;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^(\\+91[-\\s]?)?[6-9]\\d{9}$", message = "Enter a valid 10-digit Indian mobile number")
    private String phone;

    private String password;

    @NotNull(message = "Role is required")
    private Role role;

    private String preferredLanguage = "en";
    private List<String> assignedFarmIds = new ArrayList<>();
    private String businessName;
    private String villageOrCity;
    private String district;
    private String state;
    private String pincode;
    private Boolean active = true;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public List<String> getAssignedFarmIds() {
        return assignedFarmIds;
    }

    public void setAssignedFarmIds(List<String> assignedFarmIds) {
        this.assignedFarmIds = assignedFarmIds != null ? assignedFarmIds : new ArrayList<>();
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getVillageOrCity() {
        return villageOrCity;
    }

    public void setVillageOrCity(String villageOrCity) {
        this.villageOrCity = villageOrCity;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
