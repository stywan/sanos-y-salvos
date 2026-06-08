package cl.duocuc.sanossalvos.petmanagement.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FotoServiceTest {

    @Mock MinioClient minioClient;
    @InjectMocks FotoService fotoService;

    @BeforeEach
    void setUp() throws Exception {
        // Inyectar los @Value fields via reflexión (no hay Spring context)
        setField("minioUrl", "http://localhost:9000");
        setField("bucket", "mascotas");
    }

    private void setField(String fieldName, String value) throws Exception {
        Field f = FotoService.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(fotoService, value);
    }

    // ── contentType inválido ──────────────────────────────────────────────────

    @Test
    @DisplayName("subirFoto: contentType null → IllegalArgumentException")
    void subirFoto_contentTypeNull_throwsException() {
        MultipartFile file = new MockMultipartFile("foto", "foto.jpg", null, "data".getBytes());

        assertThatThrownBy(() -> fotoService.subirFoto(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Solo se aceptan");
    }

    @Test
    @DisplayName("subirFoto: tipo no permitido → IllegalArgumentException")
    void subirFoto_tipoNoPermitido_throwsException() {
        MultipartFile file = new MockMultipartFile("foto", "foto.gif", "image/gif", "data".getBytes());

        assertThatThrownBy(() -> fotoService.subirFoto(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Solo se aceptan");
    }

    // ── tamaño excedido ───────────────────────────────────────────────────────

    @Test
    @DisplayName("subirFoto: tamaño > 5 MB → IllegalArgumentException")
    void subirFoto_tamanoExcedido_throwsException() {
        byte[] bigContent = new byte[(int) (6L * 1024 * 1024)];
        MultipartFile file = new MockMultipartFile("foto", "foto.jpg", "image/jpeg", bigContent);

        assertThatThrownBy(() -> fotoService.subirFoto(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("5 MB");
    }

    // ── subida exitosa ────────────────────────────────────────────────────────

    @Test
    @DisplayName("subirFoto: JPEG válido → retorna URL con extensión .jpg")
    void subirFoto_jpeg_retornaUrlConJpg() throws Exception {
        MultipartFile file = new MockMultipartFile("foto", "foto.jpg", "image/jpeg", "data".getBytes());
        // putObject retorna ObjectWriteResponse; Mockito retorna null por defecto (no-op)
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        String url = fotoService.subirFoto(file);

        assertThat(url).startsWith("http://localhost:9000/mascotas/").endsWith(".jpg");
    }

    @Test
    @DisplayName("subirFoto: PNG válido → retorna URL con extensión .png")
    void subirFoto_png_retornaUrlConPng() throws Exception {
        MultipartFile file = new MockMultipartFile("foto", "foto.png", "image/png", "data".getBytes());
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        String url = fotoService.subirFoto(file);

        assertThat(url).startsWith("http://localhost:9000/mascotas/").endsWith(".png");
    }

    // ── error en MinIO ────────────────────────────────────────────────────────

    @Test
    @DisplayName("subirFoto: error en MinIO → RuntimeException con mensaje de error")
    void subirFoto_minioFalla_throwsRuntimeException() throws Exception {
        MultipartFile file = new MockMultipartFile("foto", "foto.jpg", "image/jpeg", "data".getBytes());
        doThrow(new RuntimeException("MinIO no disponible")).when(minioClient).putObject(any());

        assertThatThrownBy(() -> fotoService.subirFoto(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error al subir");
    }
}
