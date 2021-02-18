package org.springframework.aot.factories.fixtures;

class ProtectedFactory implements TestFactory {

	private ProtectedFactory() {
		
	}

	static class InnerProtectedFactory {

		InnerProtectedFactory() {
			
		}

	}

}
