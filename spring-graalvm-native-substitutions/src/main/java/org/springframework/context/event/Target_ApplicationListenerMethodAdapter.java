package org.springframework.context.event;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.graalvm.substitutions.OnlyPresent;
import org.springframework.graalvm.substitutions.RemoveSpelSupport;
import org.springframework.graalvm.substitutions.RemoveXmlSupport;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

@TargetClass(className = "org.springframework.context.event.ApplicationListenerMethodAdapter", onlyWith = { OnlyPresent.class, RemoveSpelSupport.class })
final class Target_ApplicationListenerMethodAdapter {

	@Delete
	private EventExpressionEvaluator evaluator;

	@Delete
	void init(ApplicationContext applicationContext, EventExpressionEvaluator evaluator) {
	}

	@Substitute
	private boolean shouldHandle(ApplicationEvent event, @Nullable Object[] args) {
		if (args == null) {
			return false;
		}
		String condition = getCondition();
		if (StringUtils.hasText(condition)) {
			throw new UnsupportedOperationException("SpEL support disabled");
		}
		return true;
	}

	@Alias
	protected String getCondition() {
		return null;
	}
}
