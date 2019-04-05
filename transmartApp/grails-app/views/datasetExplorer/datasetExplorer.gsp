<!DOCTYPE HTML>
<html>
    <head>
	<!-- Force Internet Explorer 8 to override compatibility mode -->
	<meta http-equiv="X-UA-Compatible" content="IE=Edge"/>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>

	<title>Dataset Explorer</title>

	<asset:link href="transmart.ico" rel="shortcut icon" type="image/x-ico" />
	<asset:link href="transmart.ico" rel="icon" type="image/x-ico" />

	<!-- Include jQuery, Ext and app-specific scripts: -->
	<asset:stylesheet href="folderManagement.css"/>

	<asset:stylesheet href="analyseTab.css"/>

	<%-- We do not have a central template, so this only works in the database explorer for now --%>
	<g:if test="${['true', true]*.equals(grailsApplication.config.com.recomdata.debug.jsCallbacks).any()}">
            <asset:javascript src="long-stack-traces.js"/>
	</g:if>

	<asset:javascript src="folderManagementDE.js"/>
	<asset:javascript src="jquery-plugin.js"/>
	<asset:javascript src="extjs.min.js"/>
	<asset:javascript src="session_time.js"/>
	<asset:javascript src="analyseTab.js"/>

	<tmpl:/RWG/urls/>

	<script type="text/javascript">
        var pageInfo = {
            basePath: "${request.contextPath}"
        }

        GLOBAL = {
            Version: '1.0',
            Domain: '${i2b2Domain}',
            ProjectID: '${i2b2ProjectID}',
            Username: '${i2b2Username}',
            Password: '${i2b2Password}',
            AutoLogin: true,
            Debug: false,
            NumOfSubsets: 2,
            NumOfQueryCriteriaGroups: 20,
            NumOfQueryCriteriaGroupsAtStart: 3,
            MaxSearchResults: 100,
            ONTUrl: '',
            usePMHost: '${usePmHost}',
            Config: 'jj',
            CurrentQueryName: '',
            CurrentComparisonName: ' ',
            CurrentSubsetIDs: [],
            CurrentSubsetQueries: ["", "", ""],
            CurrentPathway: '',
            CurrentPathwayName: '',
            CurrentGenes: '',
            CurrentChroms: '',
            CurrentDataType: '',
            GPURL: '${genePatternUrl}',
            EnableGP: '${enableGenePattern}',
            HeatmapType: 'Compare',
            IsAdmin: ${admin},
            Tokens: "${tokens}",
            InitialSecurity: ${initialaccess},
            restoreSubsetId: '${params.sId}',
            resulttype: 'applet',
            searchType: "${searchGenepathway}",
            DefaultCohortInfo: '',
            CurrentTimepoints: [],
            CurrentSamples: [],
            CurrentPlatforms: [],
            CurrentGpls: [],
            CurrentTissues: [],
            CurrentRbmpanels: [],
            DefaultPathToExpand: "${pathToExpand}",
            UniqueLeaves: "",
            preloadStudy: "${params.DataSetName}",
            Binning: false,
            ManualBinning: false,
            NumberOfBins: 4,
            HelpURL: '${adminHelpUrl}',
            hiDomePopUpHelpURL: '${hiDomePopUpHelpUrl}',
            ContactUs: '${contactUs}',
            AppTitle: '${appTitle}',
            BuildVersion: 'Build Version: <g:meta name="environment.BUILD_NUMBER"/> - <g:meta name="environment.BUILD_ID"/>',
            AnalysisRun: false,
            Analysis: 'Advanced',
            HighDimDataType: '',
            SNPType: '',
            basePath: pageInfo.basePath,
            hideAcrossTrialsPanel: ${hideAcrossTrialsPanel},
            gridViewEnabled: ${gridViewEnabled},
            dataExportEnabled: ${dataExportEnabled},
            dataExportJobsEnabled: ${dataExportJobsEnabled},
            analysisJobsEnabled: ${analysisJobsEnabled},
            workspaceEnabled: ${workspaceEnabled},
            sampleExplorerEnabled: ${sampleExplorerEnabled},
            metacoreAnalyticsEnabled: ${metacoreAnalyticsEnabled},
            metacoreUrl: '${metacoreUrl}',
            AnalysisHasBeenRun: false,
            ResultSetRegionParams: {},
            currentReportCodes: [],
            currentReportStudy: [],
            currentSubsetsStudy: '',
            isGridViewLoaded: false,
            analysisTabExtensions: ${analysisTabExtensions},
            xnatEnabled: ${xnatEnabled}
        };

        var sessionSearch = "${rwgSearchFilter}";
        var sessionOperators = "${rwgSearchOperators}";
        var sessionSearchCategory = "${rwgSearchCategory}";
        var searchPage = "datasetExplorer";
        var dseOpenedNodes = "${dseOpenedNodes}";
        var dseClosedNodes = "${dseClosedNodes}";
        var helpURL = '${adminHelpUrl}';

        Ext.BLANK_IMAGE_URL = "${assetPath(src:'s.gif')}";
        Ext.Ajax.timeout = 1800000;
        Ext.Updater.defaults.timeout = 1800000;

        var $j = window.$j = jQuery.noConflict();

	</script>
    </head>

    <body>

	<div id="header-div" class="header-div">
	    <g:render template='/layouts/commonheader' model="[app: 'datasetExplorer']"/>
	</div>

	<div id="main"></div>

	<h3 id="test">Loading ...</h3>

	<tmpl:/RWG/boxSearch hide="true"/>
	<tmpl:/RWG/filterBrowser/>

	<div id="modifierValueDiv" title="Modifier Value Selection" style="display:none;">
	    <g:render template="/layouts/modifierValueForm" />
	</div>

	<div id="sidebartoggle">&nbsp;</div>

	<div id="noAnalyzeResults" style="display: none;">No subject-level results found.<br/>
	    <g:if test="${!hideBrowse}">
		<g:link controller='RWG'>Switch to Browse view</g:link>
	    </g:if>
	</div>

	<div id="filter-div" style="display: none;"></div>

	<g:form name="exportdsform" controller="export" action="exportDataset"/>
	<g:form name="exportgridform" controller="chart" action="exportGrid"/>
	<g:if test="${'true' == enableGenePattern}">
	    <g:set var="gplogout" value="${genePatternUrl}/gp/logout"/>
	</g:if>
	<g:else>
	    <g:set var="gplogout" value=""/>
	</g:else>

	<iframe src="${gplogout}" width="1" height="1" scrolling="no" frameborder="0" id="gplogin"></iframe>
	<iframe src="${gplogout}" width="1" height="1" scrolling="no" frameborder="0" id="altgplogin"></iframe>

	<div id="saveSubsetsDialog" style="display:none;font: 11px arial,tahoma,helvetica,sans-serif;font-weight:normal;">
	    <form id="saveSubsetForm">
		<label for="txtSubsetDescription">Description :</label><br/>
		<input id='txtSubsetDescription' type='text' name='txtSubsetDescription' title="Subset Description"
		       style="margin-top: 5px; margin-bottom: 5px; width: 100%; height: 20px;"/><br/>
		<label for="chkSubsetPublic" style="padding-top: 5px">Make Subset Public :</label>
		<input id='chkSubsetPublic' type='checkbox' value='Y' title="Subset Public"/><br/><br/>
		<input class="submit" type="submit" value="Save Subsets"/>
	    </form>
	</div>

	<span id="visualizerSpan0"></span>
	<span id="visualizerSpan1"></span>

	<!-- This implements the Help functionality -->
	<asset:javascript src="help/D2H_ctxt.js"/>
    </body>
</html>
