/*
 * Copyright © 2013-2014 The Hyve B.V.
 *
 * This file is part of transmart-core-db.
 *
 * Transmart-core-db is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * transmart-core-db.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transmartproject.db.dataquery.highdim.rnaseq

import grails.orm.HibernateCriteriaBuilder
import org.hibernate.ScrollableResults
import org.hibernate.engine.SessionImplementor
import org.hibernate.sql.JoinFragment
import org.hibernate.transform.Transformers
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.core.exceptions.InvalidArgumentsException
import org.transmartproject.core.exceptions.UnexpectedResultException
import org.transmartproject.core.querytool.HighDimensionFilterType
import org.transmartproject.db.dataquery.highdim.AbstractHighDimensionDataTypeModule
import org.transmartproject.db.dataquery.highdim.DeSubjectSampleMapping
import org.transmartproject.db.dataquery.highdim.DefaultHighDimensionTabularResult
import org.transmartproject.db.dataquery.highdim.PlatformImpl
import org.transmartproject.db.dataquery.highdim.acgh.AcghDataTypeResource
import org.transmartproject.db.dataquery.highdim.chromoregion.ChromosomeSegmentConstraintFactory
import org.transmartproject.db.dataquery.highdim.chromoregion.DeChromosomalRegion
import org.transmartproject.db.dataquery.highdim.chromoregion.RegionRowImpl
import org.transmartproject.db.dataquery.highdim.correlations.CorrelationTypesRegistry
import org.transmartproject.db.dataquery.highdim.correlations.SearchKeywordDataConstraintFactory
import org.transmartproject.db.dataquery.highdim.parameterproducers.AllDataProjectionFactory
import org.transmartproject.db.dataquery.highdim.parameterproducers.DataRetrievalParameterFactory
import org.transmartproject.db.dataquery.highdim.parameterproducers.MapBasedParameterFactory
import org.transmartproject.db.dataquery.highdim.parameterproducers.SimpleAnnotationConstraintFactory
import org.transmartproject.db.dataquery.highdim.parameterproducers.SimpleRealProjectionsFactory

import static org.transmartproject.db.util.GormWorkarounds.createCriteriaBuilder

/**
 * Module for RNA-seq, as implemented for Postgres by TraIT.
 */
class RnaSeqModule extends AbstractHighDimensionDataTypeModule {

    static final String RNASEQ_VALUES_PROJECTION = 'rnaseq_values'

    final List<String> platformMarkerTypes = ['RNASEQ_RCNT']
    final String name = 'rnaseq'
    final String description = "Messenger RNA data (Sequencing)"
    final Map<String, Class> dataProperties = typesMap(DeSubjectRnaseqData,
						       ['readcount', 'normalizedReadcount', 'logNormalizedReadcount', 'zscore'])
    final Map<String, Class> rowProperties = typesMap(RegionRowImpl,
						      ['id', 'name', 'cytoband', 'chromosome', 'start', 'end', 'numberOfProbes', 'bioMarker'])

    @Autowired
    DataRetrievalParameterFactory standardAssayConstraintFactory
    @Autowired
    DataRetrievalParameterFactory standardDataConstraintFactory
    @Autowired
    ChromosomeSegmentConstraintFactory chromosomeSegmentConstraintFactory
    @Autowired
    CorrelationTypesRegistry correlationTypesRegistry

    HighDimensionDataTypeResource createHighDimensionResource(Map params) {
        /* return instead subclass of HighDimensionDataTypeResourceImpl,
         * because we add a method, retrieveChromosomalSegments() */
        new AcghDataTypeResource(this)
    }

    protected List<DataRetrievalParameterFactory> createAssayConstraintFactories() {
        [ standardAssayConstraintFactory ]
    }

    protected List<DataRetrievalParameterFactory> createDataConstraintFactories() {
	[standardDataConstraintFactory,
         chromosomeSegmentConstraintFactory,
         new SimpleAnnotationConstraintFactory(field: 'region', annotationClass: DeChromosomalRegion.class),
	 new SearchKeywordDataConstraintFactory(correlationTypesRegistry, 'GENE', 'region', 'geneId')]
    }

    protected List<DataRetrievalParameterFactory> createProjectionFactories() {
	[new MapBasedParameterFactory(
            (RNASEQ_VALUES_PROJECTION): { Map<String, Object> params ->
		if (params) {
                    throw new InvalidArgumentsException('Expected no parameters here')
                }
                new RnaSeqValuesProjection()
	    }),
         new SimpleRealProjectionsFactory(
                (Projection.LOG_INTENSITY_PROJECTION): 'logNormalizedReadcount',
                (Projection.LOG_NORMALIZED_READ_COUNT_PROJECTION): 'logNormalizedReadcount',  // alias that is more descriptive
                (Projection.DEFAULT_REAL_PROJECTION):  'normalizedReadcount',
                (Projection.NORMALIZED_READ_COUNT_PROJECTION): 'normalizedReadcount',                    // alias that is more descriptive
		(Projection.ZSCORE_PROJECTION): 'zscore'),
	 new AllDataProjectionFactory(dataProperties, rowProperties)]
    }

    HibernateCriteriaBuilder prepareDataQuery(Projection projection, SessionImplementor session) {
	HibernateCriteriaBuilder criteriaBuilder = createCriteriaBuilder(
	    DeSubjectRnaseqData, 'rnaseqdata', session)

        criteriaBuilder.with {
	    createAlias 'jRegion', 'region', JoinFragment.INNER_JOIN
	    createAlias 'jRegion.platform', 'platform', JoinFragment.INNER_JOIN

            projections {
                property 'rnaseqdata.assay.id',               'assayId'
                property 'rnaseqdata.readcount',              'readcount'
                property 'rnaseqdata.normalizedReadcount',    'normalizedReadcount'
                property 'rnaseqdata.logNormalizedReadcount', 'logNormalizedReadcount'
                property 'rnaseqdata.zscore',                 'zscore'

                property 'region.id',                         'id'
                property 'region.name',                       'name'
                property 'region.cytoband',                   'cytoband'
                property 'region.chromosome',                 'chromosome'
                property 'region.start',                      'start'
                property 'region.end',                        'end'
                property 'region.numberOfProbes',             'numberOfProbes'
                property 'region.geneSymbol',                 'geneSymbol'

                property 'platform.id', 'platformId'
                property 'platform.title', 'platformTitle'
                property 'platform.organism', 'platformOrganism'
                property 'platform.annotationDate', 'platformAnnotationDate'
                property 'platform.markerType', 'platformMarkerType'
                property 'platform.genomeReleaseId', 'platformGenomeReleaseId'
            }

            order 'region.id', 'asc'
            order 'assay.id',  'asc' // important

            // because we're using this transformer, every column has to have an alias
            instance.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
        }

        criteriaBuilder
    }

    TabularResult transformResults(ScrollableResults results, List<AssayColumn> assays, Projection projection) {
        /* assumption here is the assays in the passed in list are in the same
         * order as the assays in the result set */
	Map assayIndexMap = createAssayIndexMap(assays)

        new DefaultHighDimensionTabularResult(
            rowsDimensionLabel:    'Regions',
            columnsDimensionLabel: 'Sample codes',
            indicesList:           assays,
            results:               results,
            allowMissingAssays:    true,
            assayIdFromRow:        { it[0].assayId },
            inSameGroup:           { a, b -> a.id == b.id }, // same region id //
            finalizeGroup:         { List list ->
                if (list.size() != assays.size()) {
                    throw new UnexpectedResultException(
			"Expected group to be of size ${assays.size()}; got ${list.size()} objects")
                }
                def firstNonNullCell = list.find()[0]
                new RegionRowImpl(
                    id:             firstNonNullCell.id,
                    name:           firstNonNullCell.name,
                    cytoband:       firstNonNullCell.cytoband,
                    chromosome:     firstNonNullCell.chromosome,
                    start:          firstNonNullCell.start,
                    end:            firstNonNullCell.end,
                    numberOfProbes: firstNonNullCell.numberOfProbes,
                    bioMarker:      firstNonNullCell.geneSymbol,
                    platform: new PlatformImpl(
                        id:              firstNonNullCell.platformId,
                        title:           firstNonNullCell.platformTitle,
                        organism:        firstNonNullCell.platformOrganism,
                        //It converts timestamp to date
                        annotationDate:  firstNonNullCell.platformAnnotationDate ?
                            new Date(firstNonNullCell.platformAnnotationDate.getTime())
                        : null,
                        markerType:      firstNonNullCell.platformMarkerType,
                        genomeReleaseId: firstNonNullCell.platformGenomeReleaseId
                    ),
                    assayIndexMap:  assayIndexMap,
                    data:           list.collect { projection.doWithResult it?.getAt(0) }
                )
            }
        )
    }

    List<String> searchAnnotation(String concept_code, String search_term, String search_property) {
	if (!getSearchableAnnotationProperties().contains(search_property)) {
            return []
	}

        DeChromosomalRegion.createCriteria().list {
            eq('gplId', DeSubjectSampleMapping.createCriteria().get {
                eq('conceptCode', concept_code)
                projections {distinct 'platform.id'}
            })
            ilike(search_property, search_term + '%')
            projections { distinct(search_property) }
            order(search_property, 'ASC')
            maxResults(100)
        }
    }

    List<String> getSearchableAnnotationProperties() {
        ['geneSymbol', 'cytoband', 'name']
    }

    HighDimensionFilterType getHighDimensionFilterType() {
        HighDimensionFilterType.SINGLE_NUMERIC
    }

    List<String> getSearchableProjections() {
        [Projection.LOG_NORMALIZED_READ_COUNT_PROJECTION, Projection.NORMALIZED_READ_COUNT_PROJECTION, Projection.ZSCORE_PROJECTION]
    }
}
