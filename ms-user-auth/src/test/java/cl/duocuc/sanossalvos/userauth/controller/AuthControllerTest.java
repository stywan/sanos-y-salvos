package cl.duocuc.sanossalvos.userauth.controller;

import cl.duocuc.sanossalvos.userauth.dto.AuthResponse;
import cl.duocuc.sanossalvos.userauth.dto.LoginRequest;
import cl.duocuc.sanossalvos.userauth.dto.RegisterRequest;
import cl.duocuc.sanossalvos.userauth.dto.UserInfoResponse;
import cl.duocuc.sanossalvos.userauth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock  AuthService    authService;
    @InjectMocks AuthController controller;

    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        authResponse = AuthResponse.builder()
                .token("mock.jwt.token")
                .email("user@test.cl")
                .tipoUsuario("PERSONA")
                .nombreDisplay("Juan Pérez")
                .roles(List.of("ROLE_USER"))
                .build();
    }

    @Test
    @DisplayName("register: delega al servicio y retorna 201")
    void register_returns201() {
        RegisterRequest req = RegisterRequest.builder()
                .email("user@test.cl").password("pass123").tipoUsuario("PERSONA").build();
        when(authService.register(req)).thenReturn(authResponse);

        ResponseEntity<AuthResponse> resp = controller.register(req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().getToken()).isEqualTo("mock.jwt.token");
        verify(authService).register(req);
    }

    @Test
    @DisplayName("login: delega al servicio y retorna 200 con token")
    void login_returns200WithToken() {
        LoginRequest req = new LoginRequest("user@test.cl", "pass123");
        when(authService.login(req)).thenReturn(authResponse);

        ResponseEntity<AuthResponse> resp = controller.login(req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getEmail()).isEqualTo("user@test.cl");
        verify(authService).login(req);
    }

    @Test
    @DisplayName("me: retorna información del usuario autenticado")
    void me_returnsUserInfo() {
        UserDetails userDetails = new User("user@test.cl", "pass", List.of());
        UserInfoResponse userInfo = UserInfoResponse.builder()
                .id(1L).email("user@test.cl").tipoUsuario("PERSONA").build();
        when(authService.getMe("user@test.cl")).thenReturn(userInfo);

        ResponseEntity<UserInfoResponse> resp = controller.me(userDetails);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getEmail()).isEqualTo("user@test.cl");
        verify(authService).getMe("user@test.cl");
    }
}
