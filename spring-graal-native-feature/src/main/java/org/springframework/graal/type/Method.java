/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.graal.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;
import org.springframework.graal.support.SpringFeature;
import org.springframework.graal.type.Type.CompilationHint;

public class Method {
	
	private MethodNode mn;
	private TypeSystem typeSystem;

	public Method(MethodNode mn, TypeSystem ts) {
		this.mn = mn;
		this.typeSystem = ts;
	}
	
	public String toString() {
		return mn.name+mn.desc;
	}

	public String getName() {
		return mn.name;
	}

	public String getDesc() {
		return mn.desc;
	}

	public List<Hint> getHints() {
		List<Hint> hints = new ArrayList<>();
		if (mn.visibleAnnotations != null) {
			for (AnnotationNode an: mn.visibleAnnotations) {
				Type annotationType = typeSystem.Lresolve(an.desc, true);
				if (annotationType == null) {
					SpringFeature.log("Couldn't resolve "+an.desc+" annotation type whilst searching for hints on "+getName());
				} else {
					Stack<Type> s = new Stack<>();
					// s.push(this);
					annotationType.collectHints(an, hints, new HashSet<>(), s);
				}
			}
		}
		return hints.size()==0?Collections.emptyList():hints;
	}
	
	
}