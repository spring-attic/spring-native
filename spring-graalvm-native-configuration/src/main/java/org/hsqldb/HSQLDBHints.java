package org.hsqldb;

import org.hsqldb.jdbc.JDBCDriver;
import org.hsqldb.lib.FileUtil;
import org.springframework.nativex.extension.NativeImageConfiguration;
import org.springframework.nativex.extension.NativeImageHint;
import org.springframework.nativex.extension.ResourcesInfo;
import org.springframework.nativex.extension.TypeInfo;
import org.springframework.nativex.type.AccessBits;


@NativeImageHint(trigger=JDBCDriver.class, typeInfos = {
    @TypeInfo( types= {FileUtil.class},
            typeNames= {"org.hsqldb.dbinfo.DatabaseInformationFull"}, access=AccessBits.LOAD_AND_CONSTRUCT)
}, resourcesInfos = {
    @ResourcesInfo(patterns = {"org/hsqldb/resources/information-schema.sql", "org/hsqldb/resources/lob-schema.sql", "org/hsqldb/resources/jdklogging-default.properties"}),
    @ResourcesInfo(isBundle = true, patterns = "org.hsqldb.resources.sql-state-messages")
})
public class HSQLDBHints implements NativeImageConfiguration {

}
