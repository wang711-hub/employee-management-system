package jp.ac.example.employeemanagement.config;

import jp.ac.example.employeemanagement.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                    UserDetailsServiceImpl userDetailsService) throws Exception {
        RequestMatcher apiRequests = new AntPathRequestMatcher("/api/**");
        http
                .userDetailsService(userDetailsService)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/webjars/**", "/css/**", "/js/**", "/favicon.ico").permitAll()
                        .requestMatchers(HttpMethod.GET, "/employees/new", "/employees/*/edit").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/employees", "/employees/*/edit", "/employees/*/delete",
                                "/departments", "/departments/*/edit", "/departments/*/delete").hasRole("ADMIN")
                        .requestMatchers("/employees", "/departments").authenticated()
                        .requestMatchers("/api/ai/query").authenticated()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/login?logout"))
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), apiRequests));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
