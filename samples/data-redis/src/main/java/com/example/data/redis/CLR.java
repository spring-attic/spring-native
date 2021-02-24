/*
 * Copyright 2020 the original author or authors.
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
package com.example.data.redis;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class CLR implements CommandLineRunner {

	@Autowired StringRedisTemplate template;

	@Autowired ReactiveStringRedisTemplate reactiveTemplate;

	@Autowired PersonRepository repository;

	@Override
	public void run(String... args) throws Exception {

		System.out.println("\n\n");
		System.out.println("+---- REDIS ----+");

		{
			System.out.print("- Connection: ");
			template.execute((RedisCallback<? extends Object>) connection -> {
				connection.flushAll();
				return "OK";
			});
			System.out.println("OK");
		}

		{
			System.out.print("- Sync Template: ");
			template.opsForValue().set("success-token", "IT WORKS :)");
			System.out.println("OK");
		}

		{
			template.opsForValue().set("reactive-token", "OK");
			System.out.print("- Reactive Template: ");

			reactiveTemplate.opsForValue().get("reactive-token").defaultIfEmpty("FAIL").doOnNext(System.out::println).block(Duration.ofSeconds(1));
		}

		Person eddard = new Person("eddard", "stark");

		{ //
			System.out.print("- Repository Support: ");
			repository.save(eddard);

			if (!template.hasKey("persons:" + eddard.getId())) {

				System.out.println("FAIL");
				return;
			}
			System.out.println(repository.findByLastname(eddard.getLastname()).isEmpty() ? "FAIL" : "OK");
		}

		System.out.println("+---------------+");
		System.out.println("|  " + template.opsForValue().get("success-token") + "  |");
		System.out.println("+---------------+");
	}
}
