package com.poultry.smartfarm.user.repository;

import com.poultry.smartfarm.user.document.User;
import com.poultry.smartfarm.user.model.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByPhone(String phone);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByPhone(String phone);

    boolean existsByEmailIgnoreCase(String email);

    List<User> findByRole(Role role);

    List<User> findByAssignedFarmIdsContaining(String farmId);

    List<User> findByAssignedFarmIdsIn(List<String> farmIds);
}
