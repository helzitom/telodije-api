package pe.telodije.api.identidad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwt;

    public JwtAuthenticationFilter(JwtService jwt) {
        this.jwt = jwt;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String cabecera = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera != null && cabecera.startsWith(PREFIJO)) {
            try {
                Claims claims = jwt.validar(cabecera.substring(PREFIJO.length()));
                String rol = claims.get("rol", String.class);
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                claims.getSubject(),
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + rol))));
            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(req, res);
    }
}