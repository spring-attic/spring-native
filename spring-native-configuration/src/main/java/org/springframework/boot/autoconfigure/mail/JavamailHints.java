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
        types = {
                @TypeHint(types = SMTPProvider.class),
                @TypeHint(types = SMTPTransport.class),
                @TypeHint(types = multipart_mixed.class),
                @TypeHint(types = text_plain.class),
                @TypeHint(types = text_html.class),
                @TypeHint(types = text_xml.class),
                @TypeHint(types = message_rfc822.class)
        },
        resources = {
                @ResourceHint(patterns = {"org/springframework/mail/javamail/mime.types", "META-INF/mailcap", "META-INF/javamail.*"})
        }
)
public class JavamailHints implements NativeConfiguration {
}
