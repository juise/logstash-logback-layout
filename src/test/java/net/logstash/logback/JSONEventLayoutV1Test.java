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

import java.util.HashMap;


public class JSONEventLayoutV1Test {
    static Logger logger;
    static MockAppender appender;

    static String userFieldsSingle = new String("field1:value1");
    static String userFieldsMulti = new String("field2:value2,field3:value3");
    static String userFieldsSingleProperty = new String("field1:propval1");

    static final String[] logstashFields = new String[]{
        "message",
        "source_host",
        "@timestamp",
        "@version"
    };

    @BeforeClass
    public static void setupTestAppender() {
        logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        JSONEventLayoutV1 layout = new JSONEventLayoutV1();
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

    public void testJSONEventLayoutHasFieldLevel() {
        logger.error("Test3: JSON Event Layout Has Field Level");
        String message = appender.getMessages()[0];
        Object obj = JSONValue.parse(message);
        JSONObject jsonObject = (JSONObject) obj;

        Assert.assertEquals("Log level is wrong", "ERROR", jsonObject.get("level"));
    }

    @Test
    public void testJSONEventLayoutHasMDC() {
        MDC.put("foo", "bar");
        logger.warn("Test4: JSON Event Layout Has MDC");
        String message = appender.getMessages()[0];
        Object obj = JSONValue.parse(message);
        JSONObject jsonObject = (JSONObject) obj;
        JSONObject mdc = (JSONObject) jsonObject.get("mdc");

        Assert.assertEquals("MDC is wrong", "bar", mdc.get("foo"));
    }

    @Test
    public void testJSONEventLayoutHasExceptions() {
        String exceptionMessage = "EXCEPTION MESSAGE";
        logger.error("Test5: JSON Event Layout Has Exception", new IllegalArgumentException(exceptionMessage));
        String message = appender.getMessages()[0];
        Object obj = JSONValue.parse(message);
        JSONObject jsonObject = (JSONObject) obj;
        JSONObject exceptionInformation = (JSONObject) jsonObject.get("exception");

        Assert.assertEquals("Exception class missing", "java.lang.IllegalArgumentException", exceptionInformation.get("exception_class"));
        Assert.assertEquals("Exception exception message", exceptionMessage, exceptionInformation.get("exception_message"));
    }

    @Test
    public void testJSONEventLayoutHasClassName() {
        logger.warn("Test6: JSON Event Layout Has Class Name");
        String message = appender.getMessages()[0];
        Object obj = JSONValue.parse(message);
        JSONObject jsonObject = (JSONObject) obj;

        Assert.assertEquals("Logged class does not match", this.getClass().getCanonicalName().toString(), jsonObject.get("class"));
    }

    @Test
    public void testJSONEventHasFileName() {
        logger.warn("Test7: JSON Event Layout Has File Name");
        String message = appender.getMessages()[0];
        Object obj = JSONValue.parse(message);
        JSONObject jsonObject = (JSONObject) obj;

        Assert.assertNotNull("File value is missing", jsonObject.get("file"));
    }

    @Test
    public void testJSONEventHasLoggerName() {
        logger.warn("Test8: JSON Event Layout Has Logger Name");
        String message = appender.getMessages()[0];
        Object obj = JSONValue.parse(message);
        JSONObject jsonObject = (JSONObject) obj;

        Assert.assertNotNull("LoggerName value is missing", jsonObject.get("logger_name"));
    }

    @Test
    public void testJSONEventHasThreadName() {
        logger.warn("Test9: JSON Event Layout Has Thread Name");
        String message = appender.getMessages()[0];
        Object obj = JSONValue.parse(message);
        JSONObject jsonObject = (JSONObject) obj;

        Assert.assertNotNull("ThreadName value is missing", jsonObject.get("thread_name"));
    }

    @Test
    public void testJSONEventLayoutHasUserFieldsFromProps() {
        System.setProperty(JSONEventLayoutV1.ADDITIONAL_DATA_PROPERTY, userFieldsSingleProperty);
        logger.info("Test9: JSON Event Layout Has User Fields From Props");
        String message = appender.getMessages()[0];

        Assert.assertTrue("Event is not valid JSON", JSONValue.isValidJsonStrict(message));

        Object obj = JSONValue.parse(message);
        JSONObject jsonObject = (JSONObject) obj;

        Assert.assertTrue("Event does not contain field 'field1'" , jsonObject.containsKey("field1"));
        Assert.assertEquals("Event does not contain value 'value1'", "propval1", jsonObject.get("field1"));

        System.clearProperty(JSONEventLayoutV1.ADDITIONAL_DATA_PROPERTY);
    }

    @Test
    public void testJSONEventLayoutHasUserFieldsFromConfig() {
        JSONEventLayoutV1 layout = (JSONEventLayoutV1) appender.getLayout();
        layout.setUserFields(userFieldsSingle);

        logger.info("Test10: JSON Event Layout Has User Fields From Config");
        String message = appender.getMessages()[0];

        Assert.assertTrue("Event is not valid JSON", JSONValue.isValidJsonStrict(message));

        Object obj = JSONValue.parse(message);
        JSONObject jsonObject = (JSONObject) obj;

        Assert.assertTrue("Event does not contain field 'field1'" , jsonObject.containsKey("field1"));
        Assert.assertEquals("Event does not contain value 'value1'", "value1", jsonObject.get("field1"));
    }

    @Test
    public void testJSONEventLayoutUserFieldsMulti() {
        JSONEventLayoutV1 layout = (JSONEventLayoutV1) appender.getLayout();
        layout.setUserFields(userFieldsMulti);

        logger.info("Test11: JSON Event Layout Has User Fields Multi");
        String message = appender.getMessages()[0];

        Assert.assertTrue("Event is not valid JSON", JSONValue.isValidJsonStrict(message));

        Object obj = JSONValue.parse(message);
        JSONObject jsonObject = (JSONObject) obj;

        Assert.assertTrue("Event does not contain field 'field2'" , jsonObject.containsKey("field2"));
        Assert.assertEquals("Event does not contain value 'value2'", "value2", jsonObject.get("field2"));
        Assert.assertTrue("Event does not contain field 'field3'" , jsonObject.containsKey("field3"));
        Assert.assertEquals("Event does not contain value 'value3'", "value3", jsonObject.get("field3"));
    }

    @Test
    public void testJSONEventLayoutUserFieldsPropOverride() {
        JSONEventLayoutV1 layout = (JSONEventLayoutV1) appender.getLayout();
        layout.setUserFields(userFieldsSingle);

        System.setProperty(JSONEventLayoutV1.ADDITIONAL_DATA_PROPERTY, userFieldsSingleProperty);

        logger.info("Test11: JSON Event Layout Has User Fields Prop Override");
        String message = appender.getMessages()[0];

        Assert.assertTrue("Event is not valid JSON", JSONValue.isValidJsonStrict(message));

        Object obj = JSONValue.parse(message);
        JSONObject jsonObject = (JSONObject) obj;

        Assert.assertTrue("Event does not contain field 'field1'" , jsonObject.containsKey("field1"));
        Assert.assertEquals("Event does not contain value 'propval1'", "propval1", jsonObject.get("field1"));

        System.clearProperty(JSONEventLayoutV1.ADDITIONAL_DATA_PROPERTY);

    }
}
