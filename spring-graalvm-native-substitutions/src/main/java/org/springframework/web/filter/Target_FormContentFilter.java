package org.springframework.web.filter;

import java.util.HashSet;
import java.util.Set;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.graalvm.substitutions.FormHttpMessageConverterIsAround;
import org.springframework.graalvm.substitutions.OnlyPresent;
import org.springframework.graalvm.substitutions.RemoveXmlSupport;

@TargetClass(className = "org.springframework.web.filter.FormContentFilter", onlyWith = { OnlyPresent.class, FormHttpMessageConverterIsAround.class, RemoveXmlSupport.class })
final class Target_FormContentFilter {

	@Alias
	@RecomputeFieldValue(kind = RecomputeFieldValue.Kind.NewInstance, declClass = HashSet.class, isFinal = true)
	private Set<String> requiredProperties;

	@Alias
	@RecomputeFieldValue(kind = RecomputeFieldValue.Kind.NewInstance, declClass = Log.class, isFinal = true)
	protected  Log logger;

	@Alias
	@RecomputeFieldValue(kind = RecomputeFieldValue.Kind.NewInstance, declClass = FormHttpMessageConverter.class)
	private FormHttpMessageConverter formConverter;

	@Substitute
	public Target_FormContentFilter() {
		this.formConverter = new FormHttpMessageConverter();
		this.logger = LogFactory.getLog(getClass());
		this.requiredProperties = new HashSet<>(4);
	}
}
