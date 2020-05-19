package org.springframework.data.mapping.context;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.model.EntityInstantiators;
import org.springframework.data.mapping.model.InstantiationAwarePropertyAccessorFactory;
import org.springframework.data.mapping.model.MutablePersistentEntity;
import org.springframework.data.mapping.model.PersistentPropertyAccessorFactory;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.mapping.model.Target_BeanWrapperPropertyAccessorFactory;
import org.springframework.data.spel.EvaluationContextProvider;
import org.springframework.data.util.TypeInformation;
import org.springframework.graalvm.substitutions.OnlyPresent;
import org.springframework.graalvm.substitutions.RemoveCglibSupport;

@TargetClass(className = "org.springframework.data.mapping.context.AbstractMappingContext", onlyWith = { OnlyPresent.class, RemoveCglibSupport.class })
final class Target_AbstractMappingContext<E extends MutablePersistentEntity<?, P>, P extends PersistentProperty<P>> {

	@Alias
	private Optional<E> NONE;

	@Alias
	private Map<TypeInformation<?>, Optional<E>> persistentEntities;

	@Alias
	private EvaluationContextProvider evaluationContextProvider;

	@Alias
	private Set<? extends Class<?>> initialEntitySet;

	@Alias
	private boolean strict;

	@Alias
	private SimpleTypeHolder simpleTypeHolder;

	@Alias
	private ReentrantReadWriteLock lock;

	@Alias
	private Lock read;

	@Alias
	private Lock write;

	@Alias
	private PersistentPropertyPathFactory<E, P> persistentPropertyPathFactory;

	@Alias
	private PersistentPropertyAccessorFactory persistentPropertyAccessorFactory;

	@Substitute
	protected Target_AbstractMappingContext()  {
		this.persistentPropertyPathFactory = new PersistentPropertyPathFactory<E, P>((AbstractMappingContext)(Object)this);

		EntityInstantiators instantiators = new EntityInstantiators();
		Target_BeanWrapperPropertyAccessorFactory accessorFactory = Target_BeanWrapperPropertyAccessorFactory.INSTANCE;

		this.persistentPropertyAccessorFactory = new InstantiationAwarePropertyAccessorFactory(accessorFactory,
				instantiators);

		NONE = Optional.empty();
		persistentEntities = new HashMap<>();
		evaluationContextProvider = EvaluationContextProvider.DEFAULT;
		initialEntitySet = new HashSet<>();
		strict = false;
		simpleTypeHolder = SimpleTypeHolder.DEFAULT;
		lock = new ReentrantReadWriteLock();
		read = lock.readLock();
		write = lock.writeLock();
	}

}
