/*************************************************************************
 * tranSMART - translational medicine data mart
 *
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 *
 * This product includes software developed at Janssen Research & Development, LLC.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 ******************************************************************/

package i2b2

import groovy.transform.CompileStatic

@CompileStatic
class SnpDataset {
    public static final String SAMPLE_TYPE_NORMAL = 'NORMAL'
    public static final String SAMPLE_TYPE_DISEASE = 'DISEASE'

    String conceptName
    String datasetName
    Long id
    Long pairedDatasetId
    String patientGender
    Long patientNum
    String platformName
    String subjectId
    String timePoint
    String trialName

    /* conceptId is like '1222211', stored in table 'de_subject_snp_dataset', column 'concept_cd',
     * mapped to 'concept_cd' in table 'patient_dimension'.
     * The conceptName is the display name, like 'Normal Blood Lymphocyte', stored in name_char in table 'patient_dimension' */
    String conceptId

    /* This is used to organized paired datasets in GenePattern sample info text file
     The value is stored in database table 'de_subject_snp_dataset', column 'sample_type' */
    String sampleType
}
