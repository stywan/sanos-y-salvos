package cl.duocuc.sanossalvos.userauth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    @NotBlank(message = "El tipo de usuario es obligatorio")
    private String tipoUsuario; // PERSONA | VETERINARIA | REFUGIO | MUNICIPALIDAD

    // ── Campos para PERSONA ──────────────────────────
    private String nombre;
    private String apellido;

    // ── Campos compartidos (PERSONA y ORGANIZACION) ──
    private String telefono;

    // ── Campos para ORGANIZACION ─────────────────────
    private String nombreOrganizacion;
    private String descripcion;
    private String direccion;
}
