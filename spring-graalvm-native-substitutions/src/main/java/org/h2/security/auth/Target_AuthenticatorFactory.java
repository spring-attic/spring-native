package org.h2.security.auth;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.nativex.substitutions.OnlyIfPresent;
import org.springframework.nativex.substitutions.RemoveXmlSupport;

@TargetClass(className = "org.h2.security.auth.AuthenticatorFactory", onlyWith = { OnlyIfPresent.class, RemoveXmlSupport.class })
final class Target_AuthenticatorFactory {

	@Substitute
	public static Authenticator createAuthenticator() {
		return null;
	}
}
