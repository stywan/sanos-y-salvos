package cl.duocuc.sanossalvos.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"USER_AUTH_URL=http://localhost:9999",
		"PET_MANAGEMENT_URL=http://localhost:9999",
		"GEOLOCATION_URL=http://localhost:9999",
		"MATCHING_ENGINE_URL=http://localhost:9999",
		"NOTIFICATION_URL=http://localhost:9999",
		"BFF_URL=http://localhost:9999"
})
class ApiGatewayApplicationTests {

	@Test
	void contextLoads() {
	}

}
