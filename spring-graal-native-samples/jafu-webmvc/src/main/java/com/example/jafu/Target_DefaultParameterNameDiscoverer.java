package com.example.jafu;

import java.util.LinkedList;
import java.util.List;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.core.ParameterNameDiscoverer;

// Because code removal does not work yet with GraalDetector
@TargetClass(className = "org.springframework.core.DefaultParameterNameDiscoverer")
final class Target_DefaultParameterNameDiscoverer {

	@Alias
	private List<ParameterNameDiscoverer> parameterNameDiscoverers;

	@Substitute
	public Target_DefaultParameterNameDiscoverer() {
		this.parameterNameDiscoverers = new LinkedList<>();
	}
}
