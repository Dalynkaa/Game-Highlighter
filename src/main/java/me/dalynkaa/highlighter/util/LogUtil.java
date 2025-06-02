package me.dalynkaa.highlighter.util;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for convenient logging throughout the application.
 * Provides methods for different log levels with formatted messages.
 */
public class LogUtil {
    private final Logger logger;
    private final String prefix;
    private static final boolean DEV_MODE = FabricLoader.getInstance().isDevelopmentEnvironment();

    /**
     * Creates a new LogUtil instance with the specified logger
     * @param logger The SLF4J logger to use
     */
    public LogUtil(Logger logger) {
        this(logger, null);
    }

    /**
     * Creates a new LogUtil instance with the specified logger and prefix
     * @param logger The SLF4J logger to use
     * @param prefix A prefix to add to all log messages
     */
    public LogUtil(Logger logger, String prefix) {
        this.logger = logger;
        this.prefix = prefix != null ? "[" + prefix + "] " : "";
    }

    /**
     * Creates a new LogUtil instance with a logger for the specified class
     * @param clazz The class to create a logger for
     */
    public LogUtil(Class<?> clazz) {
        this(LoggerFactory.getLogger(clazz));
    }

    /**
     * Creates a new LogUtil instance with a logger for the specified class and prefix
     * @param clazz The class to create a logger for
     * @param prefix A prefix to add to all log messages
     */
    public LogUtil(Class<?> clazz, String prefix) {
        this(LoggerFactory.getLogger(clazz), prefix);
    }

    /**
     * Creates a child logger with an additional prefix
     * @param childPrefix The prefix for the child logger
     * @return A new LogUtil instance with the combined prefix
     */
    public LogUtil child(String childPrefix) {
        String newPrefix = prefix.isEmpty()
            ? childPrefix
            : prefix.substring(0, prefix.length() - 2) + ":" + childPrefix + "] ";
        return new LogUtil(logger, newPrefix);
    }

    /**
     * Logs a message at the INFO level
     * @param message The message to log
     */
    public void info(String message) {
        logger.info(prefix + message);
    }

    /**
     * Logs a formatted message at the INFO level
     * @param format The format string
     * @param args The arguments for the format string
     */
    public void info(String format, Object... args) {
        logger.info(prefix + format, args);
    }

    /**
     * Logs a message at the WARN level
     * @param message The message to log
     */
    public void warn(String message) {
        logger.warn(prefix + message);
    }

    /**
     * Logs a formatted message at the WARN level
     * @param format The format string
     * @param args The arguments for the format string
     */
    public void warn(String format, Object... args) {
        logger.warn(prefix + format, args);
    }

    /**
     * Logs a message at the ERROR level
     * @param message The message to log
     */
    public void error(String message) {
        logger.error(prefix + message);
    }

    /**
     * Logs a formatted message at the ERROR level
     * @param format The format string
     * @param args The arguments for the format string
     */
    public void error(String format, Object... args) {
        logger.error(prefix + format, args);
    }

    /**
     * Logs a message and exception at the ERROR level
     * @param message The message to log
     * @param throwable The exception to log
     */
    public void error(String message, Throwable throwable) {
        logger.error(prefix + message, throwable);
    }

    /**
     * Logs a message at the DEBUG level
     * @param message The message to log
     */
    public void debug(String message) {
        logger.debug(prefix + message);
    }

    /**
     * Logs a formatted message at the DEBUG level
     * @param format The format string
     * @param args The arguments for the format string
     */
    public void debug(String format, Object... args) {
        logger.debug(prefix + format, args);
    }

    /**
     * Logs a message at the DEBUG level if in development mode,
     * otherwise does nothing
     * @param message The message to log
     */
    public void devDebug(String message) {
        if (DEV_MODE) {
            debug(message);
        }
    }

    /**
     * Logs a formatted message at the DEBUG level if in development mode,
     * otherwise does nothing
     * @param format The format string
     * @param args The arguments for the format string
     */
    public void devDebug(String format, Object... args) {
        if (DEV_MODE) {
            debug(format, args);
        }
    }

    /**
     * Logs a message at the TRACE level
     * @param message The message to log
     */
    public void trace(String message) {
        logger.trace(prefix + message);
    }

    /**
     * Logs a formatted message at the TRACE level
     * @param format The format string
     * @param args The arguments for the format string
     */
    public void trace(String format, Object... args) {
        logger.trace(prefix + format, args);
    }
}
