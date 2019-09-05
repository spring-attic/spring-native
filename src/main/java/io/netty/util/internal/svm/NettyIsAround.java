package io.netty.util.internal.svm;

import java.util.function.BooleanSupplier;

public class NettyIsAround implements BooleanSupplier {

	@Override
	public boolean getAsBoolean() {
		try {
			Class.forName("io.netty.util.internal.CleanerJava6");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

}
