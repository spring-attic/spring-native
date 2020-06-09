package org.springframework.boot.autoconfigure.h2;

import java.sql.Statement;

import org.h2.mvstore.db.MVTableEngine;
import org.h2.store.fs.FilePathAsync;
import org.h2.store.fs.FilePathDisk;
import org.h2.store.fs.FilePathMem;
import org.h2.store.fs.FilePathNio;
import org.h2.store.fs.FilePathNioMapped;
import org.h2.store.fs.FilePathNioMem;
import org.h2.store.fs.FilePathRetryOnInterrupt;
import org.h2.store.fs.FilePathSplit;
import org.h2.store.fs.FilePathZip;

import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.graalvm.type.AccessBits;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;

@NativeImageHint(typeInfos= {
		@TypeInfo(types = {MVTableEngine.class, Statement.class,Statement[].class}),
		@TypeInfo(types= EmbeddedDatabase.class,typeNames="org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory$EmbeddedDataSourceProxy",
				access= AccessBits.CLASS|AccessBits.DECLARED_CONSTRUCTORS|AccessBits.DECLARED_METHODS),
		@TypeInfo(
				access=AccessBits.CLASS|AccessBits.DECLARED_CONSTRUCTORS,
				typeNames= {
						"org.h2.store.fs.FilePathMemLZF","org.h2.store.fs.FilePathNioMemLZF"},
				types= {
						FilePathDisk.class, FilePathMem.class, FilePathNioMem.class, FilePathSplit.class, FilePathNio.class, FilePathNioMapped.class, FilePathAsync.class, FilePathZip.class, FilePathRetryOnInterrupt.class}),
		@TypeInfo(typeNames= "org.springframework.boot.autoconfigure.jdbc.DataSourceInitializerPostProcessor",access=AccessBits.FULL_REFLECTION)
		})
public class Hints implements NativeImageConfiguration {
}
