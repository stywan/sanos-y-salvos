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
public class UserInfoResponse {

    private Long id;
    private String email;
    private String tipoUsuario;
    private String nombreDisplay;
    private List<String> roles;

    // Perfil persona
    private String nombre;
    private String apellido;

    // Perfil organización
    private String nombreOrganizacion;
    private String descripcion;
    private String direccion;

    // Compartido
    private String telefono;
}
