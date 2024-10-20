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

package org.transmartproject.db.dataquery.clinical.variables

import org.transmartproject.core.concept.ConceptFullName
import org.transmartproject.core.concept.ConceptKey
import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.clinical.ClinicalVariable
import org.transmartproject.core.dataquery.clinical.ClinicalVariableColumn
import org.transmartproject.core.exceptions.UnexpectedResultException

class CategoricalVariable extends AbstractComposedVariable implements ClinicalVariableColumn {

    String conceptPath

    String getVariableValue(DataRow<ClinicalVariableColumn, Object> dataRow) {
        for (ClinicalVariable var in innerClinicalVariables) {
            def currentValue = dataRow.getAt(var)

            if (currentValue) {
                if (!(currentValue instanceof String)) {
                    throw new UnexpectedResultException("Expected a string " +
							"for observation in row $dataRow, categorical " +
							"variable $this, when looking at child variable " +
							"$var. Instead, got '$currentValue'")
                }

                return currentValue
            }
        }

        null
    }

    String getLabel() {
        conceptPath
    }

    ConceptKey getKey() {
        ConceptFullName fullName = new ConceptFullName(conceptPath)
        new ConceptKey(fullName.parts[0], fullName.toString())
    }
}
