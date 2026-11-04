package com.joaogil.money_tracker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class MoneyTrackerApplicationTests {

	@Test
	void contextLoads() {
	}

}
