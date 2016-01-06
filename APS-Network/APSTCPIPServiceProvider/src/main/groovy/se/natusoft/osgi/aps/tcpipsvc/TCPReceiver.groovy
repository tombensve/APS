/*
 *
 * PROJECT
 *     Name
 *         APS TCPIP Service Provider
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides an implementation of APSTCPIPService. This service does not provide any security of its own,
 *         but makes use of APSTCPSecurityService, and APSUDPSecurityService when available and configured for
 *         security.
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
import se.natusoft.osgi.aps.api.net.tcpip.StreamedRequestListener
import se.natusoft.osgi.aps.tcpipsvc.config.TCPIPConfig
import se.natusoft.osgi.aps.tcpipsvc.security.TCPSecurityHandler
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.exceptions.APSNoServiceAvailableException
import se.natusoft.osgi.aps.tools.util.IntensiveExceptionsGuard

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

    /** The listen connection point. */
    URI connectionPoint

    /** A logger to log to. */
    APSLogger logger

    /** The security handler */
    TCPSecurityHandler securityHandler

    //
    // Private Members
    //

    /** There can be only one!. */
    private StreamedRequestListener listener

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
     * Returns the direction of the connection.
     */
    @Override
    public ConnectionProvider.Direction getDirection() {
        return ConnectionProvider.Direction.Read
    }

    /**
     * Adds a TCP listener.
     *
     * @param listener The listener to set.
     */
    public synchronized void setListener(StreamedRequestListener listener) throws IOException {
        if (this.listener != null) throw new IllegalArgumentException("This receiver (${this.connectionPoint}) already has a listener!")
        this.listener = listener
        if (active) {
            startReceiverThread()
        }
    }

    /**
     * Removes a TCP listener.
     */
    public synchronized void removeListener() {
        if (this.active) {
            try {
                stopReceiverThread()
            }
            catch (Exception e) {
                this.logger.error("Failed to stop receiver thread for (${this.connectionPoint}) on removeListener()!", e)
            }
        }
        this.listener = null
    }

    /**
     * Internal util method to start receiver thread. This is not started until there are listeners.
     */
    private void startReceiverThread() throws IOException {
        if (this.connectionPoint.fragment?.contains("secure")) {
            if (!this.securityHandler.hasSecurityService()) {
                throw new IOException("Security has been requested, but APSTCPSecurityService is not available!")
            }
            this.socket = this.securityHandler.createServerSocket(this.connectionPoint)
        }
        else {
            this.socket = new ServerSocket(this.connectionPoint.port)
        }
        // We have to set a timeout here so that socket.accept() does not wait forever. See comment in TCPReceiverThread below also.
        // Also note that the timeout should always be less than the join(n) value in stopReceiverThread.
        this.socket.soTimeout = SOCKET_TIMEOUT
        this.receiverThread = new TCPReceiverThread(
                connectionPoint:  this.connectionPoint,
                socket: this.socket,
                listener: this.listener,
                logger: this.logger
        )
        this.receiverThread.start()
        this.logger.info("TCP Receiver for port ${this.connectionPoint.port} was started!")
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
            this.logger.info("TCP Receiver for port ${this.connectionPoint.port} was stopped!")
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

    /** The connection point for the receiver socket. */
    URI connectionPoint

    /** The ServerSocket to listen to. */
    ServerSocket socket

    /** A listener to call on received request. */
    StreamedRequestListener listener

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
        setName("APSTCPIPService: TCP ReceiverThread[" + this.connectionPoint + "]")

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

        IntensiveExceptionsGuard<IOException> exceptionGuard = new IntensiveExceptionsGuard<>(logger, intensity, maxExceptions)
        IntensiveExceptionsGuard<Exception> exceptionGuardCallback = new IntensiveExceptionsGuard<>(logger, intensity, maxExceptions)

        this.callbackPool = Executors.newFixedThreadPool(TCPIPConfig.managed.get().expert.tcpCallbackThreadPoolSize.int)

        startRunning()

        safeLogger.info("TCPReceiverThread(" + this.connectionPoint + "): Starting up!")

        try {
            while (keepRunning()) {
                try {
                    Socket clientSocket = socket.accept()

                    this.callbackPool.execute(
                            new SocketCallbackTask(
                                    connectionPoint: this.connectionPoint,
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
                catch (SocketTimeoutException ignore) {
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

        safeLogger.info("TCPReceiverThread(" + this.connectionPoint + "): Shutting down!")
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

    /** The connection point for the socket triggering the callback. */
    URI connectionPoint

    /** The socket to read request data from and write response to. */
    Socket clientSocket

    /** Protects against runaway exceptions. */
    IntensiveExceptionsGuard<Exception> exceptionGuard

    /** The listener to call. */
    StreamedRequestListener listener

    //
    // Methods
    //

    public void run() {
        try {
            InputStream requestStream = new TCPInputStreamWrapper(wrapee: clientSocket.inputStream, logger: this.logger)
            OutputStream responseStream = new TCPOutputStreamWrapper(wrapee: clientSocket.outputStream, logger: this.logger)

            if (this.connectionPoint.fragment?.contains("async")) {
                responseStream.allowWrite = false
            }

            listener.requestReceived(this.connectionPoint, requestStream, responseStream)
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
