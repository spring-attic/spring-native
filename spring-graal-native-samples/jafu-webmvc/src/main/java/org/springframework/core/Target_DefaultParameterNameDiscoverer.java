package org.springframework.core;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

// Because code removal does not work yet with GraalDetector
@TargetClass(className = "org.springframework.core.DefaultParameterNameDiscoverer")
final class Target_DefaultParameterNameDiscoverer {

	@Substitute
	public Target_DefaultParameterNameDiscoverer() {
	}
}
