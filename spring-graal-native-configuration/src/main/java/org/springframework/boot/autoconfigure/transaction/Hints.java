package org.springframework.boot.autoconfigure.transaction;

import org.springframework.graal.extension.ConfigurationHint;
import org.springframework.graal.extension.NativeImageConfiguration;
import org.springframework.graal.extension.TypeInfo;
import org.springframework.graal.type.AccessBits;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.AbstractTransactionManagementConfiguration;
import org.springframework.transaction.annotation.ProxyTransactionManagementConfiguration;

/*
 * x
//{"name":"org.springframework.transaction.TransactionManager"},
//{"name":"org.springframework.transaction.annotation.ProxyTransactionManagementConfiguration","allDeclaredConstructors":true,"allDeclaredMethods":true},
//{"name":"org.springframework.transaction.annotation.AbstractTransactionManagementConfiguration","allDeclaredConstructors":true,"allDeclaredMethods":true},
		*/
@ConfigurationHint(value=TransactionAutoConfiguration.class, typeInfos = {
		@TypeInfo(types= {TransactionManager.class,ProxyTransactionManagementConfiguration.class,AbstractTransactionManagementConfiguration.class},access=AccessBits.CLASS|AccessBits.PUBLIC_METHODS|AccessBits.PUBLIC_CONSTRUCTORS)
},abortIfTypesMissing = true)
public class Hints implements NativeImageConfiguration {
}
