package inc.osbay.android.tutormandarin.util;

import org.apache.log4j.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.mindpipe.android.logging.log4j.LogConfigurator;
import inc.osbay.android.tutormandarin.sdk.constant.CommonConstant;

public class Log4jHelper {
    private final static LogConfigurator mLogConfigurator = new LogConfigurator();

    public Log4jHelper(){
        configureLog4j();
    }

    private void configureLog4j() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        String fileName = CommonConstant.LOG_PATH + File.separator + formatter.format(new Date()) + ".log";
        File logPath = new File(CommonConstant.LOG_PATH);
        if (!logPath.exists()) {
            logPath.mkdirs();
        }

        String filePattern = "%d - [%c] - %p : %m%n";
        int maxBackupSize = 10;
        long maxFileSize = 1024 * 1024;

        configure(fileName, filePattern, maxBackupSize, maxFileSize);
    }

    private void configure(String fileName, String filePattern, int maxBackupSize, long maxFileSize) {
        mLogConfigurator.setFileName(fileName);
        mLogConfigurator.setMaxFileSize(maxFileSize);
        mLogConfigurator.setFilePattern(filePattern);
        mLogConfigurator.setMaxBackupSize(maxBackupSize);
        mLogConfigurator.setUseLogCatAppender(true);
        mLogConfigurator.configure();
    }

    public Logger getLogger(String name) {
        return Logger.getLogger(name);
    }
}