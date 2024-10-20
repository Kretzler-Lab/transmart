/*
 * Copyright © 2013-2016 The Hyve B.V.
 *
 * This file is part of Transmart.
 *
 * Transmart is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Transmart.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transmart.audit

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.apache.log4j.Level
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.transmartproject.core.users.User

import javax.servlet.http.HttpServletRequest

/**
 * Logs to level TRACE in a specific `metric' format.
 * Configuration of output done in (out-of-tree) Config.groovy.
 */
@CompileStatic
@Log4j('logger')
class AuditLogService implements InitializingBean {

    static transactional = false

    @Autowired private GrailsApplication grailsApplication

    // NB: gson uses SimpleDateFormat internally, which is quite slow. For heavy use that might need to be optimized
    // by e.g. importing joda time, or caching and recomputing time once per second the way log4j's
    // AbsoluteTimeDateFormat does.
    @Lazy private static final Gson gson = new GsonBuilder()
            .setDateFormat('yyyy-MM-dd HH:mm:ss.SSSX')
            .serializeNulls()
            .setPrettyPrinting()
            .create()

    @Lazy volatile boolean enabled = {
        logger.effectiveLevel.toInt() <= Level.TRACE.toInt()
    }()

    private String programVersion

    void report(Map<String, Object> params = [:], String event, HttpServletRequest request) {
        if (!enabled) {
	   return
	}

        User user = (User) params.user

	Map<String, Object> msg = [program       : 'Transmart',
	                           programVersion: programVersion,
	                           user          : user.username,
	                           event         : event] as Map
	msg.putAll params

	Authentication auth = SecurityContextHolder.context.authentication
	if (auth instanceof OAuth2Authentication) {
	    msg.clientId = ((OAuth2Authentication) auth).OAuth2Request.clientId
	}

	// Override user and other fields
	msg.user = user.username
	msg.event = event
	msg.userAgent = request.getHeader 'user-agent'
	msg.timestamp = new Date()

	logger.trace msg
    }

    void afterPropertiesSet() {
    	programVersion = grailsApplication?.metadata?.'app.version' ?: ''
    }
}
