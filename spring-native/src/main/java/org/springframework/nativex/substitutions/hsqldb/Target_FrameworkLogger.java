package org.springframework.nativex.substitutions.hsqldb;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.springframework.nativex.substitutions.OnlyIfPresent;

// TODO To be removed when next version is released, see https://sourceforge.net/p/hsqldb/svn/6213/
@TargetClass(className = "org.hsqldb.lib.FrameworkLogger", onlyWith = { OnlyIfPresent.class })
final class Target_FrameworkLogger {
    @Substitute
    public static boolean isDefaultJdkConfig() {
        return true;
    }
}
