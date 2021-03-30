package com.example.quartznative;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.simpl.SimpleThreadPool;
import org.quartz.utils.DirtyFlagMap;
import org.quartz.utils.StringKeyDirtyFlagMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.nativex.hint.*;
import org.springframework.nativex.type.HintDeclaration;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.type.TypeSystem;
import org.springframework.scheduling.quartz.LocalDataSourceJobStore;
import org.springframework.scheduling.quartz.LocalTaskExecutorThreadPool;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.ResourceLoaderClassLoadHelper;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;

/**
 * todo: make sure to also register some serialization-config.json. We need some entries. See the META-INF/native-image/serialization-config.json for an example.
 * todo: extract that out into a NativeConfiguration that programatically registers the config? i dont know if Spring Native even supports serialization-config.json atm.
 */
@ResourceHint(
        patterns = {
                "org/quartz/impl/jdbcjobstore/tables_*.sql$",
                "org/quartz/impl/jdbcjobstore/tables_h2.sql$"
        }
)
@SerializationHint(
        types = {
               DirtyFlagMap.class,String.class,StringKeyDirtyFlagMap.class,JobDataMap.class,HashMap.class 
        }
)

@FieldHint(allowUnsafeAccess = true, name = "loadFactor")
@NativeHint(
        trigger = org.quartz.utils.DirtyFlagMap.class,
        types = {
                @TypeHint(types = java.util.HashMap.class),
                @TypeHint(types = SampleJob.class), // the user would still need to register this type for reflection
                @TypeHint(
                        access = AccessBits.ALL,
                        types = {
                                org.quartz.impl.triggers.SimpleTriggerImpl.class ,
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
@SpringBootApplication
public class QuartzNativeApplication {


    public static void main(String[] args) {
        SpringApplication.run(QuartzNativeApplication.class, args);
    }

    @Bean
    JobDetail sampleJobDetail(@Value("${greetings.name:World}") String name) {
        return JobBuilder
                .newJob(SampleJob.class) //
                .withIdentity("sampleJob") //
                .usingJobData("name", name) //
                .storeDurably() //
                .build();
    }

    @Bean
    Trigger sampleJobTrigger(JobDetail sampleJobDetail) {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder
                .simpleSchedule() //
                .withIntervalInSeconds(2) //
                .withRepeatCount(5);

        return TriggerBuilder//
                .newTrigger()//
                .forJob(sampleJobDetail)//
                .withIdentity("sampleTrigger") //
                .withSchedule(scheduleBuilder) //
                .build();
    }
    //
}


class SampleJob extends QuartzJobBean {

    private String name;
   
    // Invoked if a Job data map entry with that name
    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected void executeInternal(JobExecutionContext context)
            throws JobExecutionException {
        System.out.println(String.format("Hello %s @ " + Instant.now() + "!", this.name));
    }

}