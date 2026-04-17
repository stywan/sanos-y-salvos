package cl.duocuc.sanossalvos.userauth.controller;

import cl.duocuc.sanossalvos.userauth.dto.AuthResponse;
import cl.duocuc.sanossalvos.userauth.dto.LoginRequest;
import cl.duocuc.sanossalvos.userauth.dto.RegisterRequest;
import cl.duocuc.sanossalvos.userauth.dto.UserInfoResponse;
import cl.duocuc.sanossalvos.userauth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registro de nuevo usuario.
     * Público — no requiere token.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /**
     * Login — retorna JWT.
     * Público — no requiere token.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Datos del usuario autenticado.
     * Requiere: Authorization: Bearer <token>
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(authService.getMe(userDetails.getUsername()));
    }
}
