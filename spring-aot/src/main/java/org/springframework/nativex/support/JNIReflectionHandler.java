/*
 * Copyright 2021 the original author or authors.
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
import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.domain.reflect.FieldDescriptor;
import org.springframework.nativex.domain.reflect.MethodDescriptor;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.Flag;
import org.springframework.nativex.type.AccessChecker;
import org.springframework.nativex.type.AccessDescriptor;

/**
 * Handles requests for access to Java types reflectively from JNI code.
 * 
 * @author Andy Clement
 */
public class JNIReflectionHandler extends Handler {
	
	private static Log logger = LogFactory.getLog(JNIReflectionHandler.class);

	public JNIReflectionHandler(ConfigurationCollector collector) {
		super(collector);
	}

	public void addAccess(String typename, Flag...flags) {
		addAccess(typename, null, null, false, flags);
	}

	public void addAccess(String typename, AccessDescriptor accessDescriptor) {
		addAccess(typename, 
				MethodDescriptor.toStringArray(accessDescriptor.getMethodDescriptors()),
				FieldDescriptor.toStringArray(accessDescriptor.getFieldDescriptors()), 
				true, 
				AccessBits.getFlags(accessDescriptor.getAccessBits()));
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
			for (int m=0;m<mds.size();m++) {
				FieldDescriptor fieldDescriptor = fds.get(m);
				if (fieldDescriptor.isAllowUnsafeAccess()) {
					fields[m]=new String[] {fieldDescriptor.getName(),Boolean.toString(fieldDescriptor.isAllowUnsafeAccess())};
				} else {
					fields[m]=new String[] {fieldDescriptor.getName()};
				}
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
			for (String[] fs: fields) {
				boolean allowUnsafeAccess = Boolean.valueOf(fs.length>1?fs[1]:"false");
				FieldDescriptor fd = FieldDescriptor.of(fs[0],false,allowUnsafeAccess);
				FieldDescriptor existingFd = cd.getFieldDescriptorNamed(fd.getName());
				if (existingFd != null) {
					throw new IllegalStateException("nyi"); // merge of configuration necessary
				} else {
					cd.addFieldDescriptor(fd);
				}
			}
		}
		collector.addJNIClassDescriptor(cd);
	}
	
	public ClassDescriptor getClassDescriptor(String typename) {
		return collector.getJNIClassDescriptorFor(typename);
	}

}
