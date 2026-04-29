package cl.duocuc.sanossalvos.notification;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"jwt.secret=test_secret_clave_para_testing_solo_no_usar_en_produccion",
		"spring.mail.host=localhost",
		"spring.mail.port=3025",
		"management.health.mail.enabled=false"
})
class MsNotificationApplicationTests {

	@MockBean
	JavaMailSender javaMailSender;

	@Test
	void contextLoads() {
	}

}
