package jobs.steps.helpers

import jobs.table.Column
import jobs.table.columns.CensorColumn
import jobs.table.columns.ConstantValueColumn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.transmartproject.core.dataquery.clinical.ClinicalVariable

@Component
@Scope('prototype')
class CensorColumnConfigurator extends ColumnConfigurator {

    String keyForConceptPaths

    @Autowired
    private ClinicalDataRetriever clinicalDataRetriever

    @Override
    protected void doAddColumn(Closure<Column> decorateColumn) {

        String conceptPaths = getConceptPaths()

        if (conceptPaths) {
            Set<ClinicalVariable> variables = conceptPaths.split(/\|/).collect { String s ->
                        clinicalDataRetriever.createVariableFromConceptPath s.trim()
                    }

            variables = variables.collect { clinicalDataRetriever << it }

            clinicalDataRetriever.attachToTable table

            table.addColumn(decorateColumn(
                new CensorColumn(header: header, leafNodes: variables)),
			    [ClinicalDataRetriever.DATA_SOURCE_NAME] as Set)
        }
	else {
            // if no concepts are specified, all rows result in CENSORING_FALSE
            table.addColumn(new ConstantValueColumn(header: header, value: CensorColumn.CENSORING_FALSE),
			    Collections.emptySet())
        }
    }

    String getConceptPaths() {
        // empty conceptPaths are allowed (required=false)
        getStringParam keyForConceptPaths, false
    }
}
