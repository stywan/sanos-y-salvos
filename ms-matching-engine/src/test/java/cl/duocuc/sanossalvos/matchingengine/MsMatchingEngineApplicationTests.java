package cl.duocuc.sanossalvos.matchingengine;

import cl.duocuc.sanossalvos.matchingengine.client.GeolocationClient;
import cl.duocuc.sanossalvos.matchingengine.client.NotificacionClient;
import cl.duocuc.sanossalvos.matchingengine.client.PetManagementClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"services.pet-management.url=http://localhost:9999",
		"services.geolocation.url=http://localhost:9999",
		"services.notification.url=http://localhost:9999"
})
class MsMatchingEngineApplicationTests {

	@MockBean PetManagementClient petManagementClient;
	@MockBean GeolocationClient   geolocationClient;
	@MockBean NotificacionClient  notificacionClient;

	@Test
	void contextLoads() {
	}

}
