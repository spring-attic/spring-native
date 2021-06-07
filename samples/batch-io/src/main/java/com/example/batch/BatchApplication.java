package com.example.batch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.nativex.hint.AotProxyHint;

@AotProxyHint(targetClass=com.example.batch.ItemReaderListener.class, interfaces={org.springframework.aop.scope.ScopedObject.class, java.io.Serializable.class, org.springframework.aop.framework.AopInfrastructureBean.class})
@EnableBatchProcessing
@SpringBootApplication
public class BatchApplication {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(BatchApplication.class, args);
		Thread.currentThread().join(); // To be able to measure memory consumption
	}

}
