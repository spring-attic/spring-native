package org.springframework.http.codec.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.core.codec.AbstractDataBufferDecoder;
import org.springframework.core.codec.Decoder;
import org.springframework.core.codec.Encoder;
import org.springframework.http.codec.DecoderHttpMessageReader;
import org.springframework.http.codec.EncoderHttpMessageWriter;
import org.springframework.http.codec.FormHttpMessageReader;
import org.springframework.http.codec.FormHttpMessageWriter;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.codec.ServerSentEventHttpMessageReader;
import org.springframework.http.codec.json.AbstractJackson2Decoder;
import org.springframework.http.codec.json.Jackson2SmileDecoder;
import org.springframework.http.codec.json.Jackson2SmileEncoder;
import org.springframework.http.codec.multipart.MultipartHttpMessageReader;
import org.springframework.http.codec.multipart.MultipartHttpMessageWriter;
import org.springframework.http.codec.multipart.SynchronossPartHttpMessageReader;
import org.springframework.http.codec.protobuf.ProtobufDecoder;
import org.springframework.graalvm.substitutions.OnlyPresent;
import org.springframework.graalvm.substitutions.RemoveXmlSupport;
import org.springframework.lang.Nullable;

@TargetClass(className = "org.springframework.http.codec.support.BaseDefaultCodecs", onlyWith = { OnlyPresent.class, RemoveXmlSupport.class })
final class Target_BaseDefaultCodecs {

	@Alias
	static boolean jackson2Present;

	@Alias
	private static boolean jackson2SmilePresent;

	@Alias
	private static boolean jaxb2Present;

	@Alias
	private static boolean protobufPresent;

	@Alias
	static boolean synchronossMultipartPresent;

	@Alias
	private Integer maxInMemorySize;

	@Alias
	private Boolean enableLoggingRequestDetails;

	@Alias
	private Encoder<?> jackson2SmileEncoder;

	@Alias
	private boolean registerDefaults;

	@Alias
	private Decoder<?> jackson2SmileDecoder;

	@Substitute
	private void initCodec(@Nullable Object codec) {

		if (codec instanceof DecoderHttpMessageReader) {
			codec = ((DecoderHttpMessageReader) codec).getDecoder();
		}

		if (codec == null) {
			return;
		}

		Integer size = this.maxInMemorySize;
		if (size != null) {
			if (codec instanceof AbstractDataBufferDecoder) {
				((AbstractDataBufferDecoder<?>) codec).setMaxInMemorySize(size);
			}
			if (protobufPresent) {
				if (codec instanceof ProtobufDecoder) {
					((ProtobufDecoder) codec).setMaxMessageSize(size);
				}
			}
			if (jackson2Present) {
				if (codec instanceof AbstractJackson2Decoder) {
					((AbstractJackson2Decoder) codec).setMaxInMemorySize(size);
				}
			}
			if (codec instanceof FormHttpMessageReader) {
				((FormHttpMessageReader) codec).setMaxInMemorySize(size);
			}
			if (codec instanceof ServerSentEventHttpMessageReader) {
				((ServerSentEventHttpMessageReader) codec).setMaxInMemorySize(size);
				initCodec(((ServerSentEventHttpMessageReader) codec).getDecoder());
			}
			if (synchronossMultipartPresent) {
				if (codec instanceof SynchronossPartHttpMessageReader) {
					((SynchronossPartHttpMessageReader) codec).setMaxInMemorySize(size);
				}
			}
		}

		Boolean enable = this.enableLoggingRequestDetails;
		if (enable != null) {
			if (codec instanceof FormHttpMessageReader) {
				((FormHttpMessageReader) codec).setEnableLoggingRequestDetails(enable);
			}
			if (codec instanceof MultipartHttpMessageReader) {
				((MultipartHttpMessageReader) codec).setEnableLoggingRequestDetails(enable);
			}
			if (synchronossMultipartPresent) {
				if (codec instanceof SynchronossPartHttpMessageReader) {
					((SynchronossPartHttpMessageReader) codec).setEnableLoggingRequestDetails(enable);
				}
			}
			if (codec instanceof FormHttpMessageWriter) {
				((FormHttpMessageWriter) codec).setEnableLoggingRequestDetails(enable);
			}
			if (codec instanceof MultipartHttpMessageWriter) {
				((MultipartHttpMessageWriter) codec).setEnableLoggingRequestDetails(enable);
			}
		}

		if (codec instanceof MultipartHttpMessageReader) {
			initCodec(((MultipartHttpMessageReader) codec).getPartReader());
		}
		else if (codec instanceof MultipartHttpMessageWriter) {
			initCodec(((MultipartHttpMessageWriter) codec).getFormWriter());
		}
	}

	@Substitute
	final List<HttpMessageReader<?>> getObjectReaders() {
		if (!this.registerDefaults) {
			return Collections.emptyList();
		}
		List<HttpMessageReader<?>> readers = new ArrayList<>();
		if (jackson2Present) {
			addCodec(readers, new DecoderHttpMessageReader<>(getJackson2JsonDecoder()));
		}
		if (jackson2SmilePresent) {
			addCodec(readers, new DecoderHttpMessageReader<>(this.jackson2SmileDecoder != null ?
					(Jackson2SmileDecoder) this.jackson2SmileDecoder : new Jackson2SmileDecoder()));
		}

		// client vs server..
		extendObjectReaders(readers);

		return readers;
	}

	@Alias
	protected <T> void addCodec(List<T> codecs, T codec) {
	}

	@Alias
	protected void extendObjectReaders(List<HttpMessageReader<?>> objectReaders) {
	}

	@Substitute
	final List<HttpMessageWriter<?>> getBaseObjectWriters() {
		List<HttpMessageWriter<?>> writers = new ArrayList<>();
		if (jackson2Present) {
			writers.add(new EncoderHttpMessageWriter<>(getJackson2JsonEncoder()));
		}
		if (jackson2SmilePresent) {
			writers.add(new EncoderHttpMessageWriter<>(this.jackson2SmileEncoder != null ?
					(Jackson2SmileEncoder) this.jackson2SmileEncoder : new Jackson2SmileEncoder()));
		}
		return writers;
	}

	@Alias
	protected Decoder<?> getJackson2JsonDecoder() {
		return null;
	}

	@Alias
	protected Encoder<?> getJackson2JsonEncoder() {
		return null;
	}
}
