/*
 * Copyright 2019-2021 the original author or authors.
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

package org.springframework;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.nativex.type.ComponentProcessor;
import org.springframework.nativex.type.NativeContext;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.Flag;
import org.springframework.nativex.type.Field;
import org.springframework.nativex.type.Method;
import org.springframework.nativex.type.Type;

// Example input code:
//
// @Repository
// public class JdbcFooRepository implements FooRepository {
//
// public interface FooRepository {
//   Foo find(long id);
//   void save(Foo foo);
// }
// 
// public class Foo {
// 	@JsonIgnore private Long id;
// 	private String value;
// 	public Foo() { }
// 	public Foo(String value) { this.value = value; }
// 	public Long getId() { return id; }
// 	...
// 
// and the result:
//    {
//            "name": "app.main.foo.Foo",
//            "allDeclaredConstructors": true,
//            "allPublicMethods": true,
//            "allDeclaredFields": true
//    },
//    {
//            "name": "app.main.foo.FooRepository",
//            "allDeclaredMethods": true
//    },
//    {
//            "name": "app.main.foo.JdbcFooRepository",
//            "allDeclaredFields": true,
//            "allDeclaredConstructors": true,
//            "allDeclaredMethods": true,
//            "allPublicMethods": true,
//            "allDeclaredClasses": true
//    },

//{"name":"org.springframework.samples.petclinic.owner.Pet","allDeclaredConstructors":true,"allPublicMethods":true},
// from: PetRepository:  Pet findById(int id) throws DataAccessException;

//{"name":"org.springframework.samples.petclinic.owner.PetType","allDeclaredConstructors":true,"allDeclaredMethods":true},
// from: JdbcPetRepositoryImpl: public List<PetType> findPetTypes() throws DataAccessException {
// from: PetRepository:  List<PetType> findPetTypes() throws DataAccessException;

//{"name":"org.springframework.samples.petclinic.vet.Vet","allDeclaredConstructors":true,"allPublicMethods":true},
// from: VetRepository:  Collection<Vet> findAll() throws DataAccessException;

//{"name":"org.springframework.samples.petclinic.vet.Vets","allDeclaredMethods":true},
// from: plural of Vets Repository...

//{"name":"org.springframework.samples.petclinic.vet.Specialty","allDeclaredConstructors":true,"allDeclaredMethods":true},
// public class Specialty extends NamedEntity implements Serializable {
// from: public class Vet extends Person { private Set<Specialty> specialties;

//{"name":"org.springframework.samples.petclinic.visit.Visit","allDeclaredConstructors":true,"allPublicMethods":true},
// from: VisitRepository: void save(Visit visit) throws DataAccessException;, List<Visit> findByPetId(Integer petId);


//{"name":"org.springframework.samples.petclinic.owner.PetTypeFormatter","allDeclaredConstructors":true},
// from: @Component public class PetTypeFormatter implements Formatter<PetType> {


/**
 * Basic spring.components entry processor for classes marked @Repository.
 * 
 * @author Andy Clement
 * @author Christoph Strobl
 */ 
public class SpringAtRepositoryComponentProcessor implements ComponentProcessor {

	private static final String LOG_PREFIX = "SARCP: ";
	
	@Override
	public boolean handle(NativeContext imageContext, String key, List<String> values) {
		Type resolvedKey = imageContext.getTypeSystem().resolveDotted(key);
		if (resolvedKey.hasAnnotationInHierarchy("Lorg/springframework/stereotype/Repository;")) {
			imageContext.log(LOG_PREFIX+"handling @Repository "+key);
			return true;
		}
		return false;
	}

	@Override
	public void process(NativeContext imageContext, String key, List<String> values) {
		try {
			Type repositoryType = imageContext.getTypeSystem().resolveDotted(key);
			Set<String> processed = new HashSet<>(); 
			processRepositoryType(repositoryType, imageContext, processed);
			registerRepositoryProxy(imageContext, repositoryType, processed);
		} catch (Throwable t) {
			imageContext.log(LOG_PREFIX+"WARNING: Problem with SpringAtRepositoryComponentProcessor: " + t.getMessage());
			t.printStackTrace();
		}
	}
	
	public void processRepositoryType(Type repositoryType, NativeContext imageContext, Set<String> processed) {
		Type[] repositoryInterfaces = repositoryType.getInterfaces(); // For example: JdbcOwnerRepositoryImpl implements OwnerRepository
		for (Type repositoryInterface: repositoryInterfaces) {
			addAllTypesFromSignaturesInRepositoryInterface(repositoryInterface, imageContext, processed);
		}
		imageContext.log(String.format(LOG_PREFIX+"%s reflective access added - adding this repository type and its hierarchy",repositoryType.getDottedName()));
		imageContext.addReflectiveAccessHierarchy(repositoryType, AccessBits.LOAD_AND_CONSTRUCT_AND_PUBLIC_METHODS|AccessBits.DECLARED_FIELDS);
	}

	public void addAllTypesFromSignaturesInRepositoryInterface(Type repositoryInterface,
			NativeContext imageContext, Set<String> processed) {
		boolean addValidationMessagesBundle = false;
		List<Method> publicRepositoryMethods = repositoryInterface.getMethods(m -> m.isPublic());
		for (Method publicRepositoryMethod : publicRepositoryMethods) {
			Set<Type> types = publicRepositoryMethod.getSignatureTypes(true);
			for (Type type : types) {
				if (type!=null && processed.add(type.getDottedName())) {
					if (inSimilarPackage(type, repositoryInterface)) {
						imageContext.log(String.format(LOG_PREFIX + "%s reflective access added - due to method %s.%s",
								type.getDottedName(), repositoryInterface.getDottedName(), publicRepositoryMethod.toString()));
						// Note access here is PUBLIC methods. For a type like Owner that extends Person this ensures the public
						// methods on Person (that access the name components) are visible. I believe if using DECLARED methods
						// we would have to add reflective access for Owner and Person - with PUBLIC we can just do Owner
						List<Flag> flags = new ArrayList<>();
						flags.add(Flag.allPublicMethods);
						flags.add(Flag.allDeclaredConstructors);
						if (type.hasAnnotatedField(null)) {
							// These fields may need to be reflectable for other facilities to work (e.g. validation if the annotations
							// express constraints on those fields).
							// TODO because the fields tend to be private, we should probably check fields on super types
							// of this domain class and see if they require exposing for the same reason
							addValidationMessagesBundle = true;
							flags.add(Flag.allDeclaredFields);
						}
						imageContext.addReflectiveAccess(type.getDottedName(), flags.toArray(new Flag[0]));
						processPossibleDomainType(type, imageContext, processed);
					}
				}
			}
		}
		if (repositoryInterface.getName().endsWith("Repository")) {
			// Look for a plural form of the repository domain type
			String pluralName = repositoryInterface.getDottedName();
			pluralName = pluralName.substring(0, pluralName.length() - "Repository".length());
			pluralName = pluralName + "s"; // Vet > Vets
			if (processed.add(pluralName)) {
				Type resolvedPluralType = imageContext.getTypeSystem().resolveDotted(pluralName, true);
				if (resolvedPluralType != null) {
					imageContext.log(String.format(LOG_PREFIX + "%s reflective access added - found as a plural form of %s",
							pluralName, repositoryInterface.getDottedName()));
					imageContext.addReflectiveAccess(pluralName, Flag.allPublicMethods, Flag.allDeclaredConstructors);
				} else {
					imageContext.log(String.format(LOG_PREFIX + "%s PLURAL TYPE NOT FOUND", pluralName));
				}
			}
		}
		if (addValidationMessagesBundle) {
			imageContext.addResourceBundle("org.hibernate.validator.ValidationMessages");
		}
	}
	
	/**
	 * Within a domain type there may be other types that need access. For example in class <tt>Pet</tt> there may be a <tt>Set&lt;Visit&gt; visits</tt>
	 */
	private void processPossibleDomainType(Type type, NativeContext imageContext, Set<String> processed) {
		List<Field> fields = type.getFields();
		for (Field field: fields) {
			Set<String> fieldTypes = field.getTypesInSignature();
			for (String fieldType: fieldTypes) {
				if (processed.add(fieldType)) {
					Type resolvedFieldType = imageContext.getTypeSystem().resolveSlashed(fieldType,true);
					if (resolvedFieldType != null && inSimilarPackage(resolvedFieldType, type)) {
						imageContext.log(String.format(LOG_PREFIX + "%s reflective access added - due to field %s.%s",
								resolvedFieldType.getDottedName(),type.getDottedName(),field.getName()));
						imageContext.addReflectiveAccess(resolvedFieldType.getDottedName(), Flag.allPublicMethods,
								Flag.allDeclaredConstructors);
						// TODO should recurse through domain object graph? processPossibleDomainType(resolvedFieldType, imageContext);
					}
				}	
			}
		}
	}

	private boolean inSimilarPackage(Type type, Type repositoryInterface) {
		String repoPackage = repositoryInterface.getPackageName();
		String typePackage = type.getPackageName();
		if (repoPackage.startsWith(typePackage) || typePackage.startsWith(repoPackage)) {
			return true;
		}
		return false;
	}

	public void registerRepositoryProxy(NativeContext imageContext, Type repositoryType, Set<String> processed) {
		List<String> repositoryInterfacesStrings = repositoryType.getInterfacesStrings();
		if (repositoryInterfacesStrings.size()!=0) {
			List<String> repositoryInterfaces = new ArrayList<>();
			imageContext.log(LOG_PREFIX+repositoryType.getDottedName()+" is getting a proxy created");
			for (String s: repositoryInterfacesStrings) {
				repositoryInterfaces.add(s.replace("/", "."));
			}
			repositoryInterfaces.add("org.springframework.aop.SpringProxy");
			repositoryInterfaces.add("org.springframework.aop.framework.Advised");
			repositoryInterfaces.add("org.springframework.core.DecoratingProxy");
			imageContext.addProxy(repositoryInterfaces);
		} else {
			imageContext.log(LOG_PREFIX+"WARNING: unable to create proxy for repository "+repositoryType.getDottedName()+" because it has no interfaces");
		}
	}

	@Override
	public void printSummary() {
	}

}
