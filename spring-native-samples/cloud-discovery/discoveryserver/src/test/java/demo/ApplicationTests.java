// package demo;

// import static org.junit.Assert.assertEquals;

// import java.util.Map;

// import org.junit.Test;
// import org.junit.runner.RunWith;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.boot.test.IntegrationTest;
// import org.springframework.boot.test.SpringApplicationConfiguration;
// import org.springframework.boot.test.TestRestTemplate;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
// import org.springframework.test.context.web.WebAppConfiguration;
// import org.springframework.util.LinkedMultiValueMap;
// import org.springframework.util.MultiValueMap;

// @RunWith(SpringJUnit4ClassRunner.class)
// @SpringApplicationConfiguration(classes = ConfigServerApplication.class)
// @WebAppConfiguration
// @IntegrationTest("server.port=0")
// public class ApplicationTests {

// 	@Value("${local.server.port}")
// 	private int port = 0;

// 	@Test
// 	public void configurationAvailable() {
// 		@SuppressWarnings("rawtypes")
// 		ResponseEntity<Map> entity = new TestRestTemplate().getForEntity(
// 				"http://localhost:" + port + "/app/cloud", Map.class);
// 		assertEquals(HttpStatus.OK, entity.getStatusCode());
// 	}

// 	@Test
// 	public void envPostAvailable() {
// 		MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
// 		@SuppressWarnings("rawtypes")
// 		ResponseEntity<Map> entity = new TestRestTemplate().postForEntity(
// 				"http://localhost:" + port + "/admin/env", form, Map.class);
// 		assertEquals(HttpStatus.OK, entity.getStatusCode());
// 	}

// }
