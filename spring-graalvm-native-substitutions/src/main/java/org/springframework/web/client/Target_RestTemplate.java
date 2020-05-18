package org.springframework.web.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.cbor.MappingJackson2CborHttpMessageConverter;
import org.springframework.http.converter.feed.AtomFeedHttpMessageConverter;
import org.springframework.http.converter.feed.RssChannelHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.JsonbHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.smile.MappingJackson2SmileHttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.graalvm.substitutions.FormHttpMessageConverterIsAround;
import org.springframework.graalvm.substitutions.OnlyPresent;
import org.springframework.graalvm.substitutions.RemoveXmlSupport;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriTemplateHandler;

@TargetClass(className = "org.springframework.web.client.RestTemplate", onlyWith = { OnlyPresent.class, FormHttpMessageConverterIsAround.class, RemoveXmlSupport.class })
final class Target_RestTemplate {

	@Alias
	private List<HttpMessageConverter<?>> messageConverters;

	@Alias
	private UriTemplateHandler uriTemplateHandler;

	@Alias
	private static boolean romePresent;

	@Alias
	private static boolean jaxb2Present;

	@Alias
	private static boolean jackson2Present;

	@Alias
	private static boolean jackson2XmlPresent;

	@Alias
	private static boolean jackson2SmilePresent;

	@Alias
	private static boolean jackson2CborPresent;

	@Alias
	private static boolean gsonPresent;

	@Alias
	private static boolean jsonbPresent;

	@Alias
	private ResponseErrorHandler errorHandler;

	@Alias
	private ResponseExtractor<HttpHeaders> headersExtractor;


	@Substitute
	public Target_RestTemplate() {
		this.messageConverters = new ArrayList<>();
		this.errorHandler = new DefaultResponseErrorHandler();
		this.headersExtractor = new Target_RestTemplate_HeadersExtractor();

		this.messageConverters.add(new ByteArrayHttpMessageConverter());
		this.messageConverters.add(new StringHttpMessageConverter());
		this.messageConverters.add(new ResourceHttpMessageConverter(false));
		this.messageConverters.add(new FormHttpMessageConverter()); // Impossible to create a substitution for AllEncompassingFormHttpMessageConverter for now so we use that one

		if (romePresent) {
			this.messageConverters.add(new AtomFeedHttpMessageConverter());
			this.messageConverters.add(new RssChannelHttpMessageConverter());
		}

		if (jackson2XmlPresent) {
			this.messageConverters.add(new MappingJackson2XmlHttpMessageConverter());
		}

		if (jackson2Present) {
			this.messageConverters.add(new MappingJackson2HttpMessageConverter());
		}
		else if (gsonPresent) {
			this.messageConverters.add(new GsonHttpMessageConverter());
		}
		else if (jsonbPresent) {
			this.messageConverters.add(new JsonbHttpMessageConverter());
		}

		if (jackson2SmilePresent) {
			this.messageConverters.add(new MappingJackson2SmileHttpMessageConverter());
		}
		if (jackson2CborPresent) {
			this.messageConverters.add(new MappingJackson2CborHttpMessageConverter());
		}

		this.uriTemplateHandler = initUriTemplateHandler();
	}

	@Alias
	private static DefaultUriBuilderFactory initUriTemplateHandler() {
		return null;
	}

}

@TargetClass(className = "org.springframework.web.client.RestTemplate", innerClass ="HeadersExtractor", onlyWith = { OnlyPresent.class, FormHttpMessageConverterIsAround.class, RemoveXmlSupport.class })
final class Target_RestTemplate_HeadersExtractor implements ResponseExtractor<HttpHeaders> {

	@Alias
	public HttpHeaders extractData(ClientHttpResponse response) throws IOException {
		return null;
	}
}
