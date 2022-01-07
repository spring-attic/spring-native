package org.springframework.samples.petclinic.system;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
/*
 * Copyright 2012-2019 the original author or authors.
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
import org.springframework.context.annotation.Configuration;
import org.springframework.data.keyvalue.core.KeyValueTemplate;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.owner.Visit;
import org.springframework.samples.petclinic.vet.Specialty;
import org.springframework.samples.petclinic.vet.Vet;

@Configuration
public class SetUpConfiguration {

	@Bean
	public CommandLineRunner setup(KeyValueTemplate template, OwnerRepository owners) {
		return args -> {
			template.insert(1, vet(1, "James", "Carter"));
			template.insert(2, vet(2, "Helen", "Leary", "surgery"));
			template.insert(3, vet(3, "Linda", "Douglas", "surgery", "dentistry"));
			template.insert(4, vet(4, "Rafael", "Ortega", "surgery"));
			template.insert(5, vet(5, "Henry", "Stevens", "radiology"));
			template.insert(6, vet(6, "Sharon", "Jenkins"));
			template.insert(1, type(1, "cat"));
			template.insert(2, type(2, "dog"));
			template.insert(3, type(3, "lizard"));
			template.insert(4, type(4, "snake"));
			template.insert(5, type(5, "bird"));
			template.insert(6, type(6, "hamster"));
			Owner owner = owner(1, "George", "Franklin", "110 W. Liberty St.", "Madison", "6085551023");
			owners.save(owner);
			List<PetType> types = owners.findPetTypes();
			owner.addPet(pet("Leo", LocalDate.of(2010, 9, 7), owner, findPetType(types, "cat")));
			owner = owner(2, "Betty", "Davis", "638 Cardinal Ave.", "Sun Prairie", "6085551749");
			owners.save(owner);
			owner.addPet(pet("Basil", LocalDate.of(2012, 8, 6), owner, findPetType(types, "hamster")));
			owner = owner(3, "Eduardo", "Rodriquez", "2693 Commerce St.", "McFarland", "6085558763");
			owners.save(owner);
			owner.addPet(pet("Rosy", LocalDate.of(2012, 4, 17), owner, findPetType(types, "dog")));
			owner.addPet(pet("Jewel", LocalDate.of(2012, 3, 7), owner, findPetType(types, "dog")));
			owner = owner(4, "Harold", "Davis", "563 Friendly St.", "Windsor", "6085553198");
			owners.save(owner);
			owner.addPet(pet("Iggy", LocalDate.of(2010, 11, 30), owner, findPetType(types, "lizard")));
			owner = owner(5, "Peter", "McTavish", "2387 S. Fair Way", "Madison", "6085552765");
			owners.save(owner);
			owner.addPet(pet("George", LocalDate.of(2010, 1, 20), owner, findPetType(types, "snake")));
			owner = owner(6, "Jean", "Coleman", "105 N. Lake St.", "Monona", "6085552654");
			owners.save(owner);
			owner.addPet(pet("Samantha", LocalDate.of(2012, 9, 4), owner, findPetType(types, "cat")));
			owner.getPet("Samantha").addVisit(visit(LocalDate.of(2013, 1, 1), "rabies shot"));
			owner.getPet("Samantha").addVisit(visit(LocalDate.of(2013, 1, 4), "spayed"));
			owner.addPet(pet("Max", LocalDate.of(2012, 9, 4), owner, findPetType(types, "cat")));
			owner.getPet("Max").addVisit(visit(LocalDate.of(2013, 1, 2), "rabies shot"));
			owner.getPet("Max").addVisit(visit(LocalDate.of(2013, 1, 3), "neutered"));
			owner = owner(7, "Jeff", "Black", "1450 Oak Blvd.", "Monona", "6085555387");
			owners.save(owner);
			owner.addPet(pet("Lucky", LocalDate.of(2011, 8, 6), owner, findPetType(types, "bird")));
			owner = owner(8, "Maria", "Escobito", "345 Maple St.", "Madison", "6085557683");
			owners.save(owner);
			owner.addPet(pet("Mulligan", LocalDate.of(2007, 2, 24), owner, findPetType(types, "dog")));
			owner = owner(9, "David", "Schroeder", "2749 Blackhawk Trail", "Madison", "6085559435");
			owners.save(owner);
			owner.addPet(pet("Freddy", LocalDate.of(2010, 3, 9), owner, findPetType(types, "bird")));
			owner = owner(10, "Carlos", "Estaban", "2335 Independence La.", "Waunakee", "6085555487");
			owners.save(owner);
			owner.addPet(pet("Lucky", LocalDate.of(2010, 6, 24), owner, findPetType(types, "dog")));
			owner.addPet(pet("Sly", LocalDate.of(2012, 6, 8), owner, findPetType(types, "cat")));
		};
	}

	private Visit visit(LocalDate date, String description) {
		Visit visit = new Visit();
		visit.setDate(date);
		visit.setDescription(description);
		return visit;
	}

	private Pet pet(String name, LocalDate date, Owner owner, PetType type) {
		Pet pet = new Pet();
		pet.setName(name);
		pet.setBirthDate(date);
		pet.setType(type);
		return pet;
	}

	private PetType findPetType(Collection<PetType> types, String name) {
		for (PetType petType : types) {
			if (name.equals(petType.getName())) {
				return petType;
			}
		}
		return null;
	}

	private PetType type(int id, String name) {
		PetType petType = new PetType();
		petType.setId(id);
		petType.setName(name);
		return petType;
	}

	private Vet vet(Integer id, String first, String last, String... speciality) {
		Vet vet = new Vet();
		vet.setId(id);
		vet.setFirstName(first);
		vet.setLastName(last);
		for (String name : speciality) {
			Specialty spec = new Specialty();
			spec.setName(name);
			vet.addSpecialty(spec);
		}
		return vet;
	}

	private Owner owner(Integer id, String first, String last, String address, String city, String telephone) {
		Owner owner = new Owner();
		owner.setId(id);
		owner.setFirstName(first);
		owner.setLastName(last);
		owner.setAddress(address);
		owner.setCity(city);
		owner.setTelephone(telephone);
		return owner;
	}

}
