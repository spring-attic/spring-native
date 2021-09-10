package org.springframework.context.bootstrap.generator.infrastructure;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.MemberDescriptor;
import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor.PropertyDescriptor;
import org.springframework.context.bootstrap.generator.infrastructure.reflect.RuntimeReflectionRegistry;

/**
 * Handle reflection and resources metadata necessary at runtime.
 *
 * @author Stephane Nicoll
 */
public class BeanRuntimeResourcesRegistrar {

	/**
	 * Register the reflection and resources information necessary to instantiate the
	 * bean defined by the specified {@link BeanInstanceDescriptor}.
	 * @param registry the registry to use
	 * @param descriptor the descriptor of the bean instance ot handle
	 */
	public void register(RuntimeReflectionRegistry registry, BeanInstanceDescriptor descriptor) {
		MemberDescriptor<Executable> instanceCreator = descriptor.getInstanceCreator();
		registry.addMethod(instanceCreator.getMember());
		for (MemberDescriptor<?> injectionPoint : descriptor.getInjectionPoints()) {
			Member member = injectionPoint.getMember();
			if (member instanceof Executable) {
				registry.addMethod((Method) member);
			}
			else if (member instanceof Field) {
				registry.addField((Field) member);
			}
		}
		for (PropertyDescriptor property : descriptor.getProperties()) {
			Method writeMethod = property.getWriteMethod();
			if (writeMethod != null) {
				registry.addMethod(writeMethod);
			}
		}
	}

}
