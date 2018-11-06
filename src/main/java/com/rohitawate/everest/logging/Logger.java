/*
 * Copyright 2018 Rohit Awate.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rohitawate.everest.logging;

import com.rohitawate.everest.misc.EverestUtilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Logger {
    private Level writerLevel;
    private static String logEntryTemplate;
    private static String logFilePath = "Everest/logs/" + LocalDate.now() + ".html";
    private static BufferedWriter writer;

    static {
        try {
            createLogsFile();
            writer = new BufferedWriter(new FileWriter(logFilePath, true));
        } catch (IOException e) {
            System.err.println("Could not open today's log file.");
            e.printStackTrace();
        }
    }

    Logger(Level writerLevel) {
        this.writerLevel = writerLevel;

        try {
            logEntryTemplate = EverestUtilities.readFile(getClass().getResourceAsStream("/html/LogEntry.html"));
        } catch (IOException e) {
            System.err.println("Could not read log template.");
            e.printStackTrace();
        }
    }

    /**
     * Appends the log to the respective day's log file.
     */
    synchronized void log(Log log) {
        if (log.level.equals(Level.INFO)) {
            System.out.println(ConsoleColors.BLUE + log.level + " " + log.time + ": " + log.message + ConsoleColors.RESET);
        } else if (log.level.equals(Level.SEVERE)) {
            System.out.println(ConsoleColors.RED + log.level + " " + log.time + ": " + log.message + ConsoleColors.RESET);
        } else if (log.level.equals(Level.WARNING)) {
            System.out.println(ConsoleColors.YELLOW + log.level + " " + log.time + ": " + log.message + ConsoleColors.RESET);
        }

        if (log.level.greaterThanEqualTo(this.writerLevel)) {
            try {
                writer.flush();
                writer.append(getLogEntry(log));
            } catch (IOException e) {
                System.err.println("Could not write log to file.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Generates HTML with the log information.
     * Different log levels are color-coded for improved readability.
     * <p>
     * Color codes:
     * Red = Severe
     * Yellow = Warning
     * Green = Info
     */
    private String getLogEntry(Log log) {
        String logEntry = logEntryTemplate;
        logEntry = logEntry.replace("%% LogLevel %%", log.level.toString());
        logEntry = logEntry.replace("%% Time %%", log.time);
        logEntry = logEntry.replace("%% Message %%", log.message);
        StringBuilder builder = new StringBuilder();

        if (log.exception != null) {
            StackTraceElement[] stackTrace = log.exception.getStackTrace();
            builder.append(log.exception.toString());
            builder.append("<br>\n");
            if (stackTrace.length != 0) {
                for (StackTraceElement element : log.exception.getStackTrace()) {
                    builder.append(" -- ");
                    builder.append(element.toString());
                    builder.append("<br>\n");
                }
            } else {
                builder.append("Stack trace unavailable.");
            }
        }

        logEntry = logEntry.replace("%% StackTrace %%", builder.toString());

        return logEntry;
    }

    private static void createLogsFile() {
        File logsDirectory = new File("Everest/logs/");
        if (!logsDirectory.exists())
            logsDirectory.mkdirs();

        File logsFile = new File(logFilePath);

        try {
            if (logsFile.exists())
                return;

            logsFile.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath));
            String logsFileTemplate = EverestUtilities.readFile(Logger.class.getResourceAsStream("/html/LogsFile.html"));
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            logsFileTemplate = logsFileTemplate.replace("%% Date %%", dateTimeFormatter.format(LocalDate.now()));
            logsFileTemplate = logsFileTemplate.replace("%% Date %%", dateTimeFormatter.format(LocalDate.now()));
            writer.flush();
            writer.write(logsFileTemplate);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        writer.close();
    }

    public static class ConsoleColors {
        public static final String RESET = "\u001B[0m";
        public static final String RED = "\u001B[31m";
        public static final String YELLOW = "\u001B[33m";
        public static final String BLUE = "\u001B[34m";
    }
}
