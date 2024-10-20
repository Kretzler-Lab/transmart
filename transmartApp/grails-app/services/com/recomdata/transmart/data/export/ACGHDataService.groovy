package com.recomdata.transmart.data.export

import com.recomdata.transmart.data.export.util.FileWriterUtil
import groovy.transform.CompileStatic
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.HighDimensionResource
import org.transmartproject.core.dataquery.highdim.acgh.AcghValues
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.dataquery.highdim.chromoregion.RegionRow
import org.transmartproject.core.dataquery.highdim.projections.Projection

import javax.annotation.PostConstruct

class ACGHDataService {

    static transactional = false

    HighDimensionResource highDimensionResourceService
    HighDimensionDataTypeResource<RegionRow> acghResource

    @PostConstruct
    void init() {
        /* No way to automatically inject the acgh resource in Spring?
         * Would be easy in CDI by having a producer of HighDimensionDataTypeResource
         * beans creating it on the fly by looking at the injection point */
        acghResource = highDimensionResourceService.getSubResourceForType 'acgh'
    }

    void writeRegions(String study, File studyDir, String fileName, String jobName, resultInstanceId) {
	List<AssayConstraint> assayConstraints = [
            acghResource.createAssayConstraint(
                AssayConstraint.TRIAL_NAME_CONSTRAINT,
                name: study),
            acghResource.createAssayConstraint(
                AssayConstraint.PATIENT_SET_CONSTRAINT,
		result_instance_id: resultInstanceId as Long)
        ]

	Projection projection = acghResource.createProjection([:], 'acgh_values')

	TabularResult result
	FileWriterUtil writerUtil

        try {
            /* dataType == 'aCGH' => file created in a subdir w/ that name */
            writerUtil = new FileWriterUtil(studyDir, fileName, jobName, 'aCGH',
					    null, '\t' as char)
	    result = acghResource.retrieveData(assayConstraints, [], projection)
	    doWithResult result, writerUtil
        }
        finally {
            writerUtil?.finishWriting()
            result?.close()
        }
    }

    @CompileStatic
    private doWithResult(TabularResult<AssayColumn, RegionRow> regionResult, FileWriterUtil writerUtil) {

        List<AssayColumn> assays = regionResult.indicesList
        String[] header = createHeader(assays)
        writerUtil.writeLine(header as String[])

	String[] templateArray = new String[header.size() + 1]
        //+1 b/c 1st row has no header
	int i = 1 //for the first row
        for (Iterator<RegionRow> iterator = regionResult.rows; iterator.hasNext();) {
	    RegionRow row = iterator.next()

            String[] line = (String[]) templateArray.clone()

            line[0] = i++ as String
            line[1] = row.chromosome as String
            line[2] = row.start as String
            line[3] = row.end as String
            line[4] = row.numberOfProbes as String
            line[5] = row.cytoband

            int j = 6
            PER_ASSAY_COLUMNS.each { k, Closure<AcghValues> value ->
		for (AssayColumn assay in assays) {
                    line[j++] = value(row.getAt(assay)) as String
                }
            }

	    writerUtil.writeLine line
        }
    }

    private static final List<String> HEADER = ['chromosome', 'start', 'end', 'num.probes', 'cytoband'].asImmutable()

    private static final Map<String, Closure<AcghValues>> PER_ASSAY_COLUMNS = [
	chip    : { AcghValues v -> v.getChipCopyNumberValue() },
	flag    : { AcghValues v -> v.getCopyNumberState().getIntValue() },
	probloss: { AcghValues v -> v.getProbabilityOfLoss() },
	probnorm: { AcghValues v -> v.getProbabilityOfNormal() },
	probgain: { AcghValues v -> v.getProbabilityOfGain() },
	probamp : { AcghValues v -> v.getProbabilityOfAmplification() },
    ]

    private String[] createHeader(List<AssayColumn> assays) {
	List<String> header = [] + HEADER
	for (String head in PER_ASSAY_COLUMNS.keySet()) {
	    for (AssayColumn assay in assays) {
		header << head + '.' + assay.patientInTrialId
	    }
	}

	header as String[]
    }
}
