package org.example.banksystem.service;

import org.example.banksystem.dto.UserResponse;
import org.example.banksystem.entity.User;
import org.example.banksystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements UserDetailsService,
        ServiceInterface<JpaRepository<User, String>, User, String> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserRepository getRepo() {
        return userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username).orElse(null);
    }

    public UserResponse parseUser(User user) {
        return new UserResponse(
                user.getUsername(),
                user.getPassword(),
                user.getRole().name()
        );
    }
}
