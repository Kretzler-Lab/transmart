/*
 * Copyright © 2013-2016 The Hyve B.V.
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

package org.transmartproject.db.dataquery.highdim.parameterproducers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.exceptions.InvalidArgumentsException
import org.transmartproject.core.exceptions.NoSuchResourceException
import org.transmartproject.core.ontology.ConceptsResource
import org.transmartproject.core.ontology.OntologyTerm
import org.transmartproject.core.querytool.QueriesResource
import org.transmartproject.core.querytool.QueryResult
import org.transmartproject.db.dataquery.highdim.assayconstraints.AssayIdListCriteriaConstraint
import org.transmartproject.db.dataquery.highdim.assayconstraints.DefaultOntologyTermCriteriaConstraint
import org.transmartproject.db.dataquery.highdim.assayconstraints.DefaultPatientSetCriteriaConstraint
import org.transmartproject.db.dataquery.highdim.assayconstraints.DefaultTrialNameCriteriaConstraint
import org.transmartproject.db.dataquery.highdim.assayconstraints.DisjunctionAssayCriteriaConstraint
import org.transmartproject.db.dataquery.highdim.assayconstraints.NoopAssayCriteriaConstraint
import org.transmartproject.db.dataquery.highdim.assayconstraints.PatientIdListCriteriaConstraint

@Component
class StandardAssayConstraintFactory extends AbstractMethodBasedParameterFactory {

    @Autowired
    ConceptsResource conceptsResource

    @Autowired
    QueriesResource queriesResource

    private DisjunctionConstraintFactory disjunctionConstraintFactory =
        new DisjunctionConstraintFactory(DisjunctionAssayCriteriaConstraint, NoopAssayCriteriaConstraint)

    @ProducerFor(AssayConstraint.ONTOLOGY_TERM_CONSTRAINT)
    AssayConstraint createOntologyTermConstraint(Map<String, Object> params) {
        if (params.size() != 1) {
	    throw new InvalidArgumentsException("Expected exactly one parameter (concept_key), got $params")
        }

	String conceptKey = BindingUtils.getParam(params, 'concept_key')

        try {
	    OntologyTerm term = conceptsResource.getByKey(conceptKey)
	    new DefaultOntologyTermCriteriaConstraint(term: term)
        }
	catch (NoSuchResourceException e) {
	    throw new InvalidArgumentsException(e)
        }
    }

    @ProducerFor(AssayConstraint.PATIENT_SET_CONSTRAINT)
    AssayConstraint createPatientSetConstraint(Map<String, Object> params) {
        if (params.size() != 1) {
	    throw new InvalidArgumentsException("Expected exactly one parameter (result_instance_id), got $params")
        }

	Long resultInstanceId = BindingUtils.convertToLong(
	    'result_instance_id', BindingUtils.getParam(params, 'result_instance_id', Object))

        QueryResult result
        try {
	    result = queriesResource.getQueryResultFromId(resultInstanceId)
        }
	catch (NoSuchResourceException e) {
	    throw new InvalidArgumentsException(e)
        }

        new DefaultPatientSetCriteriaConstraint(queryResult: result)
    }

    @ProducerFor(AssayConstraint.TRIAL_NAME_CONSTRAINT)
    AssayConstraint createTrialNameConstraint(Map<String, Object> params) {
	BindingUtils.validateParameterNames(['name'], params)
	String name = BindingUtils.getParam(params, 'name')
        new DefaultTrialNameCriteriaConstraint(trialName: name)
    }

    @ProducerFor(AssayConstraint.ASSAY_ID_LIST_CONSTRAINT)
    AssayConstraint createAssayIdListConstraint(Map<String, Object> params) {
	BindingUtils.validateParameterNames(['ids'], params)
	List<Long> ids = BindingUtils.processLongList('ids', params)
        new AssayIdListCriteriaConstraint(ids: ids)
    }

    @ProducerFor(AssayConstraint.PATIENT_ID_LIST_CONSTRAINT)
    AssayConstraint createPatientIdListConstraint(Map<String, Object> params) {
	BindingUtils.validateParameterNames(['ids'], params)
	List<String> ids = BindingUtils.processStringList('ids', params)
        new PatientIdListCriteriaConstraint(patientIdList: ids)
    }

    @ProducerFor(AssayConstraint.DISJUNCTION_CONSTRAINT)
    AssayConstraint createDisjunctionConstraint(Map<String, Object> params, createConstraint) {
	disjunctionConstraintFactory.createDisjunctionConstraint params, createConstraint
    }
}
