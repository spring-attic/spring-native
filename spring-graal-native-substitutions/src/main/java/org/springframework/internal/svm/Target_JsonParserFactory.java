package org.springframework.internal.svm;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.boot.json.BasicJsonParser;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.json.JsonParser;
import org.springframework.util.ClassUtils;

@TargetClass(className = "org.springframework.boot.json.JsonParserFactory", onlyWith = { SkipYaml.class, OnlyPresent.class })
public final class Target_JsonParserFactory {

	@Substitute
	public static JsonParser getJsonParser() {
		if (ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper", null)) {
			return new JacksonJsonParser();
		}
		if (ClassUtils.isPresent("com.google.gson.Gson", null)) {
			return new GsonJsonParser();
		}
		return new BasicJsonParser();
	}
}
