/*
 * Copyright 2021-2021 the original author or authors.
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

package com.example.batch;

import java.util.ArrayList;
import java.util.List;

import com.example.batch.domain.Person;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;

/**
 * @author Michael Minella
 */
public class ItemReaderListener extends AbstractItemCountingItemStreamItemReader<Person>  implements StepExecutionListener {

	private List<Person> people;

	@Override
	protected Person doRead() throws Exception {
		if(getCurrentItemCount() < people.size()) {
			return people.get(getCurrentItemCount());
		}
		else {
			return null;
		}
	}

	@Override
	protected void doOpen() throws Exception {
		people = new ArrayList<>(8);

		people.add(new Person("Andrew", "Clement"));
		people.add(new Person("Sebastien", "Deleuze"));
		people.add(new Person("Jens", "Schauder"));
		people.add(new Person("Michael", "Minella"));
		people.add(new Person("David", "Syer"));
		people.add(new Person("Brian", "Clozel"));
		people.add(new Person("Andy", "Wilkinson"));
		people.add(new Person("Eleftheria", "Stein"));
	}

	@Override
	protected void doClose() throws Exception {

	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		System.out.println("Before step");
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		System.out.println("After step");
		return stepExecution.getExitStatus();
	}
}
