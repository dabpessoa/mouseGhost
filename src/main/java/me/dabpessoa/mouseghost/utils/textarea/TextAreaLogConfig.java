package me.dabpessoa.mouseghost.utils.textarea;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

public final class TextAreaLogConfig {

    private static final String APPENDER_NAME = "TEXTAREA";
    private static final String PATTERN = "%msg%n";

    private TextAreaLogConfig() {
        // Private Constructor.
    }

    public static void register() {
        ILoggerFactory factory = LoggerFactory.getILoggerFactory();
        if (!(factory instanceof LoggerContext context)) {
            return; // Logback não é o SLF4J em uso
        }

        ch.qos.logback.classic.Logger root = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

        // Evita registrar duas vezes
        if (root.getAppender(APPENDER_NAME) != null) {
            return;
        }

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern(PATTERN);
        encoder.start();                      // o encoder precisa ser iniciado primeiro

        TextAreaAppender appender = new TextAreaAppender();
        appender.setContext(context);
        appender.setName(APPENDER_NAME);
        appender.setEncoder(encoder);
        appender.start();

        root.addAppender(appender);

        // Opcional: nível por código (o application.properties também resolve)
        context.getLogger("me.dabpessoa.mouseghost").setLevel(Level.DEBUG);
    }

    public static void unregister() {
        if (LoggerFactory.getILoggerFactory() instanceof LoggerContext context) {
            ch.qos.logback.classic.Logger root = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
            var appender = root.getAppender(APPENDER_NAME);
            if (appender != null) {
                root.detachAppender(appender);
                appender.stop();
            }
        }
    }

}