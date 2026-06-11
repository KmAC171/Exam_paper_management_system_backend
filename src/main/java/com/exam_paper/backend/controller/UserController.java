package com.exam_paper.backend.controller;

import com.exam_paper.backend.dto.UserDTO;
import com.exam_paper.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserDTO loginRequest) {

        boolean isAuthenticated =
                userService.login(
                        loginRequest.getUsername(),
                        loginRequest.getPassword());

        if (isAuthenticated) {
            return ResponseEntity.ok("Login Successful");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid Username or Password!");
        }

    }
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserDTO registerRequest) {
        userService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }
}
