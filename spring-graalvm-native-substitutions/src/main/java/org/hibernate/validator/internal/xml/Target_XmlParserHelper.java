package org.hibernate.validator.internal.xml;

import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.graalvm.substitutions.OnlyIfPresent;
import org.springframework.graalvm.substitutions.RemoveXmlSupport;

@TargetClass(className = "org.hibernate.validator.internal.xml.XmlParserHelper", onlyWith = { OnlyIfPresent.class, RemoveXmlSupport.class })
@Delete
final class Target_XmlParserHelper {
}
