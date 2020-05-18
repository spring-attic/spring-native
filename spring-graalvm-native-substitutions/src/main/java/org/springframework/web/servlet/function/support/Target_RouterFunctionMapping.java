package org.springframework.web.servlet.function.support;

import java.util.ArrayList;
import java.util.List;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.graalvm.substitutions.FormHttpMessageConverterIsAround;
import org.springframework.graalvm.substitutions.OnlyPresent;
import org.springframework.graalvm.substitutions.RemoveXmlSupport;

@TargetClass(className = "org.springframework.web.servlet.function.support.RouterFunctionMapping", onlyWith = { OnlyPresent.class, FormHttpMessageConverterIsAround.class, RemoveXmlSupport.class })
final class Target_RouterFunctionMapping {

	@Alias
	private List<HttpMessageConverter<?>> messageConverters;

	@Substitute
	private void initMessageConverters() {
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<>(4);
		messageConverters.add(new ByteArrayHttpMessageConverter());
		messageConverters.add(new StringHttpMessageConverter());
		messageConverters.add(new FormHttpMessageConverter()); // Impossible to create a substitution for AllEncompassingFormHttpMessageConverter for now so we use that one

		this.messageConverters = messageConverters;
	}

}
