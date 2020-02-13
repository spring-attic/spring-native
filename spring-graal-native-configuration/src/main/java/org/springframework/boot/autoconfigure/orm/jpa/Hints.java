package org.springframework.boot.autoconfigure.orm.jpa;

import org.apache.logging.log4j.message.DefaultFlowMessageFactory;
import org.springframework.graal.extension.ConfigurationHint;
import org.springframework.graal.extension.NativeImageConfiguration;
import org.springframework.graal.extension.TypeInfo;

import com.zaxxer.hikari.util.ConcurrentBag.IConcurrentBagEntry;

/*
// TODO review this - what is the right way? That type is only needed if (strictly speaking) XMLEventFactory is used
proposedHints.put("Lorg/springframework/boot/autoconfigure/orm/jpa/HibernateJpaAutoConfiguration;",
		new CompilationHint(false, false, new String[] {
				"com.sun.xml.internal.stream.events.XMLEventFactoryImpl:REGISTRAR",
				"org.apache.logging.log4j.message.DefaultFlowMessageFactory:REGISTRAR",
				"com.zaxxer.hikari.util.ConcurrentBag$IConcurrentBagEntry[]:REGISTRAR",
				"com.zaxxer.hikari.util.ConcurrentBag$IConcurrentBagEntry:REGISTRAR"
		}));
		*/
@SuppressWarnings("restriction")
@ConfigurationHint(value=HibernateJpaAutoConfiguration.class,typeInfos= {
	@TypeInfo(typeNames= {"com.sun.xml.internal.stream.events.XMLEventFactoryImpl"},types= {DefaultFlowMessageFactory.class,IConcurrentBagEntry[].class,IConcurrentBagEntry.class})	
})
public class Hints implements NativeImageConfiguration {
}
