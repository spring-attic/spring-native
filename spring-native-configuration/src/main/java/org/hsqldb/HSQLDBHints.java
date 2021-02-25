package org.hsqldb;

import org.hsqldb.jdbc.JDBCDriver;
import org.hsqldb.lib.FileUtil;

import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.nativex.hint.TypeHint;


@NativeHint(trigger=JDBCDriver.class, types = {
    @TypeHint( types= {FileUtil.class},
            typeNames= {"org.hsqldb.dbinfo.DatabaseInformationFull"})
}, resources = {
    @ResourceHint(patterns = {"org/hsqldb/resources/information-schema.sql", "org/hsqldb/resources/lob-schema.sql", "org/hsqldb/resources/jdklogging-default.properties"}),
    @ResourceHint(isBundle = true, patterns = "org.hsqldb.resources.sql-state-messages")
})
public class HSQLDBHints implements NativeConfiguration {

}
