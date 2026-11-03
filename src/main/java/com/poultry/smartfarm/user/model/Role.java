package com.poultry.smartfarm.user.model;

public enum Role {
    SUPER_ADMIN("Super Admin"),
    FARM_ADMIN("Farm Admin"),
    FARM_MANAGER("Farm Manager"),
    FARMER("Farmer"),
    RETAILER("Retailer"),
    CHICKEN_SHOP_OWNER("Chicken Shop Owner");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isFarmRole() {
        return this == SUPER_ADMIN || this == FARM_ADMIN || this == FARM_MANAGER || this == FARMER;
    }
}
