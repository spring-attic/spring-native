package org.springframework.boot.autoconfigure.mail;

import com.sun.mail.handlers.*;
import com.sun.mail.smtp.SMTPProvider;
import com.sun.mail.smtp.SMTPTransport;

import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.nativex.hint.TypeHint;

@NativeHint(
		trigger = MailSenderPropertiesConfiguration.class,
		types = @TypeHint(types = {
				SMTPProvider.class,
				SMTPTransport.class,
				multipart_mixed.class,
				text_plain.class,
				text_html.class,
				text_xml.class,
				message_rfc822.class
		}),
		resources = {
				@ResourceHint(patterns = {
						"org/springframework/mail/javamail/mime.types",
						"META-INF/mailcap",
						"META-INF/javamail.*"
				})
		}
)
public class JavamailHints implements NativeConfiguration {
}
