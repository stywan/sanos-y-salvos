package cl.duocuc.sanossalvos.userauth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test de integración: verifica que el contexto de Spring levanta correctamente.
 * Las propiedades se definen aquí directamente para tener mayor prioridad
 * que las variables de entorno del CI (SPRING_DATASOURCE_URL, etc.).
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=test_secret_clave_para_testing_solo_no_usar_en_produccion",
        "jwt.expiration-ms=3600000"
})
class MsUserAuthApplicationTests {

    @Test
    void contextLoads() {
    }
}
