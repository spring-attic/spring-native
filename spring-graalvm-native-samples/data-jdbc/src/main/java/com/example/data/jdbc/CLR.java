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
package com.example.data.jdbc;

import java.time.Period;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CLR implements CommandLineRunner {

	@Autowired LegoSetRepository repository;

	@Override
	public void run(String... args) {

		LegoSet smallCar = createLegoSet("Small Car 01", 5, 12);
		smallCar.setManual(new Manual("Just put all the pieces together in the right order", "Jens Schauder"));
		smallCar.addModel("Suv", "SUV with sliding doors.");
		smallCar.addModel("Roadster", "Slick red roadster.");

		repository.save(smallCar);

		LegoSet f1Racer = createLegoSet("F1 Racer", 6, 15);
		f1Racer.setManual(new Manual("Build a helicopter or a plane", "M. Shoemaker"));
		f1Racer.addModel("F1 Ferrari 2018", "A very fast red car.");

		repository.save(f1Racer);

		{
			System.out.println("--- Find All ---");
			System.out.println("findAll: " + repository.findAll());
		}

		{
			System.out.println("\n--- Update One ---");

			smallCar.getManual().setText("Just make it so it looks like a car.");
			smallCar.addModel("Pickup", "A pickup truck with some tools in the back.");

			System.out.println("Updated: " + repository.save(smallCar));
		}

		{
			System.out.println("\n--- Annotated Query ---");
			System.out.println("Found: " + repository.reportModelForAge(6));
		}

		{
			System.out.println("\n--- Modifying Query ---");
			System.out.println("Modified: " + repository.lowerCaseMapKeys());
		}
	}

	private static LegoSet createLegoSet(String name, int minimumAge, int maximumAge) {

		LegoSet legoSet = new LegoSet();

		legoSet.setName(name);
		legoSet.setMinimumAge(Period.ofYears(minimumAge));
		legoSet.setMaximumAge(Period.ofYears(maximumAge));

		return legoSet;
	}
}
