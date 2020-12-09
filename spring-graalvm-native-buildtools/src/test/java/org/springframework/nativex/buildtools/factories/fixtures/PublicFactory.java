package org.springframework.nativex.buildtools.factories.fixtures;

public class PublicFactory implements TestFactory {

	public PublicFactory() {

	}

	public static class InnerFactory {

		public InnerFactory() {
			
		}

	}
	
}
