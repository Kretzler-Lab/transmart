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
package org.transmart.biomart

class Patient {
    String addressZipCode
    Long bioClinicalTrialPGroupId
    Long bioExperimentId
    Date birthDate
    String birthDateOrig
    String countryCode
    String ethnicGroupCode
    String firstName
    String genderCode
    String informedConsentCode
    String lastName
    String middleName
    String raceCode

    static mapping = {
	table 'BIOMART.BIO_PATIENT'
	id generator: 'sequence', params: [sequence: 'BIOMART.SEQ_BIO_DATA_ID'], column: 'BIO_PATIENT_ID'
        version false

        bioClinicalTrialPGroupId column: 'BIO_CLINICAL_TRIAL_P_GROUP_ID'
    }

    static constraints = {
	addressZipCode nullable: true, maxSize: 400
	bioClinicalTrialPGroupId nullable: true
	bioExperimentId nullable: true
	birthDate nullable: true
	birthDateOrig nullable: true, maxSize: 400
	countryCode nullable: true, maxSize: 400
	ethnicGroupCode nullable: true, maxSize: 400
	firstName nullable: true, maxSize: 400
	genderCode nullable: true, maxSize: 400
	informedConsentCode nullable: true, maxSize: 400
	lastName nullable: true, maxSize: 400
	middleName nullable: true, maxSize: 400
	raceCode nullable: true, maxSize: 400
    }
}
