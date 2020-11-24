package org.h2.jdbc;

import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.nativex.substitutions.OnlyIfPresent;
import org.springframework.nativex.substitutions.RemoveXmlSupport;

@TargetClass(className = "org.h2.jdbc.JdbcSQLXML", onlyWith = { OnlyIfPresent.class, RemoveXmlSupport.class })
@Delete
final class Target_JdbcSQLXML {
}
