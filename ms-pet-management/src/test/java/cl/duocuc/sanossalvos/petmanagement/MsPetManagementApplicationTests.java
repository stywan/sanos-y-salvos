package cl.duocuc.sanossalvos.petmanagement;

import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Test de integración: verifica que el contexto de Spring levanta correctamente.
 * MinioClient se mockea para no requerir un servidor MinIO en CI.
 * Las propiedades H2 sobreescriben las variables de entorno del CI.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=test_secret_clave_para_testing_solo_no_usar_en_produccion"
})
class MsPetManagementApplicationTests {

    @MockBean
    MinioClient minioClient;

    @Test
    void contextLoads() {
    }
}
