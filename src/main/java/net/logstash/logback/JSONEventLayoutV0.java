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


public class JSONEventLayoutV0 extends LayoutBase<ILoggingEvent> {
    private boolean locationInfo;

    private HashMap<String, Object> fields;
    private HashMap<String, Object> exception;

    private EventFormatter event;
    private JSONObject logstashEvent;


    public JSONEventLayoutV0() {
        this(true);
    }

    public JSONEventLayoutV0(boolean locationInfo) {
        this.locationInfo = locationInfo;
    }

    public String doLayout(ILoggingEvent loggingEvent) {
        fields = new HashMap<String, Object>();
        exception = new HashMap<String, Object>();

        event = new EventFormatter(loggingEvent);
        logstashEvent = new JSONObject();

        if (event.hasExceptionInfo()) {
            exception.put("stacktrace", event.getStackTrace());
            exception.put("exception_class", event.getExceptionClass());
            exception.put("exception_message", event.getExceptionMessage());
            addField("exception", exception);
        }

        addField("file", event.getFileName());
        addField("line_number", event.getLineNumber());
        addField("class", event.getClassName());
        addField("method", event.getMethodName());

        addField("loggerName", event.getLoggerName());
        addField("threadName", event.getThreadName());
        addField("level", event.getLevel());
        addField("mdc", event.getMDC());

        logstashEvent.put("@timestamp", event.getTimeStamp());
        logstashEvent.put("@source_host", event.getSourceHost());
        logstashEvent.put("@message", event.getMessage());
        logstashEvent.put("@fields", fields);
        return logstashEvent.toString() + "\n";
    }

    private void addField(String keyname, Object keyval) {
        if (null != keyval) {
            fields.put(keyname, keyval);
        }
    }
}
