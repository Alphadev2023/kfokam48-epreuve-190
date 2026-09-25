package com.kfokam48.relectures;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Demarre l'application sur H2 : prouve que V1 s'applique et que les entites correspondent au schema. */
@SpringBootTest
@ActiveProfiles("test")
class RelecturesApplicationTests {

	@Test
	void leContexteDemarreEtLeSchemaEstValide() {
	}
}