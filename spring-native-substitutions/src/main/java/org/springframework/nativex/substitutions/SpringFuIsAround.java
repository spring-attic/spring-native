package org.springframework.nativex.substitutions;

import java.util.function.BooleanSupplier;

public class SpringFuIsAround implements BooleanSupplier  {

	@Override
	public boolean getAsBoolean() {
		try {
			Class.forName("org.springframework.fu.jafu.JafuApplication");
			return true;
		} catch (ClassNotFoundException ex1) {
			try {
				Class.forName("org.springframework.fu.kofu.KofuApplication");
				return true;
			} catch (ClassNotFoundException ex2) {
				return false;
			}
		}
	}
}
