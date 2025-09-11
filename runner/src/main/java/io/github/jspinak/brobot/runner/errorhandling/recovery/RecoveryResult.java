package io.github.jspinak.brobot.runner.errorhandling.recovery;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** Result of a recovery attempt. */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RecoveryResult {

    private final boolean success;
    private final int attempts;
    private final Object result;
    private final String failureReason;
    private final long totalDuration;

    /** Create a successful recovery result. */
    public static RecoveryResult success(int attempts, Object result) {
        return new RecoveryResult(true, attempts, result, null, System.currentTimeMillis());
    }

    /** Create a failed recovery result. */
    public static RecoveryResult failure(int attempts, String reason) {
        return new RecoveryResult(false, attempts, null, reason, System.currentTimeMillis());
    }

    /** Check if the recovery was successful. */
    public boolean isSuccess() {
        return success;
    }

    /** Get a summary of the recovery result. */
    public String getSummary() {
        if (success) {
            return String.format("Recovery successful after %d attempt(s)", attempts);
        } else {
            return String.format(
                    "Recovery failed after %d attempt(s): %s", attempts, failureReason);
        }
    }
}
