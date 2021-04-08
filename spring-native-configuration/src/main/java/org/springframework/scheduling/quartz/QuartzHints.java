package org.springframework.scheduling.quartz;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.simpl.SimpleThreadPool;
import org.quartz.utils.DirtyFlagMap;
import org.quartz.utils.StringKeyDirtyFlagMap;
import org.springframework.nativex.hint.*;
import org.springframework.nativex.type.HintDeclaration;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.type.Type;
import org.springframework.nativex.type.TypeSystem;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
	* Supports running scheduled Quartz jobs with Spring
	*/
@ResourceHint(
	patterns = {
		"org/quartz/impl/jdbcjobstore/tables_.*.sql$"
	}
)
@SerializationHint(
	types = {
		DirtyFlagMap.class, Long.class, Integer.class,
		Boolean.class, Double.class, Float.class,
		Date.class, String.class, StringKeyDirtyFlagMap.class,
		JobDataMap.class, HashMap.class
	}
)
@NativeHint(
	types = @TypeHint(
		types = CopyOnWriteArrayList.class,
		fields = {
			@FieldHint(
				allowWrite = true,
				allowUnsafeAccess = true,
				name = "lock"
			)
		},
		access = AccessBits.ALL
	)
)
@NativeHint(
	types = @TypeHint(
		types = HashMap.class,
		fields = {
			@FieldHint(
				allowUnsafeAccess = true, name = "threshold"
			),
			@FieldHint(
				allowUnsafeAccess = true, name = "loadFactor"
			)
		},
		access = AccessBits.ALL
	)
)
@NativeHint(
	trigger = org.quartz.utils.DirtyFlagMap.class,
	types = {
		@TypeHint(types = java.util.HashMap.class),
		@TypeHint(
			access = AccessBits.ALL,
			types = {
				org.quartz.impl.triggers.SimpleTriggerImpl.class,
				org.quartz.utils.DirtyFlagMap.class,
				org.quartz.impl.jdbcjobstore.StdJDBCDelegate.class,
				org.quartz.simpl.RAMJobStore.class,
				LocalDataSourceJobStore.class,
				SimpleThreadPool.class,
				ResourceLoaderClassLoadHelper.class,
				LocalTaskExecutorThreadPool.class,
				StdSchedulerFactory.class,
			}
		)}
)
public class QuartzHints implements NativeConfiguration {

	@Override
	public List<HintDeclaration> computeHints(TypeSystem typeSystem) {
//		Type jobClazz = typeSystem.resolve(Job.class.getTypeName()) ;
		return new ArrayList<>();
	}
}
