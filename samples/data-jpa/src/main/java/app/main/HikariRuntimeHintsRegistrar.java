package app.main;

import java.sql.Statement;

import com.zaxxer.hikari.util.ConcurrentBag.IConcurrentBagEntry;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.util.ClassUtils;

/**
 * @author Moritz Halbritter
 */
// TODO: Contribute these hints to graalvm-reachability-metadata repository
class HikariRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		if (ClassUtils.isPresent("com.zaxxer.hikari.HikariDataSource", classLoader)) {
			hints.reflection().registerType(Statement[].class, hint -> {
			});
			hints.reflection().registerType(IConcurrentBagEntry[].class, hint -> {
			});
		}
	}

}
