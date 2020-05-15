package javax.xml.transform;

import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.internal.svm.OnlyPresent;
import org.springframework.internal.svm.RemoveXmlSupport;

@TargetClass(className = "javax.xml.transform.FactoryFinder", onlyWith = { OnlyPresent.class, RemoveXmlSupport.class })
@Delete
final class Target_FactoryFinder {
}

