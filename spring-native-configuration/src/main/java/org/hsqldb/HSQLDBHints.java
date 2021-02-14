package org.hsqldb;

import org.hsqldb.jdbc.JDBCDriver;
import org.hsqldb.lib.FileUtil;

import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ResourcesInfo;
import org.springframework.nativex.hint.TypeInfo;


@NativeHint(trigger=JDBCDriver.class, typeInfos = {
    @TypeInfo( types= {FileUtil.class},
            typeNames= {"org.hsqldb.dbinfo.DatabaseInformationFull"})
}, resourcesInfos = {
    @ResourcesInfo(patterns = {"org/hsqldb/resources/information-schema.sql", "org/hsqldb/resources/lob-schema.sql", "org/hsqldb/resources/jdklogging-default.properties"}),
    @ResourcesInfo(isBundle = true, patterns = "org.hsqldb.resources.sql-state-messages")
})
public class HSQLDBHints implements NativeConfiguration {

}
