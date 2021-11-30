package com.example.batch;

import java.io.Serializable;

import com.example.batch.domain.Person;

import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.scope.ScopedObject;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.AotProxyHint;
import org.springframework.nativex.hint.TypeHint;

// TODO Infer those hints
@TypeHint(types = Person.class, access = AccessBits.LOAD_AND_CONSTRUCT_AND_PUBLIC_METHODS)
@AotProxyHint(targetClass = ItemReaderListener.class, interfaces= {
		ScopedObject.class,
		Serializable.class,
		AopInfrastructureBean.class
})
@EnableBatchProcessing
@SpringBootApplication
public class BatchApplication {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(BatchApplication.class, args);
		Thread.currentThread().join(); // To be able to measure memory consumption
	}

}
