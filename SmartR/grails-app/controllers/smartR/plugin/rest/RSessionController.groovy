package smartR.plugin.rest

import groovy.util.logging.Slf4j
import heim.session.SessionService
import org.transmartproject.core.exceptions.InvalidArgumentsException

@Slf4j('logger')
class RSessionController {

    static scope = 'prototype'

    static allowedMethods = [
            create: 'POST',
            delete: 'POST',
    ]

    SessionService sessionService

    /**
     * Creates a new R session
     */
    def create() {
        def json = request.JSON
        if (!json.workflow) {
            throw new InvalidArgumentsException('Workflow not given')
        }

        UUID sessionId = sessionService.createSession(
                null /* user not used now */,
                json.workflow)

        response.status = 201
        render(contentType: 'text/json') {
            [sessionId: sessionId.toString()]
        }
    }

    def touch() {
        sessionService.touchSession(sessionId)
        render status: 204 // no content
    }

    /**
     * Deletes an R session
     */
    def delete() {
        sessionService.destroySession(sessionId)

        response.status = 202 /* session shutdown runs asynchronously */
        render ''
    }

    /**
     * Delete files associated with this session
     */
    def deleteFiles() {
	logger.info 'deleteFiles sessionId {}', sessionId
        sessionService.removeAllFiles(sessionId)
        response.status = 204
	logger.info 'deleteFiles done, fixed response.status {}', response.status

        render ''
    }

    private UUID getSessionId() {
        def json = request.JSON
        if (!json.sessionId || !(json.sessionId instanceof String)) {
            throw new InvalidArgumentsException(
                    'No session id provided or not string: ' + json.sessionId)
        }
        try {
            UUID.fromString(json.sessionId)
        }
        catch (IllegalArgumentException iae) {
            throw new InvalidArgumentsException(
                    'Invalid session id (not a UUID): ' + json.sessionId)
        }
    }

}
