import grails.util.Environment

String tmVersion = '19.1'

def forkSettingsRun   = [minMemory: 1536, maxMemory: 4096, maxPerm: 384, debug: false]
def forkSettingsWar   = [minMemory: 2048, maxMemory: 8192, maxPerm: 2560, debug: false, forkReserve: false]
def forkSettingsOther = [minMemory:  256, maxMemory: 1024, maxPerm: 384, debug: false]

// grails.assets configuration

// minify javascript files in war (true should be the default)
//grails {
//    assets {
//	minifyJs = true
//
////    excludes = ["tiny_mce/src/*.js"], // excluded from processing if included by the require tree.
//	minifyOptions = [
//	    languageMode: 'ES5', // tried ECMASCRIPT6
//	    targetLanguage: 'ES5', //Can go from ES5 to ES6 for those bleeding edgers
//	    optimizationLevel: 'WHITESPACE_ONLY', //SIMPLE or ADVANCED or WHITESPACE_ONLY
//	    //	excludes: ['**/ext-all.js','**/extjs-all.js','** /ext-all.min.js','**/fractalis.js','**/fractalis.unminified.js']
//	    excludes: ['**/fractalis.js','**/fractalis.unminified.js']
//	]
//    }
//}

//It is also possible to exclude files from minification
//grails.assets.minifyOptions.excludes = ["**/*.min.js"]

//Optionally, assets can be excluded from processing if included by your require tree.
//This can dramatically reduce compile time for your assets.
//To do so, simply leverage the excludes configuration option:
//grails.assets.excludes = ["tiny_mce/src/*.js"]

//Another piece of information to know is that files that are prefixed with _ are not compiled individually by the asset-pipeline.
//These files are considered partials and should be required into another manifest file for compilation.
//If, in the event, you need to add these files back to the precompile phase you can define a global includes property like so.
//grails.assets.includes = ["**/_*.*"]

def dm
try {
    Class dmClass = new GroovyClassLoader().parseClass(
	new File('../transmart-dev/DependencyManagement.groovy'))
    dm = dmClass?.newInstance()
}
catch (ignored) {}

grails.project.dependency.resolver = 'maven'
grails.project.fork = [test: forkSettingsOther, run: forkSettingsRun, war: forkSettingsWar, console: forkSettingsOther]

grails.project.source.level = 1.8
grails.project.target.level = 1.8

grails.project.war.file = 'target/' + appName + '.war'
grails.project.work.dir = 'target'
grails.servlet.version = '3.0'

// copying files into known locations in the deployed war file

grails.war.resources = { stagingDir -> 
    copy(todir: "${stagingDir}/WEB-INF/dataExportRscripts") {
	fileset(dir: 'src/main/resources/dataExportRScripts')
    }
    copy(todir: "${stagingDir}/WEB-INF/classes/public") {
	fileset(dir: 'src/main/resources/public')
    }
    copy(todir: "${stagingDir}/WEB-INF/HeimScripts") {
	fileset(dir: '../SmartR/src/main/resources/HeimScripts')
    }

    // to copy a single file
    //copy(file: 'relative/path/to/file.ext', todir: "${stagingDir}/WEB-INF/destination")
    // to copy a directory
    //copy(todir: "${stagingDir}/WEB-INF/destination") {
    //     fileset(dir: 'relative/path')
    // }
    // to delete unwanted files
    // delete(verbose: true) { fileset(dir: stagingDir) { include name: '**/Thumbs.db' } }
}

grails.project.dependency.resolution = {
    inherits 'global'
    log 'warn'
    checksums true
    legacyResolve false

    if (!dm) {
	repositories {
	    grailsCentral()
	    mavenLocal()
	    mavenCentral()

	    mavenRepo 'https://repo.transmartfoundation.org/content/repositories/public/'
	}
    }
    else {
	dm.configureRepositories delegate
    }

    dependencies {
	compile 'org.codenarc:CodeNarc:0.21' // 2.2.0
	compile 'axis:axis:1.4' // for GeneGo web services
	compile 'com.google.guava:guava:30.1.1-jre'
	compile 'commons-net:commons-net:3.3' // used for ftp transfers
	compile 'net.sf.ehcache:ehcache:2.9.0'
	compile 'net.sf.opencsv:opencsv:2.3'
	compile 'org.apache.commons:commons-math:2.2' //>2MB lib briefly used in ChartController
	compile 'org.apache.httpcomponents:httpclient:4.4.1'
	compile 'org.apache.httpcomponents:httpcore:4.4.1'
	compile 'org.apache.lucene:lucene-core:2.4.0'
	compile 'org.apache.lucene:lucene-demos:2.4.0'
	compile 'org.apache.lucene:lucene-highlighter:2.4.0'
	compile 'org.apache.solr:solr-core:5.4.1'
	compile 'org.apache.solr:solr-solrj:5.4.1'
	compile 'org.jfree:jfreechart:1.5.0'
	compile 'org.jfree:jfreesvg:2.1'
	compile 'org.codehaus.groovy.modules.http-builder:http-builder:0.5.2', {
	    excludes 'groovy', 'nekohtml', 'httpclient', 'httpcore'
	}
        compile 'org.rosuda.REngine:Rserve:1.8.1'
	compile('org.springframework.security.extensions:spring-security-saml2-core:1.0.0.RELEASE') {
	    //excludes of spring security necessary because they are for an older version (3.1 branch)
	    //also remove xercesImpl because it breaks tomcat and is not otherwise needed
	    // exclude 'bcprov-jdk15' to avoid a nasty cyclic dependency that prevents tomcat starting
	    excludes 'bcprov-jdk15', 'spring-security-config', 'spring-security-core', 'spring-security-web', 'xercesImpl'
	}
////	compile 'org.transmartproject:transmart-core-api:' + tmVersion

	runtime 'com.jcraft:jsch:0.1.42'
	runtime 'com.lowagie:itext:2.0.8', { transitive = false }
	runtime 'gov.nist.math:jama:1.0.1'
	runtime 'oauth.signpost:signpost-commonshttp4:1.2.1.1'
	runtime 'oauth.signpost:signpost-core:1.2.1.1'
	// no version beyond 3.7-beta3; apparently not used
	// runtime 'org.apache.poi:poi-contrib:3.6'
	runtime 'org.apache.poi:poi-scratchpad:5.0.0'
	runtime 'org.apache.poi:poi-ooxml:5.0.0'
	runtime 'org.apache.poi:poi:5.0.0'
	runtime 'xerces:xercesImpl:2.9.1'

	// you can remove whichever you are not using
	runtime 'org.postgresql:postgresql:42.3.3'           // Java 8+ 15-feb-22
	runtime 'com.oracle.database.jdbc:ojdbc8:21.5.0.0'   // Java 8+ 02-feb-22

	test 'org.gmock:gmock:0.9.0-r435-hyve2', { transitive = false }
	test 'org.grails:grails-datastore-test-support:1.0.2-grails-2.4'
	test 'org.hamcrest:hamcrest-core:1.3'
	test 'org.hamcrest:hamcrest-library:1.3'

	// unused dependencies from 16.3
	//        runtime 'org.javassist:javassist:3.16.1-GA'

	//        compile 'antlr:antlr:2.7.7'

	//        // spring security version should be in sync with that brought with
	//        // grails-spring-security-core
	runtime 'org.springframework.security:spring-security-config:3.2.9.RELEASE',
	    'org.springframework.security:spring-security-web:3.2.9.RELEASE', {
	    transitive = false
	}

	//        test 'junit:junit:4.11', {
	//            transitive = false /* don't bring hamcrest */
	//            export = false
	//        }

    }

    plugins {
	compile ':asset-pipeline:2.14.1.1'
	compile ':cache-ehcache:1.0.5'
	compile ':hibernate:3.6.10.19'
	compile ':quartz:1.0.2'
	compile ':rest-client-builder:2.1.1'
	compile ':scaffolding:2.1.2'
	compile ':spring-security-core:2.0.0'
	compile ':spring-security-kerberos:1.0.0'
	compile ':spring-security-ldap:2.0.0'
	compile ':spring-security-oauth2-provider:2.0-RC5'

	build ':release:3.1.2'
	build ':tomcat:8.0.50'

	runtime ':jquery:1.11.1'    // Provides integration for the jQuery library with Grails JavascriptProvider
	runtime ':jquery-ui:1.10.4' // Simply supplies jQuery UI resources, depends on jQuery plugin

//	runtime ':prototype:1.0'    // requires  resources:1.0
//	runtime ':resources:1.2.14'

	//test ':code-coverage:1.2.6' // Doesn't work with forked tests yet

	if (!dm) {
	    compile ':smart-r:'                   + tmVersion
////	    compile ':rdc-rmodules:'              + tmVersion
////	    compile ':transmart-core:'            + tmVersion
	    compile ':transmart-gwas:'            + tmVersion
	    compile ':transmart-gwas-plink:'      + tmVersion
////	    compile ':dalliance-plugin:'          + tmVersion
////	    compile ':transmart-mydas:'           + tmVersion
	    compile ':transmart-rest-api:'        + tmVersion
////	    compile ':transmart-shared:'          + tmVersion
	    compile ':spring-security-auth0:'     + tmVersion
	    compile ':galaxy-export-plugin:'      + tmVersion
////	    compile ':transmart-metacore-plugin:' + tmVersion
	    compile ':transmart-xnat-importer:'   + tmVersion
	    compile ':xnat-viewer:'               + tmVersion

////	    compile ':transmart-custom:'          + tmVersion
	    compile ':transmart-fractalis:'       + tmVersion
	    test ':transmart-core-db-tests:'      + tmVersion

	    // new transmart modules to be included
	    //			compile ':transmart-gnome:'           + tmVersion

	    // dependencies already included
	    //			compile ':biomart-domain:'            + tmVersion
	    //			compile ':folder-management:'         + tmVersion
	    //			compile ':search-domain:'             + tmVersion
	    //			compile ':transmart-java:'            + tmVersion
	    //			compile ':transmart-legacy-db:'       + tmVersion
	}
	else {
	    dm.internalDependencies delegate
	}
    }
}

dm?.with {
    configureInternalPlugin 'runtime', 'smart-r'
    configureInternalPlugin 'compile', 'rdc-rmodules'
    configureInternalPlugin 'runtime', 'transmart-core'
    configureInternalPlugin 'compile', 'transmart-gwas'
    configureInternalPlugin 'runtime', 'transmart-gwas-plink'
//    configureInternalPlugin 'runtime', 'dalliance-plugin'
//    configureInternalPlugin 'runtime', 'transmart-mydas'
    configureInternalPlugin 'runtime', 'transmart-rest-api'
    configureInternalPlugin 'runtime', 'transmart-shared'
    configureInternalPlugin 'runtime', 'galaxy-export-plugin'
    configureInternalPlugin 'runtime', 'transmart-metacore-plugin'
    configureInternalPlugin 'runtime', 'transmart-xnat-importer'
    configureInternalPlugin 'runtime', 'xnat-viewer'
    configureInternalPlugin 'test',    'transmart-core-db-tests'
}

dm?.inlineInternalDependencies grails, grailsSettings

// Use new NIO connector in order to support sendfile
// This is a lovely thought, but with Tomcat running Grails 2.3.6+ NIO does not function in run-war mode
// Official bug number : GRAILS-11376
if (!grails.util.Environment.isWarDeployed()) {
    grails.tomcat.nio = true
}

codenarc.reports = {
    TransmartAppReport('html') {
        outputFile = 'CodeNarc-transmartApp-Report.html'
        title = 'transmartApp Report'
    }
}
