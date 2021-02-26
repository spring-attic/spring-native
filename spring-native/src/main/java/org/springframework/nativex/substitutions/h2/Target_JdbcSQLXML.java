package org.springframework.nativex.substitutions.h2;

import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.nativex.substitutions.OnlyIfPresent;
import org.springframework.nativex.substitutions.RemoveXmlSupport;

@TargetClass(className = "org.h2.jdbc.JdbcSQLXML", onlyWith = { OnlyIfPresent.class, RemoveXmlSupport.class })
@Delete
final class Target_JdbcSQLXML {
}
