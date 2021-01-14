package update;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLScriptExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLScriptExecutor.class);

    /**
     * executes the provided sql file.
     *<p>
     * If script file is a directory all files in that directory are executed in
     * nonspecific order. Nested directories are ignored.
     * <p>
     * Client is expected to close the connection.
     *
     * @param connection an open SQL connection
     * @param script     the script file or directory
     * @return
     */
     boolean execute(Connection connection, File script) {

        ScriptRunner scriptRunner = new ScriptRunner(connection);
        scriptRunner.setLogWriter(new NullWriter());

        if (script.isDirectory()) {
            File[] fileslist = script.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    return !pathname.isDirectory();
                }
            });

            boolean result = true;
            for (File file : fileslist) {
                result = result && executeSingleSQLFile(file, scriptRunner);
            }
            return result;

        } else {
            return executeSingleSQLFile(script, scriptRunner);
        }
    }

    private boolean executeSingleSQLFile(File script, ScriptRunner scriptRunner) {
        LOGGER.info("starting execution of " + script.getAbsolutePath());
        if (script.isDirectory()) {
            return false;
        }
        try (FileReader in = new FileReader(script);
             BufferedReader reader = new BufferedReader(in);) {

            scriptRunner.runScript(reader);
        } catch (IOException e) {
            LOGGER.error("error executing " + script.getAbsolutePath(), e);
            return false;
        }
        return true;
    }

}
