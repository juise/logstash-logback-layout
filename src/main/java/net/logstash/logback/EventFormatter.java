package net.logstash.logback;

import java.util.Map;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.ArrayList;

import net.logstash.logback.data.HostData;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;


public class EventFormatter {
    public static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    public static final FastDateFormat ISO8601_DATETIME_TIME_ZONE_FORMAT_WITH_MILLIS = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", UTC);

    private String message;
    private String timestamp;
    private String sourceHost;

    private String level;
    private String loggerName;
    private String threadName;

    private Map<String, String> mdc;

    private boolean exceptionInfo;
    private String stackTrace;
    private String exceptionClass;
    private String exceptionMessage;

    private String fileName;
    private String lineNumber;
    private String className;
    private String methodName;

    private IThrowableProxy throwableProxy;
    private StackTraceElement[] stackTraceElement;
    private StackTraceElementProxy[] stackTraceElementProxy;

    public EventFormatter(ILoggingEvent loggingEvent) {
        message = loggingEvent.getFormattedMessage();
        timestamp = dateFormat(loggingEvent.getTimeStamp());
        sourceHost = new HostData().getHostName();

        loggerName = loggingEvent.getLoggerName();
        threadName = loggingEvent.getThreadName();
        level = loggingEvent.getLevel().toString();

        mdc = loggingEvent.getMDCPropertyMap();

        throwableProxy = loggingEvent.getThrowableProxy();
        if (throwableProxy != null) {
            ArrayList<String> stackTraceElements = new ArrayList();
            for (StackTraceElementProxy stackTraceElement: throwableProxy.getStackTraceElementProxyArray()) {
                stackTraceElements.add(stackTraceElement.getSTEAsString());
            }
            stackTrace = StringUtils.join(stackTraceElements.toArray(), "\n");
            exceptionClass = throwableProxy.getClassName();
            exceptionMessage = throwableProxy.getMessage();
            exceptionInfo = true;
        }

        stackTraceElement = loggingEvent.getCallerData();
        fileName = stackTraceElement[0].getFileName();
        lineNumber = String.valueOf(stackTraceElement[0].getLineNumber());
        className = stackTraceElement[0].getClassName();
        methodName = stackTraceElement[0].getMethodName();
    }

    public static String dateFormat(long timestamp) {
        return ISO8601_DATETIME_TIME_ZONE_FORMAT_WITH_MILLIS.format(timestamp);
    }

    public String getMessage() {
        return message;
    }

    public String getTimeStamp() {
        return timestamp;
    }

    public String getSourceHost() {
        return sourceHost;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public String getThreadName() {
        return threadName;
    }

    public String getLevel() {
        return level;
    }

    public Map<String, String> getMDC() {
        return mdc;
    }

    public boolean hasExceptionInfo() {
        return exceptionInfo;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public String getExceptionClass() {
        return exceptionClass;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public String getFileName() {
        return fileName;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }
}
