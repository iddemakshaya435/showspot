package com.eventbooking.service;

import com.eventbooking.dto.AuthRequest;
import com.eventbooking.dto.AuthResponse;
import com.eventbooking.dto.RegisterRequest;
import com.eventbooking.entity.User;
import com.eventbooking.repository.UserRepository;
import com.eventbooking.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    public AuthResponse authenticateUser(AuthRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication.getPrincipal());

        User user = (User) authentication.getPrincipal();
        
        return new AuthResponse(jwt, user.getId(), user.getEmail(), 
                               user.getFirstName(), user.getLastName(), user.getRole().name());
    }

    public AuthResponse registerUser(RegisterRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        User user = new User(signUpRequest.getFirstName(),
                           signUpRequest.getLastName(),
                           signUpRequest.getEmail(),
                           encoder.encode(signUpRequest.getPassword()));

        user.setPhoneNumber(signUpRequest.getPhoneNumber());
        user = userRepository.save(user);

        String jwt = jwtUtils.generateTokenFromUsername(user.getEmail());
        
        return new AuthResponse(jwt, user.getId(), user.getEmail(),
                               user.getFirstName(), user.getLastName(), user.getRole().name());
    }

    public AuthResponse refreshToken(String token) {
        String jwt = token.substring(7); // Remove "Bearer " prefix
        String username = jwtUtils.getUserNameFromJwtToken(jwt);
        
        if (jwtUtils.validateJwtToken(jwt)) {
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            String newJwt = jwtUtils.generateTokenFromUsername(username);
            
            return new AuthResponse(newJwt, user.getId(), user.getEmail(),
                                   user.getFirstName(), user.getLastName(), user.getRole().name());
        }
        
        throw new RuntimeException("Invalid token");
    }
}