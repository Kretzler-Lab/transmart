package jobs.table.columns.binning

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import jobs.table.Column
import jobs.table.columns.ColumnDecorator
import org.mapdb.Fun

@CompileStatic
class EvenSpacedBinningColumnDecorator implements ColumnDecorator {

    @Delegate
    Column inner

    int numberOfBins

    private Map<String, Number> min = [:].withDefault { Double.POSITIVE_INFINITY }
    private Map<String, Number> max = [:].withDefault { Double.NEGATIVE_INFINITY }

    private Map<String, List> binNames
    private Map<String, BigDecimal> inverseBinInterval

    EvenSpacedBinningColumnDecorator() {
	Map<String, List> map = [:]
	binNames = map.withDefault { String ctx ->
	    map[ctx] = (1..numberOfBins).collect { int it ->
                def lowerBound = min[ctx] + ((max[ctx] - min[ctx]) / numberOfBins) * (it - 1)
                def upperBound = min[ctx] + ((max[ctx] - min[ctx]) / numberOfBins) * it
                def op2 = it == numberOfBins ? '≤' : '<'
                "$lowerBound ≤ $header $op2 $upperBound" as String
            }
        }

	Map<String, BigDecimal> map2 = [:]
	inverseBinInterval = map2.withDefault { String ctx ->
		map2[ctx] = numberOfBins / (max[ctx] - min[ctx])
	}
    }

    private void considerValue(String ctx, Number value) {
        if (value < min[ctx]) {
            min[ctx] = value
        }
        if (value > max[ctx]) {
            max[ctx] = value
        }
    }

    private void considerValue(String ctx, Object value) {
        considerValue(ctx, value as BigDecimal)
    }

    @CompileDynamic
    // multi-dispatch
    private void considerValue(String ctx, Map<String, Object> value) {
        /* otherwise found map inside map inside consumeResultingTableRows()'s map? */
        assert ctx == ''
        for (entry in value) {
            considerValue entry.key, entry.value
        }
    }

    @CompileDynamic
    // multi-dispatch
    Map<String, Object> consumeResultingTableRows() {
        Map<String, Object> innerResult = inner.consumeResultingTableRows()

        for (entry in innerResult) {
            assert entry.value != null /* otherwise violates contract of consumeRTR() */
            considerValue '', entry.value
        }

        innerResult
    }

    private String transform(String ctx, Number value) {
        /* normalize to interval [0, numberOfBins] */
        def norm = (value - min[ctx]) * inverseBinInterval[ctx]
        def bin = (norm as int)
        assert bin >= 0
        if (bin == numberOfBins) { //happens for max
            bin--
        }

        binNames[ctx][bin]
    }

    // NOTE: assumes there's no transformer in inner
    Closure<Object> getValueTransformer() {
        { Fun.Tuple3<String, Integer, String> key, Object value ->
            (Object) transform(key.c,
                    (Number) ((value instanceof Number) ? value : (value as BigDecimal)))
        }
    }

    void beforeDataSourceIteration(String dataSourceName, Iterable dataSource) {
        // just for validation
        if (!header) {
            throw new IllegalStateException('Bug: header not set here')
        }

        inner.beforeDataSourceIteration dataSourceName, dataSource
    }
}
