package com.SolicitudTraslado.config;

// --- Imports necesarios ---
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
// --- Fin de Imports ---

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Habilita @PreAuthorize
public class SecurityConfig {

    // Inyectamos la URL del JWKS desde el application.properties
    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    // --- ¡CAMBIO AQUÍ! ---
    // Inyectamos nuestro JwtDecoder personalizado en el método
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // Endpoints públicos de Swagger y health
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                .requestMatchers("/actuator/health").permitAll()
                // Endpoint público para buscar por nombre (de CiudadController)
                .requestMatchers("/api/solicitudes/ciudades/search").permitAll()
                // Todas las demás peticiones requieren autenticación
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                // Spring usará nuestro bean 'jwtDecoder()' personalizado
                .jwt(jwt -> jwt
                    // --- ¡CAMBIO AQUÍ! ---
                    // Se lo pasamos explícitamente
                    .decoder(jwtDecoder) 
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF para APIs stateless
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Política de sesión sin estado
            );

        return http.build();
    }

    /**
     * BEAN CORREGIDO: Arreglo para el 401 (Issuer Mismatch).
     * Valida el token usando la firma (de jwkSetUri), pero NO el 'issuer'.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        // Validamos solo la fecha de expiración, no el 'issuer'
        jwtDecoder.setJwtValidator(new JwtTimestampValidator());
        return jwtDecoder;
    }

    /**
     * Conversor de roles (para leer "realm_access.roles" de Keycloak).
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        Converter<Jwt, Collection<GrantedAuthority>> authoritiesConverter = jwt -> {
            // Obtenemos el claim "realm_access"
            Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaims().get("realm_access");

            if (realmAccess == null || realmAccess.isEmpty()) {
                return List.of(); // Devuelve una lista vacía si no hay roles
            }

            // Obtenemos la lista de "roles"
            Collection<String> roles = (Collection<String>) realmAccess.get("roles");

            // Mapeamos cada rol a un SimpleGrantedAuthority con el prefijo "ROLE_"
            return roles.stream()
                    .map(roleName -> "ROLE_" + roleName.toUpperCase())
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        };

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }
}