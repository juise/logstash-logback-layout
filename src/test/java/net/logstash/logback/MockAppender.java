package net.logstash.logback;

import java.util.List;
import java.util.ArrayList;

import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.AppenderBase;

import ch.qos.logback.classic.spi.ILoggingEvent;


public class MockAppender extends AppenderBase<ILoggingEvent> {
    private static LayoutBase layout;
    private static List messages = new ArrayList();

    public MockAppender(LayoutBase layout){
        this.layout = layout;
    }

    public LayoutBase getLayout() {
        return layout;
    }

    public void clear() {
        messages.clear();
    }

    public static String[] getMessages() {
        return (String[]) messages.toArray(new String[messages.size()]);
    }

    @Override
    public void start() {
        if (this.layout == null) {
            addError("No encoder set for the appender named [" + name + "].");
            return;
        }
        super.start();
    }

    @Override
    protected void append(ILoggingEvent loggingevent) {
        messages.add(layout.doLayout(loggingevent));
    }
}
