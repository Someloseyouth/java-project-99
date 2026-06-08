package hexlet.code;

import hexlet.code.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestConfig.class)
class AppApplicationTests {

	@Test
	void contextLoads() {
	}

}
