package com.recomdata.transmart.data.association

import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.springframework.core.io.Resource

@Slf4j('logger')
class DataAssociationController {
    def assetResourceLocator

    private static final List<String> scripts = [
	'FormValidator.js',
	'HighDimensionalData.js',
	'RmodulesView.js',
	'dataAssociation.js',
	'PDFGenerator.js',
	'rmodules-tsmart-overrides.js',
	'rmodules-tsmart-generic.js',
	'plugin/IC50.js'].asImmutable()
    
    //TODO: requires images: servletContext.contextPath+pluginContextPath+'/css/jquery-ui-1.10.3.custom.css']
    private static final List<String> styles = ['rmodules.css', 'jquery.qtip.min.css'].asImmutable()

    def pluginService

    /**
     * Load the initial DataAssociation page.
     */
    def defaultPage() {
	logger.info 'defaultPage called to render dataAssociation.gsp path {}', pluginContextPath
        render template: 'dataAssociation', contextPath: pluginContextPath
    }

    def variableSelection(String analysis) {
	logger.info 'variableSelection {} view ../plugin/{}', analysis, pluginService.findPluginModuleByModuleName(analysis).formPage
        render view: '../plugin/' + pluginService.findPluginModuleByModuleName(analysis).formPage
    }

    /**
     * Load required scripts for running analysis
     */
    def loadScripts() {
	List<Map> rows = []

	for (String script in scripts) {
	    Resource assetRes = assetResourceLocator.findAssetForURI(script)
	    logger.info 'loadScripts script {} at {}', script, assetRes.getPath()
	    rows << [path: servletContext.contextPath + assetRes.getPath(), type: 'script']
	}

	for (String style in styles) {	
	    Resource assetRes = assetResourceLocator.findAssetForURI(style)
	    logger.info 'loadScripts style {} at {}', style, assetRes.getPath()
	    rows << [path: servletContext.contextPath + assetRes.getPath(), type: 'css']
	}

	render([success: true, totalCount: rows.size(), files: rows] as JSON)
    }
}
