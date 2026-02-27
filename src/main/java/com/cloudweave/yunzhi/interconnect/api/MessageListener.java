/*
 * InterConnect-Client-Spigot
 * Copyright (C) 2024 CloudWeave-YunZhi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.cloudweave.yunzhi.interconnect.api;

import org.json.JSONObject;

import javax.annotation.Nonnull;

/**
 * Message listener interface for receiving custom events from other servers.
 * 
 * <p>Usage example:</p>
 * <pre>
 * public class MyListener implements MessageListener {
 *     @Override
 *     public void onMessageReceived(String fromServer, String eventType, JSONObject data) {
 *         if ("my_custom_event".equals(eventType)) {
 *             // Handle the event
 *         }
 *     }
 * }
 * 
 * // Register the listener
 * InterConnectAPI api = InterConnectAPI.getInstance();
 * api.registerMessageListener(new MyListener());
 * </pre>
 * 
 * @author CloudWeave-YunZhi
 * @version 1.0.0
 */
public interface MessageListener {

    /**
     * Called when a message is received from another server.
     * 
     * @param fromServer The name of the server that sent the message
     * @param fromUuid The UUID of the server that sent the message
     * @param eventType The type of event
     * @param data The event data as a JSONObject
     */
    void onMessageReceived(@Nonnull String fromServer, @Nonnull String fromUuid, 
                          @Nonnull String eventType, @Nonnull JSONObject data);

    /**
     * Get the priority of this listener.
     * Higher priority listeners are called first.
     * 
     * @return The priority (default: 0)
     */
    default int getPriority() {
        return 0;
    }
}
