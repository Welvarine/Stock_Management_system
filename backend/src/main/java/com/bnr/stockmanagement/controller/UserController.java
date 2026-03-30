package com.bnr.stockmanagement.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.bnr.stockmanagement.entity.User;
import com.bnr.stockmanagement.repository.UserRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @GetMapping("/")
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public User findOne(@PathVariable Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @PostMapping("/")
    public User create(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @PostMapping("/login")
    public User login(@RequestBody User loginUser) {
        // Find user by username
        User user = userRepository.findByUsername(loginUser.getUsername()).orElse(null);

        if (user == null) {
            throw new RuntimeException("Invalid username or password");
        }

        // Main path: BCrypt hash comparison.
        if (passwordEncoder.matches(loginUser.getPassword(), user.getPassword())) {
            return user;
        }

        // Compatibility path for legacy plain-text users: allow once, then upgrade.
        if (user.getPassword().equals(loginUser.getPassword())) {
            user.setPassword(passwordEncoder.encode(loginUser.getPassword()));
            userRepository.save(user);
            return user;
        }

        throw new RuntimeException("Invalid username or password");
    }

    @DeleteMapping("/{id}")
    public Integer destroy(@PathVariable Long id){
        userRepository.deleteById(id);
        return (int) (long) id;
    }
}
