package org.apache.catalina.servlets;

import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.graalvm.substitutions.OnlyPresent;

@Delete
@TargetClass(className = "org.apache.catalina.servlets.DefaultServlet", onlyWith = { OnlyPresent.class })
final class Target_DefaultServlet {

}
