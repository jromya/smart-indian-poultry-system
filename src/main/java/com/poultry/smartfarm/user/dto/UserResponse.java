package com.poultry.smartfarm.user.dto;

import com.poultry.smartfarm.user.document.User;
import com.poultry.smartfarm.user.model.Role;

import java.time.LocalDateTime;
import java.util.List;

public class UserResponse {

    private String id;
    private String fullName;
    private String username;
    private String email;
    private String phone;
    private Role role;
    private String roleDisplayName;
    private String preferredLanguage;
    private List<String> assignedFarmIds;
    private String businessName;
    private String villageOrCity;
    private String district;
    private String state;
    private String pincode;
    private boolean active;
    private LocalDateTime createdAt;

    public static UserResponse fromDocument(User user) {
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setRoleDisplayName(user.getRole() != null ? user.getRole().getDisplayName() : "");
        dto.setPreferredLanguage(user.getPreferredLanguage());
        dto.setAssignedFarmIds(user.getAssignedFarmIds());
        dto.setBusinessName(user.getBusinessName());
        dto.setVillageOrCity(user.getVillageOrCity());
        dto.setDistrict(user.getDistrict());
        dto.setState(user.getState());
        dto.setPincode(user.getPincode());
        dto.setActive(user.isActive());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getRoleDisplayName() {
        return roleDisplayName;
    }

    public void setRoleDisplayName(String roleDisplayName) {
        this.roleDisplayName = roleDisplayName;
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
        this.assignedFarmIds = assignedFarmIds;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
