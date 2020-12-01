package org.hsqldb.lib;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.springframework.nativex.substitutions.OnlyIfPresent;

@TargetClass(className = "org.hsqldb.lib.FrameworkLogger", onlyWith = { OnlyIfPresent.class })
public final class Target_FrameworkLogger {
    @Substitute
    public static boolean isDefaultJdkConfig() {
        return true;
    }
}
