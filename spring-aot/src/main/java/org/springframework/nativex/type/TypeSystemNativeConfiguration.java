package org.springframework.nativex.type;

import java.util.List;

/**
 * Deprecated variant of NativeConfiguration, exists for compatibility purpose for
 * programmatic hints still relying on {@link TypeSystem}.
 */
@Deprecated
public interface TypeSystemNativeConfiguration {

	List<HintDeclaration> computeHints(TypeSystem typeSystem);
}
