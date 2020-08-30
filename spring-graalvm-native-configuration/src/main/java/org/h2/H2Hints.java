package org.h2;

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

@NativeImageHint(trigger= Driver.class, typeInfos= {
		@TypeInfo(
				typeNames= {"org.h2.store.fs.FilePathMemLZF","org.h2.store.fs.FilePathNioMemLZF"},
				types= {MVTableEngine.class, FilePathDisk.class, FilePathMem.class, FilePathNioMem.class, FilePathSplit.class, FilePathNio.class, FilePathNioMapped.class, FilePathAsync.class, FilePathZip.class, FilePathRetryOnInterrupt.class}
		)})

public class H2Hints implements NativeImageConfiguration {
}
