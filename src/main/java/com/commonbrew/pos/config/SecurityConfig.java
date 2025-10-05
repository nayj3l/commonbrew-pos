package com.commonbrew.pos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Authorize requests - static resources + login page must be public
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/css/**", "/js/**", "/images/**", "/webjars/**", // static
                    "/login", "/h2-console/**", "/error" // public endpoints
                ).permitAll()
                .anyRequest().authenticated()
            )

            // Custom form login
            .formLogin(form -> form
                .loginPage("/login")                 // show custom login page
                .loginProcessingUrl("/login")        // POST target for form submit
                .defaultSuccessUrl("/", true)        // after login
                .failureUrl("/login?error=true")
                .permitAll()
            )

            // Logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST")) // explicit
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )

            // Allow H2 console frames (if using H2 in dev)
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

            // CSRF: keep enabled by default; if you need to ignore H2 console:
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**"))
            );

        return http.build();
    }

    // Simple in-memory user for testing (remove/replace with real user service in production)
    @Bean
    public UserDetailsService users(PasswordEncoder encoder) {
        var user = User.withUsername("admin")
                       .password(encoder.encode("admin"))
                       .roles("ADMIN")
                       .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
