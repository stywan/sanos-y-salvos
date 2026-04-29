package cl.duocuc.sanossalvos.petmanagement.controller;

import cl.duocuc.sanossalvos.petmanagement.dto.FotoUploadResponse;
import cl.duocuc.sanossalvos.petmanagement.service.FotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/pets/fotos")
@RequiredArgsConstructor
public class FotoController {

    private final FotoService fotoService;

    /**
     * Sube una imagen a MinIO y devuelve su URL pública.
     * El frontend sube cada foto antes de crear el reporte,
     * y luego envía las URLs en CrearReporteRequest.fotosUrls.
     */
    @PostMapping("/upload")
    public ResponseEntity<FotoUploadResponse> upload(
            @RequestParam("file") MultipartFile file) {
        String url = fotoService.subirFoto(file);
        return ResponseEntity.ok(new FotoUploadResponse(url));
    }
}
