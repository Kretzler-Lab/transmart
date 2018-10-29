package jobs.misc

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.config.Scope
import org.springframework.stereotype.Component

@Slf4j('logger')
@Component
class JobSpringScope implements Scope {

    @Override
    Object get(String name, ObjectFactory<?> objectFactory) {
        def object = beansStorage[name]
        if (object == null) {
            object = objectFactory.getObject()
            beansStorage[name] = object
        }
        object
    }

    @Override
    Object remove(String name) {
        beansStorage.remove name
    }

    @Override
    void registerDestructionCallback(String name, Runnable callback) {
        logger.warn('Destruction callbacks are not supported')
    }

    @Override
    Object resolveContextualObject(String key) {
        // apparently just used for evaluating bean expressions
        // does not matter for our purposes
        null
    }

    @Override
    String getConversationId() {
        AnalysisQuartzJobAdapter.CURRENT_JOB_NAME
    }

    private Map<String, Object> getBeansStorage() {
        AnalysisQuartzJobAdapter.BEANS_STORAGE
    }
}
