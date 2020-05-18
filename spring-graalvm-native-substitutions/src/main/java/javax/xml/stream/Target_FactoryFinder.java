package javax.xml.stream;

import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.graalvm.substitutions.OnlyPresent;
import org.springframework.graalvm.substitutions.RemoveXmlSupport;

@TargetClass(className = "javax.xml.stream.FactoryFinder", onlyWith = { OnlyPresent.class, RemoveXmlSupport.class })
@Delete
final class Target_FactoryFinder {
}

