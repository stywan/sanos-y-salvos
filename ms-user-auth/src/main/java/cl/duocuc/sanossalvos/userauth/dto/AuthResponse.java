package cl.duocuc.sanossalvos.userauth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;

    @Builder.Default
    private String tipo = "Bearer";

    private Long usuarioId;
    private String email;
    private String tipoUsuario;
    private String nombreDisplay;
    private List<String> roles;
}
