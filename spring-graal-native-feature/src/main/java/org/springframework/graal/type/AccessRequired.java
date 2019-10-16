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
import java.util.List;

import org.springframework.graal.domain.reflect.Flag;

/**
 * Captures what kind of access is needed to a type
 * 
 * @author Andy Clement
 */
public enum AccessRequired {
    EXISTENCE_CHECK(true, false, false, false, false), // Only need to reflect on it, not even its methods/ctors
    REGISTRAR(true, false, false, true, false), // registrars need to be accessible with constructor access
    // TODO ALL is a bit heavy handed including fields too - check users of ALL shouldn't be using something slimmer
    ALL(true, true, true, true, true), // Need to reflect on it, including methods/ctors and accessing as a resource (this is basically what @Configuration needs)
	EXISTENCE_AND_RESOURCE(true, false, false, false, true);

    private boolean typeReflect;
    private boolean fieldReflect;
    private boolean methodReflect;
    private boolean constructorReflect;
    private boolean resourceAccess;

    private AccessRequired(boolean typeReflect, boolean fieldReflect, boolean methodReflect, boolean constructorReflect, boolean resourceAccess) {
        this.typeReflect = typeReflect;
        this.fieldReflect = fieldReflect;
        this.methodReflect = methodReflect;
        this.constructorReflect = constructorReflect;
        this.resourceAccess = resourceAccess;
    }

    public boolean isResourceAccess() {
        return resourceAccess;
    }

    public boolean isConstructorReflect() {
        return constructorReflect;
    }

    public boolean isMethodReflect() {
        return methodReflect;
    }

    public boolean isFieldReflect() {
        return fieldReflect;
    }

    public boolean isTypeReflect() {
        return typeReflect;
    }
    
    public Flag[] getFlags() {
       List<Flag> flags = new ArrayList<>();
       if (isFieldReflect()) {
           flags.add(Flag.allDeclaredFields);
       }
       if (isConstructorReflect()) {
           flags.add(Flag.allDeclaredConstructors);
       }
       if (isMethodReflect()) {
           flags.add(Flag.allDeclaredMethods);
       }
       return flags.toArray(new Flag[0]);
    }

}