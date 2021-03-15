package com.example.wavefront.sleuth.test;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "management.metrics.export.wavefront.enabled=false")
@SpringBootTest
public class WavefrontSleuthApplicationTest {

	@Test
	public void contextLoads() {
	}

}
