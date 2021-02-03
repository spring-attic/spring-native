package org.springframework.nativex.buildtools.factories.fixtures;

class ProtectedFactory implements TestFactory {

	private ProtectedFactory() {
		
	}

	static class InnerProtectedFactory {

		InnerProtectedFactory() {
			
		}

	}

}
