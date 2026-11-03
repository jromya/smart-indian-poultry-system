package com.poultry.smartfarm.user.security;

import com.poultry.smartfarm.user.document.User;
import com.poultry.smartfarm.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        String trimmed = identifier == null ? "" : identifier.trim();
        User user = userRepository.findByUsernameIgnoreCase(trimmed)
                .or(() -> userRepository.findByPhone(trimmed))
                .or(() -> userRepository.findByEmailIgnoreCase(trimmed))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username/phone/email: " + trimmed));
        return new CustomUserDetails(user);
    }
}
