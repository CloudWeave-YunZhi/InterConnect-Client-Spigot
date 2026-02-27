/*
 * InterConnect-Client-Spigot
 * Copyright (C) 2024 CloudWeave-YunZhi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This file is based on bStats Metrics class.
 */

package com.cloudweave.yunzhi.interconnect.metrics;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

public class Metrics {

    private final Plugin plugin;
    private final MetricsBase metricsBase;

    public Metrics(JavaPlugin plugin, int serviceId) {
        this.plugin = plugin;
        File bStatsFolder = new File(plugin.getDataFolder().getParentFile(), "bStats");
        File configFile = new File(bStatsFolder, "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        if (!config.isSet("serverUuid")) {
            config.addDefault("enabled", true);
            config.addDefault("serverUuid", UUID.randomUUID().toString());
            config.addDefault("logFailedRequests", false);
            config.addDefault("logSentData", false);
            config.addDefault("logResponseStatusText", false);
            try {
                config.save(configFile);
            } catch (IOException ignored) { }
        }
        metricsBase = new MetricsBase(
                "bukkit",
                config.getString("serverUuid"),
                serviceId,
                config.getBoolean("enabled", true),
                this::appendPlatformData,
                this::appendServiceData,
                task -> Bukkit.getScheduler().runTask(plugin, task),
                plugin::isEnabled,
                (message, error) -> plugin.getLogger().log(Level.WARNING, message, error),
                (message) -> plugin.getLogger().log(Level.INFO, message),
                config.getBoolean("logFailedRequests", false),
                config.getBoolean("logSentData", false),
                config.getBoolean("logResponseStatusText", false),
                false
        );
    }

    public void addCustomChart(CustomChart chart) {
        metricsBase.addCustomChart(chart);
    }

    private void appendPlatformData(JsonObjectBuilder builder) {
        builder.appendField("playerAmount", getPlayerAmount());
        builder.appendField("onlineMode", Bukkit.getOnlineMode() ? 1 : 0);
        builder.appendField("bukkitVersion", Bukkit.getVersion());
        builder.appendField("bukkitName", Bukkit.getName());
        builder.appendField("javaVersion", System.getProperty("java.version"));
        builder.appendField("osName", System.getProperty("os.name"));
        builder.appendField("osArch", System.getProperty("os.arch"));
        builder.appendField("osVersion", System.getProperty("os.version"));
        builder.appendField("coreCount", Runtime.getRuntime().availableProcessors());
    }

    private void appendServiceData(JsonObjectBuilder builder) {
        builder.appendField("pluginVersion", plugin.getDescription().getVersion());
    }

    private int getPlayerAmount() {
        try {
            Method onlinePlayersMethod = Class.forName("org.bukkit.Server").getMethod("getOnlinePlayers");
            return onlinePlayersMethod.getReturnType().equals(Collection.class)
                    ? ((Collection<?>) onlinePlayersMethod.invoke(Bukkit.getServer())).size()
                    : ((Player[]) onlinePlayersMethod.invoke(Bukkit.getServer())).length;
        } catch (Exception e) {
            return Bukkit.getOnlinePlayers().size();
        }
    }

    public static abstract class CustomChart {
        final String chartId;
        CustomChart(String chartId) {
            this.chartId = chartId;
        }
        protected JsonObjectBuilder.JsonObject getRequestJsonObject(
                BiConsumer<String, Throwable> errorLogger,
                boolean logErrors
        ) {
            JsonObjectBuilder builder = new JsonObjectBuilder();
            builder.appendField("chartId", chartId);
            try {
                JsonObjectBuilder.JsonObject data = getChartData();
                if (data == null) return null;
                builder.appendField("data", data);
            } catch (Throwable t) {
                if (logErrors) errorLogger.accept("Failed to get data for custom chart with id " + chartId, t);
                return null;
            }
            return builder.build();
        }
        protected abstract JsonObjectBuilder.JsonObject getChartData() throws Exception;
    }

    public static class SimplePie extends CustomChart {
        private final Callable<String> callable;
        public SimplePie(String chartId, Callable<String> callable) {
            super(chartId);
            this.callable = callable;
        }
        @Override
        protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
            String value = callable.call();
            if (value == null || value.isEmpty()) return null;
            return new JsonObjectBuilder().appendField("value", value).build();
        }
    }

    public static class MetricsBase {
        public static final String METRICS_VERSION = "3.0.2";
        private static final String REPORT_URL = "https://bStats.org/api/v2/data/%s";
        private final ScheduledExecutorService scheduler;
        private final String platform;
        private final String serverUuid;
        private final int serviceId;
        private final Consumer<JsonObjectBuilder> appendPlatformDataConsumer;
        private final Consumer<JsonObjectBuilder> appendServiceDataConsumer;
        private final Consumer<Runnable> submitTaskConsumer;
        private final Supplier<Boolean> checkServiceEnabledSupplier;
        private final BiConsumer<String, Throwable> errorLogger;
        private final Consumer<String> infoLogger;
        private final boolean logErrors;
        private final boolean logSentData;
        private final boolean logResponseStatusText;
        private final Set<CustomChart> customCharts = new HashSet<>();
        private final boolean enabled;

        public MetricsBase(
                String platform,
                String serverUuid,
                int serviceId,
                boolean enabled,
                Consumer<JsonObjectBuilder> appendPlatformDataConsumer,
                Consumer<JsonObjectBuilder> appendServiceDataConsumer,
                Consumer<Runnable> submitTaskConsumer,
                Supplier<Boolean> checkServiceEnabledSupplier,
                BiConsumer<String, Throwable> errorLogger,
                Consumer<String> infoLogger,
                boolean logErrors,
                boolean logSentData,
                boolean logResponseStatusText,
                boolean skipRelocateCheck
        ) {
            this.platform = platform;
            this.serverUuid = serverUuid;
            this.serviceId = serviceId;
            this.enabled = enabled;
            this.appendPlatformDataConsumer = appendPlatformDataConsumer;
            this.appendServiceDataConsumer = appendServiceDataConsumer;
            this.submitTaskConsumer = submitTaskConsumer;
            this.checkServiceEnabledSupplier = checkServiceEnabledSupplier;
            this.errorLogger = errorLogger;
            this.infoLogger = infoLogger;
            this.logErrors = logErrors;
            this.logSentData = logSentData;
            this.logResponseStatusText = logResponseStatusText;
            this.scheduler = new ScheduledThreadPoolExecutor(1, task -> new Thread(task, "bStats-Metrics"));
            if (enabled) startSubmitting();
        }

        public void addCustomChart(CustomChart chart) {
            customCharts.add(chart);
        }

        private void startSubmitting() {
            final Runnable submitTask = () -> {
                if (!enabled || !checkServiceEnabledSupplier.get()) {
                    scheduler.shutdown();
                    return;
                }
                if (submitTaskConsumer != null) {
                    submitTaskConsumer.accept(this::submitData);
                } else {
                    this.submitData();
                }
            };
            long initialDelay = (long) (1000 * 60 * (3 + Math.random() * 3));
            long secondDelay = (long) (1000 * 60 * (Math.random() * 30));
            scheduler.schedule(submitTask, initialDelay, TimeUnit.MILLISECONDS);
            scheduler.scheduleAtFixedRate(submitTask, initialDelay + secondDelay, 1000 * 60 * 30, TimeUnit.MILLISECONDS);
        }

        private void submitData() {
            final JsonObjectBuilder baseJsonBuilder = new JsonObjectBuilder();
            appendPlatformDataConsumer.accept(baseJsonBuilder);
            final JsonObjectBuilder serviceJsonBuilder = new JsonObjectBuilder();
            appendServiceDataConsumer.accept(serviceJsonBuilder);
            JsonObjectBuilder.JsonObject[] chartData = customCharts.stream()
                    .map(customChart -> customChart.getRequestJsonObject(errorLogger, logErrors))
                    .filter(Objects::nonNull)
                    .toArray(JsonObjectBuilder.JsonObject[]::new);
            serviceJsonBuilder.appendField("id", serviceId);
            serviceJsonBuilder.appendField("customCharts", chartData);
            baseJsonBuilder.appendField("service", serviceJsonBuilder.build());
            baseJsonBuilder.appendField("serverUUID", serverUuid);
            baseJsonBuilder.appendField("metricsVersion", METRICS_VERSION);
            JsonObjectBuilder.JsonObject data = baseJsonBuilder.build();
            if (logSentData) infoLogger.accept("Sent bStats metrics data: " + data.toString());
            String url = String.format(REPORT_URL, platform);
            try {
                sendData(data, url);
            } catch (Exception e) {
                if (logErrors) errorLogger.accept("Could not submit bStats metrics data", e);
            }
        }

        private void sendData(JsonObjectBuilder.JsonObject data, String url) throws Exception {
            if (logSentData) {
                infoLogger.accept("Sending bStats metrics data to " + url + "...");
            }
            HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            byte[] compressedData = compress(data.toString());
            connection.setRequestMethod("POST");
            connection.addRequestProperty("Accept", "application/json");
            connection.addRequestProperty("Connection", "close");
            connection.addRequestProperty("Content-Encoding", "gzip");
            connection.addRequestProperty("Content-Length", String.valueOf(compressedData.length));
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "Metrics-Service/1");
            connection.setDoOutput(true);
            try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                outputStream.write(compressedData);
            }
            StringBuilder builder = new StringBuilder();
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    builder.append(line);
                }
            }
            if (logResponseStatusText) {
                infoLogger.accept("Sent data to bStats and received response: " + builder);
            }
        }

        private byte[] compress(String str) throws IOException {
            if (str == null) return null;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (GZIPOutputStream gzip = new GZIPOutputStream(outputStream)) {
                gzip.write(str.getBytes(StandardCharsets.UTF_8));
            }
            return outputStream.toByteArray();
        }
    }

    public static class JsonObjectBuilder {
        private StringBuilder builder = new StringBuilder();
        private boolean hasAtLeastOneField = false;

        public JsonObjectBuilder() {
            builder.append("{");
        }

        public JsonObjectBuilder appendField(String key, String value) {
            if (value == null) throw new IllegalArgumentException("JSON value must not be null");
            appendFieldUnescaped(key, "\"" + escape(value) + "\"");
            return this;
        }

        public JsonObjectBuilder appendField(String key, int value) {
            appendFieldUnescaped(key, String.valueOf(value));
            return this;
        }

        public JsonObjectBuilder appendField(String key, JsonObject value) {
            if (value == null) throw new IllegalArgumentException("JSON value must not be null");
            appendFieldUnescaped(key, value.toString());
            return this;
        }

        public JsonObjectBuilder appendField(String key, JsonObject[] values) {
            if (values == null) throw new IllegalArgumentException("JSON values must not be null");
            String escapedValues = Arrays.stream(values).map(JsonObject::toString).collect(Collectors.joining(","));
            appendFieldUnescaped(key, "[" + escapedValues + "]");
            return this;
        }

        private void appendFieldUnescaped(String key, String escapedValue) {
            if (builder == null) throw new IllegalStateException("JSON has already been built");
            if (hasAtLeastOneField) builder.append(",");
            builder.append("\"").append(escape(key)).append("\":").append(escapedValue);
            hasAtLeastOneField = true;
        }

        public JsonObject build() {
            if (builder == null) throw new IllegalStateException("JSON has already been built");
            JsonObject object = new JsonObject(builder.append("}").toString());
            builder = null;
            return object;
        }

        private static String escape(String value) {
            final StringBuilder builder = new StringBuilder();
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                if (c == '"') builder.append("\\\"");
                else if (c == '\\') builder.append("\\\\");
                else if (c <= '\u000F') builder.append("\\u000").append(Integer.toHexString(c));
                else if (c <= '\u001F') builder.append("\\u00").append(Integer.toHexString(c));
                else builder.append(c);
            }
            return builder.toString();
        }

        public static class JsonObject {
            private final String value;
            private JsonObject(String value) {
                this.value = value;
            }
            @Override
            public String toString() {
                return value;
            }
        }
    }
}
