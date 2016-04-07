package se.natusoft.osgi.aps.discoveryservice

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * This wraps a java.util.concurrent.ExecutorService. This so that one instance of this can be injected into both
 * APSSimpleDiscoveryServiceProvider and DiscoveryHandler. The actual instance is setup and configured here.
 */
@CompileStatic
@TypeChecked
class DiscoveryExecutorService implements ExecutorService {

    private ExecutorService executorService = Executors.newFixedThreadPool(10)

    @Override
    void shutdown() {
        this.executorService.shutdown()
    }

    @Override
    List<Runnable> shutdownNow() {
        return this.executorService.shutdownNow()
    }

    @Override
    boolean isShutdown() {
        return this.executorService.isShutdown()
    }

    @Override
    boolean isTerminated() {
        return this.executorService.isTerminated()
    }

    @Override
    boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return this.executorService.awaitTermination(timeout, unit)
    }

    @Override
    def <T> Future<T> submit(Callable<T> task) {
        return this.executorService.submit(task)
    }

    @Override
    def <T> Future<T> submit(Runnable task, T result) {
        return this.executorService.submit(task, result)
    }

    @Override
    Future<?> submit(Runnable task) {
        return this.executorService.submit(task)
    }

    @Override
    def <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return this.executorService.invokeAll(tasks)
    }

    @Override
    def <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return this.executorService.invokeAll(tasks, timeout, unit)
    }

    @Override
    def <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return this.executorService.invokeAny(tasks)
    }

    @Override
    def <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return this.executorService.invokeAny(tasks, timeout, unit)
    }

    @Override
    void execute(Runnable command) {
        this.executorService.execute(command)
    }
}
