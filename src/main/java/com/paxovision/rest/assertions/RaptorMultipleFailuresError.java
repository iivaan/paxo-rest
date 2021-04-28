package com.paxovision.rest.assertions;

import static java.lang.String.format;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import java.util.List;
import java.util.stream.Collectors;
import org.opentest4j.MultipleFailuresError;

/** Raptor version of (@link MultipleFailuresError} to get more control on the error formatting. */
public class RaptorMultipleFailuresError extends MultipleFailuresError {

    private static final String STACK_TRACE_SPACES = "   ";
    private static final String LAST_LINE_STACK_TRACE_PREFIX = " ";
    private static final String ERROR_MESSAGE_PREFIX;
    private static final String LAST_LINE_ERROR_MESSAGE_PREFIX;
    private static final String STACK_TRACE_PREFIX;

    static {
        // set non-empty value to RAPTOR_UNICODE environment variable to enable Unicode output
        if (!Strings.isNullOrEmpty(System.getenv("RAPTOR_UNICODE"))) {
            ERROR_MESSAGE_PREFIX = "|---%d: ";
            LAST_LINE_ERROR_MESSAGE_PREFIX = "|___%d: ";
            STACK_TRACE_PREFIX = "|";
        } else {
            // ascii fallback mode
            ERROR_MESSAGE_PREFIX = "+---%d: ";
            LAST_LINE_ERROR_MESSAGE_PREFIX = "+---%d: ";
            STACK_TRACE_PREFIX = "|";
        }
    }

    private static final String EOL = System.getProperty("line.separator");
    private final String customHeading;

    public RaptorMultipleFailuresError(String heading, List<? extends Throwable> failures) {
        super(heading, failures);
        this.customHeading = heading;
    }

    @Override
    public String getMessage() {

        List<Throwable> failures = getFailures();
        int failureCount = failures.size();

        if (failureCount == 0) {
            return super.getMessage();
        }

        StringBuilder builder =
                new StringBuilder(EOL)
                    .append(isBlank(customHeading) ? "Multiple Failures" : customHeading.trim())
                    .append(" (")
                    .append(failureCount)
                    .append(" ")
                    .append(pluralize(failureCount, "failure", "failures"))
                    .append(")");

        for (int i = 0; i < failureCount; i++) {
            String errorMessagePrefix;
            String stackTracePrefix;

            // configure error message line prefix and stack trace line prefix for the current error
            if (i + 1 == failureCount) {
                errorMessagePrefix = EOL + format(LAST_LINE_ERROR_MESSAGE_PREFIX, i + 1);
                stackTracePrefix = LAST_LINE_STACK_TRACE_PREFIX + STACK_TRACE_SPACES;
            } else {
                errorMessagePrefix = EOL + format(ERROR_MESSAGE_PREFIX, i + 1);
                stackTracePrefix = STACK_TRACE_PREFIX + STACK_TRACE_SPACES;
            }

            builder.append(errorMessagePrefix);

            // get message from the current throwable
            final String message = nullSafeMessage(failures.get(i)).trim();

            // re-format message acctording to errorMessagePrefix and stackTracePrefix values
            final List<String> splitted =
                    Splitter.on(EOL).splitToList(message).stream()
                        .map(str -> str.startsWith("at") ? STACK_TRACE_SPACES + str : str)
                        .collect(Collectors.toList());

            final String reformattedMessage =
                    Joiner.on(EOL + stackTracePrefix).join(splitted).trim();

            builder.append(reformattedMessage);
        }
        return builder.toString();
    }

    private static boolean isBlank(String str) {
        return str == null || str.trim().length() == 0;
    }

    private static String pluralize(int count, String singular, String plural) {
        return count == 1 ? singular : plural;

    }

    private static String nullSafeMessage(Throwable failure) {
        return isBlank(failure.getMessage())
                ? "<no message> in " + failure.getClass().getName()
                : failure.getMessage();
    }




}
