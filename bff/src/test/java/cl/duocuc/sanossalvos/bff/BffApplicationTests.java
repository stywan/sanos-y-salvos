package cl.duocuc.sanossalvos.bff;

import cl.duocuc.sanossalvos.bff.client.GeolocationClient;
import cl.duocuc.sanossalvos.bff.client.MatchingEngineClient;
import cl.duocuc.sanossalvos.bff.client.NotificacionClient;
import cl.duocuc.sanossalvos.bff.client.PetManagementClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(properties = {
        "jwt.secret=clave_local_desarrollo_no_usar_en_produccion_para_tests",
        "services.pet-management.url=http://localhost:9999",
        "services.geolocation.url=http://localhost:9999",
        "services.matching-engine.url=http://localhost:9999",
        "services.notification.url=http://localhost:9999"
})
class BffApplicationTests {

    // Mockear los clientes evita que Spring intente conectarse a servicios reales
    @MockBean
    PetManagementClient petManagementClient;

    @MockBean
    GeolocationClient geolocationClient;

    @MockBean
    MatchingEngineClient matchingEngineClient;

    @MockBean
    NotificacionClient notificacionClient;

    @Test
    void contextLoads() {
    }
}
