package com.zaxxer;

import java.sql.Statement;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.util.ConcurrentBag.IConcurrentBagEntry;

import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = HikariDataSource.class, types = {
		@TypeHint(types = HikariDataSource.class, access = TypeAccess.PUBLIC_CONSTRUCTORS),
		@TypeHint(types = Statement[].class, access = {}),
		@TypeHint(types = IConcurrentBagEntry[].class, access = {})
})
public class HikariHints implements NativeConfiguration {
}
