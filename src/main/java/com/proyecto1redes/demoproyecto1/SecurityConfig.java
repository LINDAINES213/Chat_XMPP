package com.proyecto1redes.demoproyecto1;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().permitAll() // Permite acceso a todas las rutas sin autenticación
            )
            .csrf(csrf -> csrf.disable()) // Desactiva CSRF para simplificar

            .logout(logout -> logout
                .logoutUrl("/perform_logout")
                .logoutSuccessUrl("/") // Redirige a la página principal tras el logout
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
                
        );


        return http.build();
    }
}
