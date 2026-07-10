package com.certificate.platform.service;

import com.certificate.platform.dto.RegisterRequest;
import com.certificate.platform.model.User;
import com.certificate.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void registerIssuer(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setOrganizationName(request.getOrganizationName());
        user.setDesignation(request.getDesignation());
        user.setRole(User.Role.ISSUER);
        user.setStatus(User.Status.PENDING);
        userRepository.save(user);
    }

    public List<User> getAllIssuers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.ISSUER)
                .toList();
    }

    public void approveIssuer(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(User.Status.APPROVED);
        userRepository.save(user);
    }

    public void rejectIssuer(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(User.Status.REJECTED);
        userRepository.save(user);
    }
}