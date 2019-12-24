package com.example.jafu;

import com.oracle.svm.core.annotate.AutomaticFeature;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

import org.springframework.context.support.GenericApplicationContextWithoutSpel;
import org.springframework.core.io.VfsUtils;

@AutomaticFeature
public class JafuFeature implements Feature {

	@Override
	public void beforeAnalysis(BeforeAnalysisAccess access) {
		RuntimeReflection.registerForReflectiveInstantiation(GenericApplicationContextWithoutSpel.class);
		RuntimeClassInitialization.initializeAtRunTime(VfsUtils.class);
	}
}
