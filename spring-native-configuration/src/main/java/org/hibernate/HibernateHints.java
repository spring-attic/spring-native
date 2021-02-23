package org.hibernate;

import org.hibernate.query.Query;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ProxyInfo;
import org.springframework.nativex.hint.TypeInfo;

import static org.springframework.nativex.hint.AccessBits.*;

@NativeHint(trigger = org.hibernate.Session.class,
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
			@ProxyInfo(types = {Query.class, org.hibernate.query.spi.QueryImplementor.class,})
		},
		typeInfos = {
			@TypeInfo(types = org.hibernate.query.spi.QueryImplementor.class, access = PUBLIC_METHODS | DECLARED_FIELDS | DECLARED_METHODS | DECLARED_CONSTRUCTORS)
		}
)
public class HibernateHints implements NativeConfiguration {
}
