/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.shared.utils.logging;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Colored message utils, to manage colors consistently across plugins (only if Maven version is at least 3.5.0).
 * For Maven version before 3.5.0, message built with this util will never add color.
 * <p>
 * Colors are written as <a href="https://en.wikipedia.org/wiki/ANSI_escape_code#Colors">ANSI escape sequences</a>.
 * Whether they are written follows the color setting of the running Maven, read from the
 * {@code org.fusesource.jansi} API that Maven core exports to plugins. This library does not depend on
 * Jansi: when that API is not visible, messages are never colored.
 *
 * @since 3.1.0
 * @deprecated migrate to {@code org.apache.maven.message.MessageBuilderFactory} (Maven 3.10.0 and later)
 *             or {@code org.apache.maven.api.services.MessageBuilderFactory} (Maven 4) once your
 *             plugin or extension requires one of those Maven versions. Until then, keep using
 *             this API; it remains supported.
 */
@Deprecated
public class MessageUtils {
    private static final String JANSI_PACKAGE = "org.fusesource.jansi.";

    /** System property read by Jansi's {@code AnsiConsole}, see {@code AnsiConsole.JANSI_MODE}. */
    private static final String JANSI_MODE = "jansi.mode";

    private static final boolean JANSI;

    /** Whether AnsiConsole, AnsiPrintStream and AnsiMode are visible too. */
    private static boolean jansiConsole;

    private static Method ansiIsEnabled;
    private static Method ansiSetEnabled;
    private static Method consoleSystemInstall;
    private static Method consoleSystemUninstall;
    private static Method consoleIsInstalled;
    private static Method consoleOut;
    private static Method consoleGetTerminalWidth;
    private static Method printStreamSetMode;
    private static Object modeForce;
    private static Object modeStrip;

    /** Reference to the JVM shutdown hook, if registered. */
    private static Thread shutdownHook;

    /** Synchronization monitor for the "uninstall". */
    private static final Object STARTUP_SHUTDOWN_MONITOR = new Object();

    static {
        // Jansi API is provided by Maven core since 3.5.0; Maven 3.10 provides a compatible subset.
        // Color only needs Ansi, as before; the console classes are optional.
        ClassLoader loader = MessageUtils.class.getClassLoader();
        boolean jansi;
        try {
            Class<?> ansi = Class.forName(JANSI_PACKAGE + "Ansi", false, loader);
            ansiIsEnabled = ansi.getMethod("isEnabled");
            ansiSetEnabled = ansi.getMethod("setEnabled", boolean.class);
            jansi = true;
        } catch (ReflectiveOperationException | LinkageError e) {
            jansi = false;
        }
        JANSI = jansi;
        if (jansi) {
            try {
                Class<?> console = Class.forName(JANSI_PACKAGE + "AnsiConsole", false, loader);
                Class<?> printStream = Class.forName(JANSI_PACKAGE + "AnsiPrintStream", false, loader);
                Class<?> mode = Class.forName(JANSI_PACKAGE + "AnsiMode", false, loader);
                consoleSystemInstall = console.getMethod("systemInstall");
                consoleSystemUninstall = console.getMethod("systemUninstall");
                consoleIsInstalled = console.getMethod("isInstalled");
                consoleOut = console.getMethod("out");
                consoleGetTerminalWidth = console.getMethod("getTerminalWidth");
                printStreamSetMode = printStream.getMethod("setMode", mode);
                modeForce = enumConstant(mode, "Force");
                modeStrip = enumConstant(mode, "Strip");
                jansiConsole = true;
            } catch (ReflectiveOperationException | LinkageError e) {
                jansiConsole = false;
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object enumConstant(Class<?> type, String name) {
        return Enum.valueOf((Class) type, name);
    }

    private static Object invoke(Method method, Object target, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new IllegalStateException(cause);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean isInstalled() {
        return jansiConsole && (Boolean) invoke(consoleIsInstalled, null);
    }

    /**
     * Install color support.
     * This method is called by Maven core, and calling it is not necessary in plugins.
     */
    public static void systemInstall() {
        if (jansiConsole) {
            invoke(consoleSystemInstall, null);
        }
    }

    /**
     * Undo a previous {@link #systemInstall()}.  If {@link #systemInstall()} was called
     * multiple times, {@link #systemUninstall()} must be called call the same number of times before
     * it is actually uninstalled.
     */
    public static void systemUninstall() {
        synchronized (STARTUP_SHUTDOWN_MONITOR) {
            doSystemUninstall();

            // hook can only set when Jansi is true
            if (shutdownHook != null) {
                try {
                    Runtime.getRuntime().removeShutdownHook(shutdownHook);
                } catch (IllegalStateException ex) {
                    // ignore - VM is already shutting down
                }
            }
        }
    }

    private static void doSystemUninstall() {
        if (jansiConsole) {
            invoke(consoleSystemUninstall, null);
        }
    }

    /**
     * Enables message color (if Jansi is available).
     *
     * @param flag to enable Jansi
     */
    public static void setColorEnabled(boolean flag) {
        if (JANSI) {
            if (jansiConsole) {
                invoke(printStreamSetMode, invoke(consoleOut, null), flag ? modeForce : modeStrip);
            }
            invoke(ansiSetEnabled, null, flag);
            System.setProperty(JANSI_MODE, flag ? "force" : "strip");
            boolean installed = isInstalled();
            while (isInstalled()) {
                invoke(consoleSystemUninstall, null);
            }
            if (installed) {
                invoke(consoleSystemInstall, null);
            }
        }
    }

    /**
     * Is message color enabled: requires Jansi available (through Maven) and the color has not been disabled.
     *
     * @return whether colored messages are enabled
     */
    public static boolean isColorEnabled() {
        return JANSI && (Boolean) invoke(ansiIsEnabled, null);
    }

    /**
     * Create a default message buffer.
     *
     * @return a new buffer
     */
    public static MessageBuilder buffer() {
        return JANSI ? new AnsiMessageBuilder() : new PlainMessageBuilder();
    }

    /**
     * Create a message buffer with defined String builder.
     *
     * @param builder initial content of the message buffer
     * @return a new buffer
     */
    public static MessageBuilder buffer(StringBuilder builder) {
        return JANSI ? new AnsiMessageBuilder(builder) : new PlainMessageBuilder(builder);
    }

    /**
     * Create a message buffer with an internal buffer of defined size.
     *
     * @param size size of the buffer
     * @return a new buffer
     */
    public static MessageBuilder buffer(int size) {
        return JANSI ? new AnsiMessageBuilder(size) : new PlainMessageBuilder(size);
    }

    /**
     * Create a logger level renderer.
     *
     * @return a logger level renderer
     * @since 3.2.0
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public static LoggerLevelRenderer level() {
        return JANSI ? new AnsiMessageBuilder(20) : new PlainMessageBuilder(7);
    }

    /**
     * Remove any ANSI code from a message (colors or other escape sequences).
     *
     * @param msg message eventually containing ANSI codes
     * @return the message with ANSI codes removed
     */
    public static String stripAnsiCodes(String msg) {
        return msg.replaceAll("\u001B\\[[;\\d]*[ -/]*[@-~]", "");
    }

    /**
     * Register a shutdown hook with the JVM runtime, uninstalling Ansi support on
     * JVM shutdown unless is has already been uninstalled at that time.
     * <p>Delegates to {@link #doSystemUninstall()} for the actual uninstall procedure.
     *
     * @see Runtime#addShutdownHook(Thread)
     * @see MessageUtils#systemUninstall()
     * @see #doSystemUninstall()
     */
    public static void registerShutdownHook() {
        if (jansiConsole && shutdownHook == null) {
            // No shutdown hook registered yet.
            shutdownHook = new Thread(() -> {
                synchronized (STARTUP_SHUTDOWN_MONITOR) {
                    while (isInstalled()) {
                        doSystemUninstall();
                    }
                }
            });
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        }
    }

    /**
     * Get the terminal width or -1 if the width cannot be determined.
     *
     * @return the terminal width
     */
    public static int getTerminalWidth() {
        if (jansiConsole) {
            int width = (Integer) invoke(consoleGetTerminalWidth, null);
            return width > 0 ? width : -1;
        } else {
            return -1;
        }
    }
}
