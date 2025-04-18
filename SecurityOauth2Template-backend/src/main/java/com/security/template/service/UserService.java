package com.security.template.service;

import com.security.template.model.Role;
import com.security.template.model.User;
import com.security.template.model.UserRole;
import com.security.template.repository.RoleRepository;
import com.security.template.repository.UserRepository;
import com.security.template.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(User user, String roleName) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        Role role = roleRepository.findByName(roleName);
        if (role == null) {
            //Create role if it does not exist.
            role = new Role();
            role.setName(roleName);
            role = roleRepository.save(role);
        }

        UserRole userRole = new UserRole();
        userRole.setUser(savedUser);
        userRole.setRole(role);
        userRoleRepository.save(userRole);

        return savedUser;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}
