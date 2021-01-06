package org.apache.catalina.servlets;

import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.nativex.substitutions.OnlyIfPresent;

@Delete
@TargetClass(className = "org.apache.catalina.servlets.DefaultServlet", onlyWith = { OnlyIfPresent.class })
final class Target_DefaultServlet {

}
