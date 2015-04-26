/*
 *
 * PROJECT
 *     Name
 *         APS TCPIP Service NonSecure Provider
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides a nonsecure implementation of APSTCPIPService.
 *
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *
 * LICENSE
 *     Apache 2.0 (Open Source)
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 * AUTHORS
 *     tommy ()
 *         Changes:
 *         2015-04-11: Created!
 *
 */
package se.natusoft.osgi.aps.tcpipsvc

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.net.tcpip.TCPListener
import se.natusoft.osgi.aps.tcpipsvc.config.TCPIPConfig
import se.natusoft.osgi.aps.tcpipsvc.security.TCPSecurityHandler
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.util.ExceptionGuard

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Handles receive of TCP request.
 */
@CompileStatic
@TypeChecked
class TCPReceiver implements ConnectionProvider {
    //
    // Constants
    //

    private static final int SOCKET_TIMEOUT = 3000 // 3 seconds.

    //
    // Properties
    //

    /** Our config */
    ConfigWrapper config

    /** A logger to log to. */
    APSLogger logger

    /** The security handler */
    TCPSecurityHandler securityHandler

    //
    // Private Members
    //

    /** There can be only one!. */
    private TCPListener listener

    /** The state of this receiver. */
    private boolean active = false

    /** The server socket to listen to. */
    private ServerSocket socket = null

    private TCPReceiverThread receiverThread = null

    //
    // Methods
    //

    /**
     * Starts the provider.
     *
     * @throws IOException
     */
    @Override
    public synchronized void start() throws IOException {
        if (!this.active && this.listener != null) {
            startReceiverThread()
        }
        this.active = true
    }

    /**
     * Stops the provider.
     *
     * @throws IOException
     */
    @Override
    public void stop() throws IOException {
        if (this.active && this.listener != null) {
            stopReceiverThread()
        }
        this.active = false
    }

    /**
     * This method is called when configuration have been updated.
     */
    @Override
    public void configChanged() throws IOException {
        if (this.active && this.listener != null) {
            stopReceiverThread()
            startReceiverThread()
        }
    }

    /**
     * Returns the type of the connection.
     */
    @Override
    public ConnectionProvider.Type getType() {
        return ConnectionProvider.Type.TCP
    }

    /**
     * Returns the direction of the connection.
     */
    @Override
    public ConnectionProvider.Direction getDirection() {
        return ConnectionProvider.Direction.Read
    }

    /**
     * Adds a TCP listener.
     *
     * @param listener The listener to add.
     */
    public synchronized void setListener(TCPListener listener) {
        boolean hasOldListener = this.listener != null
        this.listener = listener
        if (!hasOldListener && this.active) {
            startReceiverThread()
        }
    }

    /**
     * Removes a TCP listener.
     */
    public synchronized void removeListener() {
        this.listener = null
        if (this.active) {
            stopReceiverThread()
        }
    }

    /**
     * Internal util method to start receiver thread. This is not started until there are listeners.
     */
    private void startReceiverThread() {
        this.socket = this.securityHandler.createServerSocket(config.secure)
        // We have to set a timeout here so that socket.accept() does not wait forever. See comment in TCPReceiverThread below also.
        // Also note that the timeout should always be less than the join(n) value in stopReceiverThread.
        this.socket.soTimeout = SOCKET_TIMEOUT
        this.socket.bind(new InetSocketAddress(this.config.host, this.config.port))
        this.receiverThread = new TCPReceiverThread(
                config: this.config,
                socket: this.socket,
                listener: this.listener,
                logger: this.logger
        )
        this.receiverThread.start()
    }

    /**
     *  Internal util method to stop receiver thread. This is stopped when there are no more listener or the receiver is stopped.
     *
     * @throws IOException
     */
    private void stopReceiverThread() throws IOException {
        if (this.socket != null) {
            this.receiverThread.stopThread()
            try {
                this.receiverThread.join(SOCKET_TIMEOUT + 1000)
            }
            catch (InterruptedException ie) {
                this.logger.error("The TCP receiver thread did not stop in reasonable time!", ie)
            }
            this.socket.close()
            this.socket = null
        }
    }

}

//
// Support Classes
//

// These are top level classes rather than inner classes due to Groovy 2.3.* having runtime problems
// with groovy constructs in inner classes.
//
// https://issues.apache.org/jira/browse/GROOVY-7379

/**
 * This listens on connections and handles request, calling listeners.
 */
@CompileStatic
@TypeChecked
public class TCPReceiverThread extends Thread {
    //
    // Properties
    //

    /** To log to. */
    APSLogger logger

    /** The instance config. */
    ConfigWrapper config

    /** The ServerSocket to listen to. */
    ServerSocket socket

    /** A listener to call on received request. */
    TCPListener listener

    //
    // Private Members
    //

    /** The running state of the thread. */
    private boolean running = false

    /** For executing callbacks in parallel. */
    private ExecutorService callbackPool

    //
    // Methods
    //

    /**
     * Gets passed logger or creates a local logger if none have been provided.
     */
    private APSLogger getSafeLogger() {
        if (this.logger == null) {
            this.logger = new APSLogger(System.out)
        }
        return this.logger
    }

    public synchronized void stopThread() {
        this.running = false
    }

    private synchronized boolean keepRunning() {
        this.running
    }

    private synchronized startRunning() {
        this.running = true
    }

    public void run() {
        setName("APSTCPIPService: TCP ReceiverThread[" + this.config.name + "]")

        long intensity
        int maxExceptions
        try {
            intensity = TCPIPConfig.managed.get().expert.exceptionGuardReactLimit.long
            maxExceptions = TCPIPConfig.managed.get().expert.exceptionGuardMaxExceptions.int
        }
        catch (NumberFormatException nfe) {
            logger.error("Bad non numeric values for 'exceptionGuardReactLimit' or 'exceptionGuardMaxExceptions' config! " +
                    "Using default values.", nfe)
            intensity = 500
            maxExceptions = 10
        }

        ExceptionGuard<IOException> exceptionGuard = new ExceptionGuard<>(logger, intensity, maxExceptions)
        ExceptionGuard<Exception> exceptionGuardCallback = new ExceptionGuard<>(logger, intensity, maxExceptions)

        this.callbackPool = Executors.newFixedThreadPool(TCPIPConfig.managed.get().expert.tcpCallbackThreadPoolSize.int)

        startRunning()

        safeLogger.info("TCPReceiverThread(" + this.config.name + "): Starting up!")

        try {
            while (keepRunning()) {
                try {
                    Socket clientSocket = socket.accept()

                    this.callbackPool.execute(
                            new SocketCallbackTask(
                                    config: this.config,
                                    clientSocket: clientSocket,
                                    exceptionGuard: exceptionGuardCallback,
                                    listener: this.listener,
                                    logger: this.logger
                            )
                    )

                    // Note: Since this is passed of to threads, this check will probably not check the result
                    // of the last created thread. But if many of the callback threads fail in a short time,
                    // the fail flag will eventually be set. Also note that it is the same instance we pass
                    // to all threads.
                    if (exceptionGuardCallback.failed) {
                        break
                    }
                }
                catch (SocketTimeoutException ste) {
                    // We have to set a timeout on the server socket so that accept() above does timeout
                    // otherwise the thread loop could not be stopped without data being received. So
                    // when timeouts occur they are expected and thus we do nothing here.
                }
                catch (IOException ioe) {
                    logger.error("Something failed for the receiver thread!", ioe)

                    if (!exceptionGuard.checkException(ioe)) {
                        break
                    }
                }
            }
        }
        finally {
            this.callbackPool.shutdown()
        }

        safeLogger.info("TCPReceiverThread(" + this.config.name + "): Shutting down!")
    }
}

/**
 * Handles individual callbacks.
 */
@CompileStatic
@TypeChecked
public class SocketCallbackTask implements Runnable {
    //
    // Properties
    //

    /** A logger to log to. */
    APSLogger logger

    /** Instance config */
    ConfigWrapper config

    /** The socket to read request data from and write response to. */
    Socket clientSocket

    /** Protects against runaway exceptions. */
    ExceptionGuard<Exception> exceptionGuard

    /** The listener to call. */
    TCPListener listener

    //
    // Methods
    //

    public void run() {
        try {
            InputStream requestStream = new TCPInputStreamWrapper(wrapee: clientSocket.inputStream, logger: this.logger)
            OutputStream responseStream = new TCPOutputStreamWrapper(wrapee: clientSocket.outputStream, logger: this.logger)

            listener.tcpRequestReceived(config.name, clientSocket.inetAddress, requestStream, responseStream)
            this.exceptionGuard.clearCount()
        }
        catch (Exception e) {
            logger.error("Callback for listener:'" + listener + "' failed!", e)
            this.exceptionGuard.checkException(e)
        }
        finally {
            try {
                clientSocket.close()
            }
            catch (IOException ioe) {
                this.logger.error("Failed to close client socket!", ioe)
            }
        }
    }
}
