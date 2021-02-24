/*
 * Copyright 2019-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.demo;

import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.ToStringConsumer;
import org.testcontainers.utility.MountableFile;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Dave Syer
 *
 */
// @Disabled
public class ContainerTests {

	@Test
	void test() throws Exception {
		ToStringConsumer consumer = new ToStringConsumer();
		try (@SuppressWarnings("resource")
		GenericContainer<?> container = new GenericContainer<>("lambci/lambda:provided").withLogConsumer(consumer)
				.withCopyFileToContainer(MountableFile.forClasspathResource("bootstrap"), "/var/task/")
				.withEnv("DOCKER_LAMBDA_STAY_OPEN", "1").withExposedPorts(9001)) {
			container.start();
			int port = container.getFirstMappedPort();
			String host = container.getHost();
			System.err.println(host + ":" + port);
			DemoApplication.main(new String[] { "--AWS_LAMBDA_RUNTIME_API=" + host + ":" + port, "--_HANDLER=foobar",
					"--logging.level.org.springframework=DEBUG" });
			ResponseEntity<String> response = Awaitility.waitAtMost(30, TimeUnit.SECONDS).until(() -> {
				ResponseEntity<String> result = new RestTemplate().postForEntity(
						"http://" + host + ":" + port + "/2015-03-31/functions/foobar/invocations", "foo",
						String.class);
				return result;
			}, result -> result != null);
			assertThat(response.getBody()).contains("hi foo!");
			assertThat(response.getHeaders()).containsKey("X-Amzn-Requestid");
		}
		String output = consumer.toUtf8String();
		assertThat(output).contains("Lambda API listening on port 9001");
		assertThat(output).contains("START RequestId:");
		assertThat(output).contains("END RequestId:");
	}

}
