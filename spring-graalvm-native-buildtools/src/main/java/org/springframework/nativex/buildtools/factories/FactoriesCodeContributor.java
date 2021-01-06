package org.springframework.nativex.buildtools.factories;

/**
 * Contribute code for instantiating Spring Factories.
 *
 * @author Brian Clozel
 */
interface FactoriesCodeContributor {

	/**
	 * Whether this contributor can contribute code for instantiating the given factory.
	 */
	boolean canContribute(SpringFactory factory);

	/**
	 * Contribute code for instantiating the factory given as argument.
	 */
	void contribute(SpringFactory factory, CodeGenerator code);
}
