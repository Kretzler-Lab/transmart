class TransmartFractalisGrailsPlugin {

	private static final String TRANSMART_EXTENSIONS_REGISTRY_BEAN_NAME = 'transmartExtensionsRegistry'

	def version = '19.1'
	def grailsVersion = '2.5.4 > *'
	def title = 'Transmart Fractalis Plugin'
	def author = 'Sascha Herzinger'
	def authorEmail = 'Sascha_Herzinger@hms.harvard.edu'
	def description = '''\
Connects tranSMART with an instance of https://git-r3lab.uni.lu/Fractalis
'''
	def documentation = 'https://wiki.transmartfoundation.org/'
	def license = 'APACHE'
	def organization = [name: 'i2b2/tranSMART Foundation', url: 'https://www.transmartfoundation.org/']
	def developers = [[name: 'Peter Rice',    email: 'ricepeterm@yahoo.co.uk'],
    			  [name: 'Burt Beckwith', email: 'burt_beckwith@hms.harvard.edu']]
	def issueManagement = [system: 'JIRA', url: 'https://jira.transmartfoundation.org/browse/TRANSREL']
	def scm = [url: 'https://github.com/tranSMART-Foundation/transmart/tree/master/transmart-fractalis']

	def doWithApplicationContext = { ctx ->
		if (true.is(application.config.fractalis.active) &&
				ctx.containsBean(TRANSMART_EXTENSIONS_REGISTRY_BEAN_NAME)) {
			ctx.getBean(TRANSMART_EXTENSIONS_REGISTRY_BEAN_NAME).registerAnalysisTabExtension(
                    'transmart-fractalis', '/Fractalis/loadScripts', 'addFractalisPanel')
		}
	}
}
