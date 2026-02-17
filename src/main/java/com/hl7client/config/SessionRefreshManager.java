package com.hl7client.config;

import com.hl7client.client.AuthRefresher;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SessionRefreshManager {

    private static final Logger LOGGER =
            Logger.getLogger(SessionRefreshManager.class.getName());

    private static final DateTimeFormatter EXP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final Duration REFRESH_BEFORE = Duration.ofMinutes(2);

    private static volatile ScheduledExecutorService scheduler;

    private SessionRefreshManager() {
    }

    // ---------- API PÚBLICA ----------

    public static synchronized void ensureStarted(AuthRefresher refresher) {
        Objects.requireNonNull(refresher, "AuthRefresher requerido para refresh");

        stop();

        String tokenExp = SessionContext.getTokenExp();
        if (tokenExp == null || tokenExp.isEmpty()) {
            LOGGER.fine("No tokenExp present, refresh scheduler not started");
            return;
        }

        long delaySeconds;
        try {
            delaySeconds = calculateDelaySeconds(tokenExp);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Invalid tokenExp format, clearing session", e);
            SessionContext.clear();
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "session-refresh-thread");
            t.setDaemon(true);
            return t;
        });

        LOGGER.info("Session refresh scheduled in " + delaySeconds + " seconds");

        scheduler.schedule(() -> refreshAndReschedule(refresher),
                delaySeconds, TimeUnit.SECONDS);
    }

    public static synchronized void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
            LOGGER.fine("Session refresh scheduler stopped");
        }
    }

    // ---------- internos ----------

    private static void refreshAndReschedule(AuthRefresher refresher) {
        try {
            refresher.refreshAuth();
            ensureStarted(refresher);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Session refresh failed, clearing session", e);
            SessionContext.clear();
            stop();
        }
    }

    private static long calculateDelaySeconds(String tokenExp) {
        LocalDateTime expTime =
                LocalDateTime.parse(tokenExp, EXP_FORMAT);

        LocalDateTime refreshTime = expTime.minus(REFRESH_BEFORE);

        long delay = Duration
                .between(LocalDateTime.now(), refreshTime)
                .getSeconds();

        return Math.max(delay, 5);
    }
}
