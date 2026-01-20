package com.muhwezi.choicehotspot.socket;

import android.util.Log;

import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Socket.IO service for real-time updates.
 * Mirrors the JavaScript SocketService implementation.
 */
public class SocketService {

    private static final String TAG = "SocketService";
    private static final String BASE_URL = "https://choicehotspot.online";
    private static final int MAX_RECONNECT_ATTEMPTS = 10;
    private static final long RECONNECT_DELAY_MS = 1000;
    private static final long RECONNECT_DELAY_MAX_MS = 5000;

    private static SocketService instance;

    private Socket socket;
    private boolean isConnected = false;
    private int connectionAttempts = 0;

    private final Map<String, List<Emitter.Listener>> listeners = new HashMap<>();
    private final List<ConnectionCallback> connectionCallbacks = new ArrayList<>();
    private final List<DisconnectionCallback> disconnectionCallbacks = new ArrayList<>();
    private final List<ErrorCallback> errorCallbacks = new ArrayList<>();
    private final List<ReconnectCallback> reconnectCallbacks = new ArrayList<>();

    private SocketService() {
    }

    /**
     * Get the singleton instance.
     */
    public static synchronized SocketService getInstance() {
        if (instance == null) {
            instance = new SocketService();
        }
        return instance;
    }

    /**
     * Connect to the Socket.IO server.
     * 
     * @param authToken Optional auth token
     * @return The socket instance
     */
    public Socket connect(String authToken) {
        if (socket != null && socket.connected()) {
            Log.d(TAG, "🔌 Socket already connected");
            return socket;
        }

        try {
            IO.Options options = new IO.Options();
            options.transports = new String[] { "websocket", "polling" };
            options.reconnection = true;
            options.reconnectionAttempts = MAX_RECONNECT_ATTEMPTS;
            options.reconnectionDelay = RECONNECT_DELAY_MS;
            options.reconnectionDelayMax = RECONNECT_DELAY_MAX_MS;
            options.timeout = 20000;
            options.forceNew = true;

            // Add auth token if provided
            if (authToken != null && !authToken.isEmpty()) {
                options.auth = new HashMap<>();
                ((Map<String, String>) options.auth).put("token", authToken);
            }

            socket = IO.socket(URI.create(BASE_URL), options);
            setupEventListeners();
            socket.connect();

            Log.d(TAG, "🔌 Connecting to Socket.IO server...");
            return socket;

        } catch (Exception e) {
            Log.e(TAG, "🔌 Failed to initialize Socket.IO: " + e.getMessage());
            notifyError(e);
            throw new RuntimeException("Failed to connect to socket", e);
        }
    }

    /**
     * Connect with stored auth token.
     */
    public Socket connect() {
        return connect(null);
    }

    /**
     * Setup socket event listeners.
     */
    private void setupEventListeners() {
        if (socket == null)
            return;

        socket.on(Socket.EVENT_CONNECT, args -> {
            isConnected = true;
            connectionAttempts = 0;
            Log.d(TAG, "🔌 Socket.IO connected successfully");
            notifyConnected();
        });

        socket.on(Socket.EVENT_DISCONNECT, args -> {
            isConnected = false;
            String reason = args.length > 0 ? args[0].toString() : "unknown";
            Log.d(TAG, "🔌 Socket.IO disconnected: " + reason);
            notifyDisconnected(reason);

            // If server intentionally disconnected, try to reconnect
            if ("io server disconnect".equals(reason)) {
                socket.connect();
            }
        });

        socket.on(Socket.EVENT_CONNECT_ERROR, args -> {
            connectionAttempts++;
            Exception error = args.length > 0 && args[0] instanceof Exception
                    ? (Exception) args[0]
                    : new Exception("Connection error");
            Log.e(TAG, "🔌 Socket.IO connection error: " + error.getMessage());
            notifyError(error);
        });

        socket.on("unauthorized", args -> {
            Log.e(TAG, "🔌 Socket.IO unauthorized");
            // Handle unauthorized - typically redirect to login
            disconnect();
        });
    }

    /**
     * Disconnect from the socket server.
     */
    public void disconnect() {
        if (socket != null) {
            socket.disconnect();
            socket.off();
            socket = null;
            isConnected = false;
            listeners.clear();
            Log.d(TAG, "🔌 Socket.IO disconnected manually");
        }
    }

    /**
     * Listen for an event.
     * 
     * @param event    Event name
     * @param listener Event callback
     * @return Unsubscribe runnable
     */
    public Runnable on(String event, Emitter.Listener listener) {
        if (socket == null) {
            connect();
        }

        socket.on(event, listener);

        // Track listener for cleanup
        if (!listeners.containsKey(event)) {
            listeners.put(event, new ArrayList<>());
        }
        listeners.get(event).add(listener);

        // Return unsubscribe function
        return () -> off(event, listener);
    }

    /**
     * Remove event listener.
     */
    public void off(String event, Emitter.Listener listener) {
        if (socket != null) {
            if (listener != null) {
                socket.off(event, listener);
                List<Emitter.Listener> eventListeners = listeners.get(event);
                if (eventListeners != null) {
                    eventListeners.remove(listener);
                }
            } else {
                socket.off(event);
                listeners.remove(event);
            }
        }
    }

    /**
     * Emit an event.
     * 
     * @param event Event name
     * @param data  Event data
     * @return true if emitted successfully
     */
    public boolean emit(String event, Object data) {
        if (socket == null || !isConnected) {
            Log.w(TAG, "🔌 Socket not connected, cannot emit event: " + event);
            return false;
        }

        try {
            socket.emit(event, data);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "🔌 Error emitting socket event: " + e.getMessage());
            return false;
        }
    }

    /**
     * Emit an event with acknowledgment callback.
     */
    public boolean emit(String event, Object data, io.socket.client.Ack ack) {
        if (socket == null || !isConnected) {
            Log.w(TAG, "🔌 Socket not connected, cannot emit event: " + event);
            return false;
        }

        try {
            socket.emit(event, data, ack);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "🔌 Error emitting socket event: " + e.getMessage());
            return false;
        }
    }

    // ==================== Application-specific event handlers ====================

    public Runnable onLiveData(Emitter.Listener listener) {
        return on("live_data", listener);
    }

    public Runnable onUpdateVouchers(Emitter.Listener listener) {
        return on("update_vouchers", listener);
    }

    public Runnable onUpdateUsers(Emitter.Listener listener) {
        return on("update_users", listener);
    }

    public Runnable onSystemUpdate(Emitter.Listener listener) {
        return on("system_update", listener);
    }

    public Runnable onUserConnected(Emitter.Listener listener) {
        return on("user_connected", listener);
    }

    public Runnable onUserDisconnected(Emitter.Listener listener) {
        return on("user_disconnected", listener);
    }

    public Runnable onVoucherGenerated(Emitter.Listener listener) {
        return on("voucher_generated", listener);
    }

    public Runnable onVoucherUsed(Emitter.Listener listener) {
        return on("voucher_used", listener);
    }

    // ==================== Connection Status ====================

    public boolean isConnected() {
        return isConnected;
    }

    public int getConnectionAttempts() {
        return connectionAttempts;
    }

    // ==================== Connection Callbacks ====================

    public Runnable onConnect(ConnectionCallback callback) {
        connectionCallbacks.add(callback);
        return () -> connectionCallbacks.remove(callback);
    }

    public Runnable onDisconnect(DisconnectionCallback callback) {
        disconnectionCallbacks.add(callback);
        return () -> disconnectionCallbacks.remove(callback);
    }

    public Runnable onError(ErrorCallback callback) {
        errorCallbacks.add(callback);
        return () -> errorCallbacks.remove(callback);
    }

    public Runnable onReconnect(ReconnectCallback callback) {
        reconnectCallbacks.add(callback);
        return () -> reconnectCallbacks.remove(callback);
    }

    private void notifyConnected() {
        for (ConnectionCallback cb : new ArrayList<>(connectionCallbacks)) {
            cb.onConnected();
        }
    }

    private void notifyDisconnected(String reason) {
        for (DisconnectionCallback cb : new ArrayList<>(disconnectionCallbacks)) {
            cb.onDisconnected(reason);
        }
    }

    private void notifyError(Exception error) {
        for (ErrorCallback cb : new ArrayList<>(errorCallbacks)) {
            cb.onError(error);
        }
    }

    /**
     * Remove all listeners and callbacks.
     */
    public void removeAllListeners() {
        if (socket != null) {
            socket.off();
        }
        listeners.clear();
        connectionCallbacks.clear();
        disconnectionCallbacks.clear();
        errorCallbacks.clear();
        reconnectCallbacks.clear();
    }

    // ==================== Callback Interfaces ====================

    public interface ConnectionCallback {
        void onConnected();
    }

    public interface DisconnectionCallback {
        void onDisconnected(String reason);
    }

    public interface ErrorCallback {
        void onError(Exception error);
    }

    public interface ReconnectCallback {
        void onReconnected(int attemptNumber);
    }
}
