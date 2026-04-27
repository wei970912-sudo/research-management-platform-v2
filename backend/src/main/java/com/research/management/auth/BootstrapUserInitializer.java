package com.research.management.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BootstrapUserInitializer {

    @Bean
    ApplicationRunner initializeUsers(
            AppUserRepository repository,
            PasswordEncoder passwordEncoder,
            @Value("${app.auth.bootstrap.admin.username:admin}") String adminUsername,
            @Value("${app.auth.bootstrap.admin.password:Admin@123456}") String adminPassword,
            @Value("${app.auth.bootstrap.admin.display-name:系统管理员}") String adminDisplayName,
            @Value("${app.auth.bootstrap.viewer.username:viewer}") String viewerUsername,
            @Value("${app.auth.bootstrap.viewer.password:Viewer@123456}") String viewerPassword,
            @Value("${app.auth.bootstrap.viewer.display-name:只读查看员}") String viewerDisplayName
    ) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }

            AppUserEntity admin = new AppUserEntity();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole("ADMIN");
            admin.setDisplayName(adminDisplayName);

            AppUserEntity viewer = new AppUserEntity();
            viewer.setUsername(viewerUsername);
            viewer.setPassword(passwordEncoder.encode(viewerPassword));
            viewer.setRole("VIEWER");
            viewer.setDisplayName(viewerDisplayName);

            repository.save(admin);
            repository.save(viewer);
        };
    }
}
