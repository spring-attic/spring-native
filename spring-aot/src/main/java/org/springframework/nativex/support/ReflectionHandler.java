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

package org.springframework.nativex.support;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.nativex.AotOptions;
import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.domain.reflect.FieldDescriptor;
import org.springframework.nativex.domain.reflect.MethodDescriptor;
import org.springframework.nativex.domain.reflect.ReflectionDescriptor;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.Flag;
import org.springframework.nativex.type.AccessChecker;
import org.springframework.nativex.type.AccessDescriptor;


/**
 * Loads up the constant data defined in resource file and registers reflective
 * access being necessary with the image build. Also provides an method
 * ({@code addAccess(String typename, Flag... flags)} usable from elsewhere
 * when needing to register reflective access to a type (e.g. used when resource
 * processing).
 * 
 * @author Andy Clement
 */
public class ReflectionHandler extends Handler {
	
	private static Log logger = LogFactory.getLog(ReflectionHandler.class);

	private ReflectionDescriptor constantReflectionDescriptor;

	private final AotOptions aotOptions;

	public ReflectionHandler(ConfigurationCollector collector, AotOptions aotOptions) {
		super(collector);
		this.aotOptions = aotOptions;
	}

	private void registerWebApplicationTypeClasses() {
		if (ts.resolveDotted("org.springframework.web.reactive.DispatcherHandler", true) !=null && 
			ts.resolveDotted("org.springframework.web.servlet.DispatcherServlet", true) == null && 
			ts.resolveDotted("org.glassfish.jersey.servlet.ServletContainer", true) == null) {
			addAccess("org.springframework.web.reactive.DispatcherHandler");
		} else if (ts.resolveDotted("javax.servlet.Servlet", true) !=null && 
			ts.resolveDotted("org.springframework.web.context.ConfigurableWebApplicationContext", true) != null) {
			addAccess("javax.servlet.Servlet");
			addAccess("org.springframework.web.context.ConfigurableWebApplicationContext");
		}
	}

	public void register() {
		registerWebApplicationTypeClasses();
		if (!aotOptions.isRemoveYamlSupport()) {
			addAccess("org.yaml.snakeyaml.Yaml", Flag.allDeclaredConstructors, Flag.allDeclaredMethods);
		}
	}

	/**
	 * Record that reflective access to a type (and a selection of its members based
	 * on the flags) should be possible at runtime. This method will pre-emptively
	 * check all type references to ensure later native-image processing will not
	 * fail if, for example, it trips up over a type reference in a generic type
	 * that isn't on the image building classpath. NOTE: it is assumed that if
	 * elements are not accessible that the runtime doesn't need them (this is done
	 * under the spring model where conditional checks on auto configuration would
	 * cause no attempts to be made to types/members that aren't added here).
	 * 
	 * @param typename the dotted type name for which to add reflective access
	 * @param flags    any members that should be accessible via reflection
	 */
	public void addAccess(String typename, Flag...flags) {
		addAccess(typename, null, null, false, flags);
	}
	
	public void addAccess(String typename, boolean silent, AccessDescriptor ad) {
		if (ad.noMembersSpecified()) {
			addAccess(typename, null, null, silent, AccessBits.getFlags(ad.getAccessBits()));
		} else {
			List<org.springframework.nativex.type.MethodDescriptor> mds = ad.getMethodDescriptors();
			String[][] methodsAndConstructors = new String[mds.size()][];
			for (int m=0;m<mds.size();m++) {
				org.springframework.nativex.type.MethodDescriptor methodDescriptor = mds.get(m);
				methodsAndConstructors[m] = new String[methodDescriptor.getParameterTypes().size()+1];
				methodsAndConstructors[m][0] = methodDescriptor.getName();
				List<String> ps = methodDescriptor.getParameterTypes();
				for (int p=0;p<ps.size();p++) {
					methodsAndConstructors[m][p+1]=ps.get(p);
				}
			}
			List<FieldDescriptor> fds = ad.getFieldDescriptors();
			String[][] fields = new String[fds.size()][];
			for (int f=0;f<fds.size();f++) {
				FieldDescriptor fd = fds.get(f);
				fields[f] = FieldDescriptor.toStringArray(fd.getName(), fd.isAllowUnsafeAccess(), fd.isAllowWrite());
			}
			addAccess(typename, methodsAndConstructors, fields, silent, AccessBits.getFlags(ad.getAccessBits()));
		}
	}
	
	public static String[] subarray(String[] array) {
		if (array.length == 1) {
			return null;
		} else {
			return Arrays.copyOfRange(array, 1, array.length);
		}
	}

	public void addAccess(String typename, String[][] methodsAndConstructors, String[][] fields, boolean silent, Flag... flags) {
		if (!silent) {
			logger.debug("Registering reflective access to " + typename+": "+(flags==null?"":Arrays.asList(flags)));
		}
		List<AccessChecker> accessCheckers = ts.getAccessCheckers();
		for (AccessChecker accessChecker: accessCheckers) {
			boolean isOK = accessChecker.check(ts, typename);
			if (!isOK) {
				logger.debug(typename+" discarded due to access check by "+accessChecker.getClass().getName());
				return;
			}
		}
		// This can return null if, for example, the supertype of the specified type is
		// not on the classpath. In a simple app there may be a number of types coming in
		// from spring-boot-autoconfigure but they extend types not on the classpath.
//		Class<?> type = rra.resolveType(typename);
//		if (type == null) {
//			logger.info("WARNING: Possible problem, cannot resolve " + typename);
//			return null;
//		}
		
		ClassDescriptor cd = ClassDescriptor.of(typename);
		if (cd == null) {
			cd  = ClassDescriptor.of(typename);
		}
		// Update flags...
		for (Flag f : flags) {
			cd.setFlag(f);
		}
		if (methodsAndConstructors != null) {
			for (String[] mc: methodsAndConstructors) {
				MethodDescriptor md = MethodDescriptor.of(mc[0], subarray(mc));
				if (!cd.contains(md)) {
					cd.addMethodDescriptor(md);	
				}
			}
		}
		if (fields != null) {
			for (String[] field: fields) {
				FieldDescriptor fieldDescriptor = FieldDescriptor.of(field);
				FieldDescriptor existingFieldDescriptor = cd.getFieldDescriptorNamed(fieldDescriptor.getName());
				if (existingFieldDescriptor != null) {
					existingFieldDescriptor.merge(fieldDescriptor);
				} else {
					cd.addFieldDescriptor(fieldDescriptor);
				}
			}
		}
		collector.addClassDescriptor(cd);
	}
	
	public ClassDescriptor getClassDescriptor(String typename) {
		return collector.getClassDescriptorFor(typename);
	}

}
