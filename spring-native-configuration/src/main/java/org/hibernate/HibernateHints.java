package org.hibernate;

import org.hibernate.query.Query;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ProxyHint;
import org.springframework.nativex.hint.TypeHint;

import static org.springframework.nativex.hint.AccessBits.*;

@NativeHint(trigger = org.hibernate.Session.class,
		proxies = {
			@ProxyHint(types = {
					org.hibernate.Session.class,
					org.springframework.orm.jpa.EntityManagerProxy.class
			}),
			@ProxyHint(types = {
					org.hibernate.SessionFactory.class,
					org.springframework.orm.jpa.EntityManagerFactoryInfo.class
			}),
			@ProxyHint(types = {
					org.hibernate.jpa.HibernateEntityManagerFactory.class,
					org.springframework.orm.jpa.EntityManagerFactoryInfo.class
			}),
			@ProxyHint(types = {Query.class, org.hibernate.query.spi.QueryImplementor.class,})
		},
		types = {
			@TypeHint(types = org.hibernate.query.spi.QueryImplementor.class, access = PUBLIC_METHODS | DECLARED_FIELDS | DECLARED_METHODS | DECLARED_CONSTRUCTORS)
		}
)
public class HibernateHints implements NativeConfiguration {
}
