package com.exam_paper.backend.service;

import com.exam_paper.backend.entity.User;
import com.exam_paper.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public boolean login(String username, String password){
        Optional<User> userOpt = userRepository.findByUsername(username);

        if(userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println(">>> User found in DB! Username: " + user.getUsername());
            System.out.println(">>> DB Password: " + user.getPassword());

            boolean isMatch = user.getPassword().equals(password);
            System.out.println(">>> Password Match Status: " + isMatch);
            return isMatch;
            //return user.getPassword().equals(password);
        }
        System.out.println(">>> User NOT found in DB for username: " + username);
        return false;
    }
}
