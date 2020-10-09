package org.springframework.core;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.graalvm.substitutions.FunctionalMode;
import org.springframework.graalvm.substitutions.OnlyIfPresent;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

// With the substitution on DefaultParameterNameDiscoverer, it is possible for this parameterNameDiscovers to be null, hence the substitution below
@TargetClass(className= "org.springframework.core.PrioritizedParameterNameDiscoverer", onlyWith= {FunctionalMode.class, OnlyIfPresent.class})
final class Target_PrioritizedParameterNameDiscoverer {

	@Alias
	private List<ParameterNameDiscoverer> parameterNameDiscoverers;
	
	@Substitute
	public String[] getParameterNames(Method method) {
		if (this.parameterNameDiscoverers!=null) {
			for (ParameterNameDiscoverer pnd : this.parameterNameDiscoverers) {
				String[] result = pnd.getParameterNames(method);
					if (result != null) {
						return result;
					}
			}
		}
		return null;
	}
}
