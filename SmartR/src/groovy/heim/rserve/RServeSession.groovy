package heim.rserve

import groovy.util.logging.Slf4j
import heim.session.SmartRSessionScope
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.rosuda.REngine.Rserve.RConnection
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

@SmartRSessionScope
@Component
@Slf4j('logger')
class RServeSession implements DisposableBean {

    @Value('#{sessionId}')
    String sessionId

    @Autowired
    RConnectionProvider rConnectionProvider

    private volatile Thread rConnectionHoldingThread
    private volatile boolean shuttingDown
    private final Lock rConnectionLock = new ReentrantLock()

    @Lazy
    private RConnection rConnection = {
        rConnectionProvider.get()
    }()

    /*
     * TODO: the implementation will block threads unnecessarily.
     * Explore implementation with GPars' Agents or Actors?
     */
    public <R> R doWithRConnection(Closure<R> callable) {
        if (shuttingDown) {
            logger.warn('Rserve session already shutting down; ' +
                    "won't execute code")
            return null
        }

        rConnectionLock.lockInterruptibly()
        rConnectionHoldingThread = Thread.currentThread()
        logger.debug 'Thread ' + Thread.currentThread().name + ' ' +
                'got access to R connection in session ' + sessionId
        try {
            return callable.call(rConnection)
        }
        finally {
            rConnectionLock.unlock()
            rConnectionHoldingThread = null
            logger.debug 'Thread ' + Thread.currentThread().name + ' has released ' +
                    'access to R connection in session ' + sessionId
        }
    }

    @Override
    void destroy() throws Exception {
        shuttingDown = true
        if (rConnection.isConnected()) {
            logger.debug('Asked to disconnect R connection ' + rConnection)
            if (rConnectionHoldingThread) {
                logger.info('Trying to interrupt thread holding the R connection')
                rConnectionHoldingThread.interrupt()
            }

            rConnectionLock.lock()
            try {
                boolean result = rConnection.close()
                if (!result) {
                    logger.warn(
                            'Close() returned false on R connection ' + rConnection)
                }
                else {
                    logger.debug('Closed R connection ' + rConnection)
                }
            }
            finally {
                rConnection.unlock()
            }
        }
    }
}
