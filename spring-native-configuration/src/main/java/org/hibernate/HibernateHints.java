package org.hibernate;

import org.springframework.nativex.extension.NativeImageConfiguration;
import org.springframework.nativex.extension.NativeImageHint;
import org.springframework.nativex.extension.ProxyInfo;
import org.springframework.nativex.extension.TypeInfo;

import static org.springframework.nativex.type.AccessBits.*;

@NativeImageHint(trigger = org.hibernate.Session.class,
		proxyInfos = {
			@ProxyInfo(types = {
					org.hibernate.Session.class,
					org.springframework.orm.jpa.EntityManagerProxy.class
			}),
			@ProxyInfo(types = {
					org.hibernate.SessionFactory.class,
					org.springframework.orm.jpa.EntityManagerFactoryInfo.class
			}),
			@ProxyInfo(types = {
					org.hibernate.jpa.HibernateEntityManagerFactory.class,
					org.springframework.orm.jpa.EntityManagerFactoryInfo.class
			}),
			@ProxyInfo(types = org.hibernate.query.spi.QueryImplementor.class)
		},
		typeInfos = {
			@TypeInfo(types = org.hibernate.query.spi.QueryImplementor.class, access = PUBLIC_METHODS | DECLARED_FIELDS | DECLARED_METHODS | DECLARED_CONSTRUCTORS)
		}
)
public class HibernateHints implements NativeImageConfiguration {
}
