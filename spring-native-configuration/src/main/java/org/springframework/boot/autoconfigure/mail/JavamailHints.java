package org.springframework.boot.autoconfigure.mail;

import com.sun.mail.handlers.*;
import com.sun.mail.smtp.SMTPProvider;
import com.sun.mail.smtp.SMTPTransport;

import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ResourcesInfo;
import org.springframework.nativex.hint.TypeInfo;

@NativeHint(
        trigger = MailSenderPropertiesConfiguration.class,
        typeInfos = {
                @TypeInfo(types = SMTPProvider.class),
                @TypeInfo(types = SMTPTransport.class),
                @TypeInfo(types = multipart_mixed.class),
                @TypeInfo(types = text_plain.class),
                @TypeInfo(types = text_html.class),
                @TypeInfo(types = text_xml.class),
                @TypeInfo(types = message_rfc822.class)
        },
        resourcesInfos = {
                @ResourcesInfo(patterns = {"org/springframework/mail/javamail/mime.types", "META-INF/mailcap", "META-INF/javamail.*"})
        }
)
public class JavamailHints implements NativeConfiguration {
}
