package org.springframework.aot.factories.fixtures;

public class PublicFactory implements TestFactory {

	public PublicFactory() {

	}

	public static class InnerFactory {

		public InnerFactory() {
			
		}

	}
	
}
