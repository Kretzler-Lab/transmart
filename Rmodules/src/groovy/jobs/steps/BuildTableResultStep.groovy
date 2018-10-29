package jobs.steps

import groovy.util.logging.Slf4j
import jobs.steps.helpers.ColumnConfigurator
import jobs.table.Table

@Slf4j('logger')
class BuildTableResultStep implements Step {

    final String statusName = 'Collecting Data'

    Table table

    List<ColumnConfigurator> configurators

    @Override
    void execute() {
        try {
            configurators.each {
                it.addColumn()
            }

            table.buildTable()
        } catch (Exception e) {
            try {
                table.close()
            } catch (Exception e2) {
                logger.error('Error closing table after exception retrieving results', e2)
            }

            throw e
        }
    }
}
