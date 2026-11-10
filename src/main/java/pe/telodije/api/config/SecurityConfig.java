package pe.telodije.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pe.telodije.api.identidad.JwtAuthenticationFilter;
import pe.telodije.api.identidad.JwtService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    //Método que retorna la contraseña condificada con ayuda de Spring Security
    @Bean
    PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    //Método para poder filtrar los usuarios y manejar la seguridad de las solicitudes HTTP
    //Se recibe la solicitud http y el jwt ayudan a deifnir las reglas de seguridad de la aplicación
    // Sin un usuario intenta ingresas sin los permisos se necesarios se genera una excepción
    @Bean
    SecurityFilterChain filtros(HttpSecurity http, JwtService jwt) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/actuator/health", "/error",
                                "/api/v1/alojamientos/cercanos",
                                "/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "MODERADOR")
                        .requestMatchers("/actuator/health", "/error",
                                "/api/v1/alojamientos/cercanos",
                                "/api/v1/alojamientos/*/resenas",
                                "/api/v1/auth/**",
                                "/api/v1/concurrencia",
                                "/api/v1/alojamientos/*/detalle",
                                "/api/v1/servicios")
                                .permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthenticationFilter(jwt),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}