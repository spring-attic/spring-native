package org.springframework.transaction.annotation;

import org.springframework.context.annotation.AutoProxyRegistrar;
import org.springframework.graal.extension.ConfigurationHint;
import org.springframework.graal.extension.NativeImageConfiguration;
import org.springframework.graal.extension.TypeInfo;
import org.springframework.transaction.aspectj.AspectJJtaTransactionManagementConfiguration;
import org.springframework.transaction.aspectj.AspectJTransactionManagementConfiguration;

/*
proposedHints.put(TransactionManagementConfigurationSelector,
		new CompilationHint(false, true, new String[] { 
		// TODO really the 'skip if missing' can be different for each one here...
			"org.springframework.context.annotation.AutoProxyRegistrar",
			"org.springframework.transaction.annotation.ProxyTransactionManagementConfiguration",
			"org.springframework.transaction.aspectj.AspectJJtaTransactionManagementConfiguration",
			"org.springframework.transaction.aspectj.AspectJTransactionManagementConfiguration"
		}));
		*/
// TODO really the 'skip if missing' can be different for each one here...
@ConfigurationHint(value=TransactionManagementConfigurationSelector.class,typeInfos = {
	@TypeInfo(types= {AutoProxyRegistrar.class, ProxyTransactionManagementConfiguration.class,AspectJJtaTransactionManagementConfiguration.class,	
			AspectJTransactionManagementConfiguration.class })
},follow=true)
public class Hints implements NativeImageConfiguration {
}
