package jp.ac.example.employeemanagement.service;

import jp.ac.example.employeemanagement.domain.AppUser;
import jp.ac.example.employeemanagement.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DemoUserInitializer implements ApplicationRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;
    private final String adminUsername;
    private final String adminPassword;
    private final String viewerUsername;
    private final String viewerPassword;

    public DemoUserInitializer(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder,
                               @Value("${app.demo-users.enabled:false}") boolean enabled,
                               @Value("${app.demo-users.admin-username:admin}") String adminUsername,
                               @Value("${app.demo-users.admin-password:}") String adminPassword,
                               @Value("${app.demo-users.viewer-username:viewer}") String viewerUsername,
                               @Value("${app.demo-users.viewer-password:}") String viewerPassword) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.viewerUsername = viewerUsername;
        this.viewerPassword = viewerPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        requirePassword(adminPassword, "APP_DEMO_ADMIN_PASSWORD");
        requirePassword(viewerPassword, "APP_DEMO_VIEWER_PASSWORD");
        if (adminUsername.equals(viewerUsername)) {
            throw new IllegalStateException("ADMIN と VIEWER のユーザー名は別々に設定してください。");
        }
        createUserIfMissing(adminUsername, adminPassword, "ADMIN");
        createUserIfMissing(viewerUsername, viewerPassword, "VIEWER");
    }

    private void createUserIfMissing(String username, String password, String role) {
        if (appUserRepository.findByUsername(username).isPresent()) {
            return;
        }
        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setPasswordHash(passwordEncoder.encode(password));
        appUser.setRole(role);
        appUser.setEnabled(true);
        appUserRepository.save(appUser);
    }

    private void requirePassword(String password, String variableName) {
        if (password == null || password.length() < 12) {
            throw new IllegalStateException(variableName + " を12文字以上で設定してください。");
        }
    }
}
