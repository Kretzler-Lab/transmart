package com.recomdata.transmart.rmodules
/*
 * Although UrlMappings.groovy is excluded [from the plugin package], you are
 * allowed to include a UrlMappings definition with a different name, such as
 * MyPluginUrlMappings.groovy.
 */

class RmodulesUrlMappings {
    static mappings = {
	def analysisFilesClosure = {
	    controller = 'analysisFiles'
	    action     = 'download'
	    constraints {
		analysisName matches: /.+-[a-zA-Z]+\d*-\d+/
	    }
	}

	"/analysisFiles/$analysisName/$path**"        analysisFilesClosure
	"/images/analysisFiles/$analysisName/$path**" analysisFilesClosure

	// see also the exclusion of images/analysisFiles in doWithSpring
    }
}
