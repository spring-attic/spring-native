package org.hsqldb;

import org.hsqldb.jdbc.JDBCDriver;
import org.hsqldb.lib.FileUtil;

import org.springframework.nativex.extension.NativeConfiguration;
import org.springframework.nativex.extension.NativeHint;
import org.springframework.nativex.extension.ResourcesInfo;
import org.springframework.nativex.extension.TypeInfo;


@NativeHint(trigger=JDBCDriver.class, typeInfos = {
    @TypeInfo( types= {FileUtil.class},
            typeNames= {"org.hsqldb.dbinfo.DatabaseInformationFull"})
}, resourcesInfos = {
    @ResourcesInfo(patterns = {"org/hsqldb/resources/information-schema.sql", "org/hsqldb/resources/lob-schema.sql", "org/hsqldb/resources/jdklogging-default.properties"}),
    @ResourcesInfo(isBundle = true, patterns = "org.hsqldb.resources.sql-state-messages")
})
public class HSQLDBHints implements NativeConfiguration {

}
