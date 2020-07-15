package org.hibernate.validator.internal.engine;

import javax.validation.ClockProvider;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.xml.config.ValidationBootstrapParameters;
import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProvider;

import org.springframework.graalvm.substitutions.OnlyIfPresent;
import org.springframework.graalvm.substitutions.RemoveXmlSupport;

@TargetClass(className = "org.hibernate.validator.internal.engine.AbstractConfigurationImpl", onlyWith = { OnlyIfPresent.class, RemoveXmlSupport.class })
final class Target_AbstractConfigurationImpl {

	@Alias
	private static Log LOG;

	@Alias
	private ValidationBootstrapParameters validationBootstrapParameters;

	@Alias
	private ConstraintValidatorFactory defaultConstraintValidatorFactory;

	@Alias
	private ParameterNameProvider defaultParameterNameProvider;

	@Alias
	private ClockProvider defaultClockProvider;

	@Alias
	private PropertyNodeNameProvider defaultPropertyNodeNameProvider;



	@Substitute
	private void parseValidationXml() {
		LOG.ignoringXmlConfiguration();

		if ( validationBootstrapParameters.getTraversableResolver() == null ) {
			validationBootstrapParameters.setTraversableResolver( getDefaultTraversableResolver() );
		}
		if ( validationBootstrapParameters.getConstraintValidatorFactory() == null ) {
			validationBootstrapParameters.setConstraintValidatorFactory( defaultConstraintValidatorFactory );
		}
		if ( validationBootstrapParameters.getParameterNameProvider() == null ) {
			validationBootstrapParameters.setParameterNameProvider( defaultParameterNameProvider );
		}
		if ( validationBootstrapParameters.getClockProvider() == null ) {
			validationBootstrapParameters.setClockProvider( defaultClockProvider );
		}
		if ( validationBootstrapParameters.getPropertyNodeNameProvider() == null ) {
			validationBootstrapParameters.setPropertyNodeNameProvider( defaultPropertyNodeNameProvider );
		}
	}

	@Alias
	public final TraversableResolver getDefaultTraversableResolver() {
		return null;
	}

}
