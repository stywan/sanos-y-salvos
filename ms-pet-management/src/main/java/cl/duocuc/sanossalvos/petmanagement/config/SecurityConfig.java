package cl.duocuc.sanossalvos.petmanagement.config;

import cl.duocuc.sanossalvos.petmanagement.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Catálogos públicos (para llenar dropdowns sin login)
                .requestMatchers(HttpMethod.GET, "/api/pets/especies/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/pets/razas/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/pets/colores/**").permitAll()
                // Listado y detalle de reportes público (para que cualquiera pueda buscar)
                .requestMatchers(HttpMethod.GET, "/api/pets/reportes/**").permitAll()
                // Actuator
                .requestMatchers("/actuator/**").permitAll()
                // Todo lo demás requiere autenticación
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
