package com.bnr.stockmanagement.config;

import com.bnr.stockmanagement.entity.User;
import com.bnr.stockmanagement.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SystemAdminInitializer {

    @Bean
    CommandLineRunner initSystemAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. Ensure System Admin exists
            User sysAdmin = userRepository.findByUsername("sysadmin").orElse(null);
            if (sysAdmin == null) {
                sysAdmin = User.builder()
                        .username("sysadmin")
                        .email("sysadmin@bnr.rw")
                        .role(User.Role.system_admin)
                        .build();
            }
            sysAdmin.setPassword(passwordEncoder.encode("password123"));
            sysAdmin.setFullName("System Administrator");
            userRepository.save(sysAdmin);

            System.out.println("System Admin Initialized.");
        };
    }
}
