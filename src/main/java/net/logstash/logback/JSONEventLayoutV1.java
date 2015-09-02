package net.logstash.logback;

import net.minidev.json.JSONObject;

import net.logstash.logback.EventFormatter;
import net.logstash.logback.data.HostData;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;

import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.Map;
import java.util.HashMap;
import java.util.TimeZone;


public class JSONEventLayoutV1 extends LayoutBase<ILoggingEvent> {
    private String userFields;
    private boolean locationInfo;

    private static final Integer version = 1;
    public static String ADDITIONAL_DATA_PROPERTY = "net.logstash.logback.JSONEventLayoutV1.UserFields";

    private HashMap<String, Object> exception;

    private EventFormatter event;
    private JSONObject logstashEvent;


    public JSONEventLayoutV1() {
        this(true);
    }

    public JSONEventLayoutV1(boolean locationInfo) {
        this.locationInfo = locationInfo;
    }

    public String doLayout(ILoggingEvent loggingEvent) {
        exception = new HashMap<String, Object>();

        event = new EventFormatter(loggingEvent);
        logstashEvent = new JSONObject();

        if (event.hasExceptionInfo()) {
            exception.put("stacktrace", event.getStackTrace());
            exception.put("exception_class", event.getExceptionClass());
            exception.put("exception_message", event.getExceptionMessage());
            logstashEvent.put("exception", exception);
        }

        logstashEvent.put("file", event.getFileName());
        logstashEvent.put("line_number", event.getLineNumber());
        logstashEvent.put("class", event.getClassName());
        logstashEvent.put("method", event.getMethodName());

        logstashEvent.put("logger_name", event.getLoggerName());
        logstashEvent.put("thread_name", event.getThreadName());
        logstashEvent.put("level", event.getLevel());
        logstashEvent.put("mdc", event.getMDC());

        logstashEvent.put("@timestamp", event.getTimeStamp());
        logstashEvent.put("@version", version);
        logstashEvent.put("source_host", event.getSourceHost());
        logstashEvent.put("message", event.getMessage());

        if (userFields != null) {
            addUserFields(userFields);
        }

        String additionalUserFields = System.getProperty(ADDITIONAL_DATA_PROPERTY);
        if (additionalUserFields != null) {
            addUserFields(additionalUserFields);
        }

        return logstashEvent.toString() + "\n";
    }

    public String getUserFields() {
        return userFields;
    }

    public void setUserFields(String userFields) {
        this.userFields = userFields;
    }

    private void addUserFields(String data) {
        if (null != data) {
            String[] pairs = data.split(",");
            for (String pair : pairs) {
                String[] userField = pair.split(":", 2);
                if (userField[0] != null) {
                    String key = userField[0];
                    String val = userField[1];
                    logstashEvent.put(key, val);
                }
            }
        }
    }

}
