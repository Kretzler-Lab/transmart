<g:select style="width: 400px" id="vendor" name="vendor" noSelection="${['null':'Select...']}" from="${vendors}" value="${vendor}"
			onchange="${remoteFunction(action:'ajaxTechnologies', update: 'technologywrapper', onSuccess: 'updatePlatforms()', params:'\'vendorName=\' + this.value + \'&technologyName=\' + jQuery(\'#technology\').val() + \'&measurementName=\' + jQuery(\'#measurement\').val()' )};
					${remoteFunction(action:'ajaxMeasurements', update: 'measurementwrapper', onSuccess: 'updatePlatforms()', params:'\'vendorName=\' + this.value + \'&technologyName=\' + jQuery(\'#technology\').val() + \'&measurementName=\' + jQuery(\'#measurement\').val()' )}"/>

