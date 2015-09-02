package net.logstash.logback;

import org.junit.Test;
import org.junit.After;
import org.junit.Ignore;
import org.junit.BeforeClass;

import junit.framework.Assert;

import net.minidev.json.JSONValue;
import net.minidev.json.JSONObject;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

import org.slf4j.MDC;
import org.slf4j.LoggerFactory;


public class JSONEventLayoutV0Test {
    static Logger logger;
    static MockAppender appender;

    static final String[] logstashFields = new String[] {
        "@message",
        "@source_host",
        "@fields",
        "@timestamp"
    };

    @BeforeClass
    public static void setupTestAppender() {
        logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        JSONEventLayoutV0 layout = new JSONEventLayoutV0();
        layout.setContext(context);
        layout.start();

        appender = new MockAppender(layout);
        appender.start();

        logger.addAppender(appender);
    }

    @After
    public void clearTestAppender() {
        appender.clear();
    }

    @Test
    public void testJSONEventLayoutIsJSON() {
        logger.info("Test1: JSON Event Layout Is JSON");
        String message = appender.getMessages()[0];

        Assert.assertTrue("Event is not valid JSON", JSONValue.isValidJsonStrict(message));
    }

    @Test
    public void testJSONEventLayoutHasKeys() {
        logger.info("Test2: JSON Event Layout Has Keys");
        String message = appender.getMessages()[0];
        Object obj = JSONValue.parse(message);
        JSONObject jsonObject = (JSONObject) obj;

        for (String fieldName : logstashFields) {
            Assert.assertTrue("Event does not contain field: " + fieldName, jsonObject.containsKey(fieldName));
        }
    }

    @Test
    public void testJSONEventLayoutHasFieldLevel() {
        logger.error("Test3: JSON Event Layout Has Field Level");
        String message = appender.getMessages()[0];
        Object obj = JSONValue.parse(message);
        JSONObject jsonObject = (JSONObject) obj;
        JSONObject atFields = (JSONObject) jsonObject.get("@fields");

        Assert.assertEquals("Log level is wrong", "ERROR", atFields.get("level"));
    }

    @Test
    public void testJSONEventLayoutHasMDC() {
        MDC.put("foo", "bar");
        logger.warn("Test4: JSON Event Layout Has MDC");
        String message = appender.getMessages()[0];
        Object obj = JSONValue.parse(message);
        JSONObject jsonObject = (JSONObject) obj;
        JSONObject atFields = (JSONObject) jsonObject.get("@fields");
        JSONObject mdcData = (JSONObject) atFields.get("mdc");

        Assert.assertEquals("MDC is wrong", "bar", mdcData.get("foo"));
    }

    @Test
    public void testJSONEventLayoutHasExceptions() {
        String exceptionMessage = "EXCEPTION MESSAGE";
        logger.error("Test5: JSON Event Layout Has Exception", new IllegalArgumentException(exceptionMessage));
        String message = appender.getMessages()[0];
        Object obj = JSONValue.parse(message);
        JSONObject jsonObject = (JSONObject) obj;
        JSONObject atFields = (JSONObject) jsonObject.get("@fields");
        JSONObject exceptionInformation = (JSONObject) atFields.get("exception");

        Assert.assertEquals("Exception class missing", "java.lang.IllegalArgumentException", exceptionInformation.get("exception_class"));
        Assert.assertEquals("Exception exception message", exceptionMessage, exceptionInformation.get("exception_message"));
    }

    @Test
    public void testJSONEventLayoutHasClassName() {
        logger.warn("Test6: JSON Event Layout Has Class Name");
        String message = appender.getMessages()[0];
        Object obj = JSONValue.parse(message);
        JSONObject jsonObject = (JSONObject) obj;
        JSONObject atFields = (JSONObject) jsonObject.get("@fields");

        Assert.assertEquals("Logged class does not match", this.getClass().getCanonicalName().toString(), atFields.get("class"));
    }

    @Test
    public void testJSONEventHasFileName() {
        logger.warn("Test7: JSON Event Layout Has File Name");
        String message = appender.getMessages()[0];
        Object obj = JSONValue.parse(message);
        JSONObject jsonObject = (JSONObject) obj;
        JSONObject atFields = (JSONObject) jsonObject.get("@fields");

        Assert.assertNotNull("File value is missing", atFields.get("file"));
    }

    @Test
    public void testJSONEventHasLoggerName() {
        logger.warn("Test8: JSON Event Layout Has Logger Name");
        String message = appender.getMessages()[0];
        Object obj = JSONValue.parse(message);
        JSONObject jsonObject = (JSONObject) obj;
        JSONObject atFields = (JSONObject) jsonObject.get("@fields");

        Assert.assertNotNull("LoggerName value is missing", atFields.get("loggerName"));
    }

    @Test
    public void testJSONEventHasThreadName() {
        logger.warn("Test9: JSON Event Layout Has Thread Name");
        String message = appender.getMessages()[0];
        Object obj = JSONValue.parse(message);
        JSONObject jsonObject = (JSONObject) obj;
        JSONObject atFields = (JSONObject) jsonObject.get("@fields");

        Assert.assertNotNull("ThreadName value is missing", atFields.get("threadName"));
    }
}
