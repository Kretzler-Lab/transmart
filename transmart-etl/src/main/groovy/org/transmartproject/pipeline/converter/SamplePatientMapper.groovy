/*************************************************************************
 * tranSMART - translational medicine data mart
 * 
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 * 
 * This product includes software developed at Janssen Research & Development, LLC.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version, along with the following terms:
 *
 * 1.	You may convey a work based on this program in accordance with section 5,
 *      provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it,
 *      in any medium, provided that you retain the above notices.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************/
  

package org.transmartproject.pipeline.converter

import java.util.Map;

import org.transmartproject.pipeline.transmart.SubjectSampleMapping;
import org.transmartproject.pipeline.util.Util

import groovy.sql.Sql
import groovy.util.logging.Slf4j

@Slf4j('logger')
class SamplePatientMapper {

    File exptGsmMappingFile, gsmMappingFile, sampleInfoFile
    String outputDirectory
    Sql deapp
    SubjectSampleMapping subjectSampleMapping
    Map sampleIdMapping, samples, sampleTypeMap
    Map samplePatientMap, patientMap, exptGsmMapping, exptPatientMapping

    /**
     *  load a mapping between EXPT to GSMP used in Copy Number files and 
     *  created by Deven for GSE14860 SNP dataset
     *  
     * @return		mapping between expt and GSM:GSM
     */
    void createExptMappingFromFile(){

	exptGsmMapping = [:]
	exptPatientMapping = [:]

	String [] str
	String gsm1, gsm2, patientNum1, patientNum2, sampleId1, sampleId2
	if(exptGsmMappingFile.exists()){

	    logger.info("Reading " + exptGsmMappingFile.toString())

	    exptGsmMappingFile.eachLine{
		str = it.split(",")
		if(str[0].indexOf("experiment_id") == -1){
		    gsm1 = str[1].trim().replace(".CEL", "")
		    gsm2 = str[2].trim().replace(".CEL", "")
		    exptGsmMapping[str[0].trim()] = gsm1 + ":" + gsm2

		    sampleId1 = sampleIdMapping[gsm1]
		    sampleId2 = sampleIdMapping[gsm2]
		    if(!sampleId1.equals(null)) patientNum1 = patientMap[sampleId1]
		    if(!sampleId2.equals(null)) patientNum2 = patientMap[sampleId2]

		    if(!patientNum1.equals(null) && !patientNum2.equals(null) && !patientNum1.equals(patientNum2)){
			logger.error "Patient Number are inconsistent: $gsm1($patientNum1) and $gsm2($patientNum2)"
			throw new RuntimeException("Patient Number are inconsistent: $gsm1($patientNum1) and $gsm2($patientNum2)")
		    }else{
			if(!patientNum1.equals(null)) {
			    exptPatientMapping[str[0].trim()] = patientNum1
			}
			else if (!patientNum2.equals(null)) {
			    exptPatientMapping[str[0].trim()] = patientNum2
			}
			else logger.error "Cannot find patient number for: $it "
		    }
		}
	    }
	}else{
	    logger.warn("Cannot find " + exptGsmMappingFile.toString())
	}
    }

    /**
     *  parse curated sample info file in tab-delimited format and return a map with GSM# as its key, 
     *  and also populate the map sampleIdMapping: GSM# -> subject id
     *   
     * @return
     */
    Map loadSampleInfo(){
	Map sampleInfo = [:]
	sampleIdMapping = [:]
	samples = [:]
	sampleTypeMap = [:]

	String [] str
	if(sampleInfoFile.exists()){
	    logger.info("Reading " + sampleInfoFile.toString())
	    int index = 1
	    sampleInfoFile.eachLine{
		//if((it.indexOf("study_id") == -1) && (it.indexOf("Data+SNP_Profiling+PLATFORM+TISSUETYPE") >= 0)){
		if((it.indexOf("study_id") == -1) && (it.toUpperCase().indexOf("SNP_PROFILING") >= 0)){
		    //logger.info it
		    str = it.split("\t")
		    if(str.size() != 9){
			logger.warn("Line: " + index + " missing column(s) in: " + sampleInfoFile.toString())
			logger.info index + ":  " + str.size() + ":  " + it
		    } else{
			Map dataMap = [:]

			dataMap["TRIAL_NAME"] = str[0].trim()
			dataMap["SUBJECT_ID"] = str[2].trim()

			//dataMap["SAMPLE_CD"] = str[3].trim()
			dataMap["GPL_ID"] = str[4].trim()

			// sample type & sample concept code
			String sampleType = str[5].trim()
			dataMap["SAMPLE_TYPE"] = sampleType
			sampleTypeMap[sampleType] = 1

			dataMap["CATEGORY_CD"] = str[8].trim()
			dataMap["SOURCE_CD"] = "STD"
			dataMap["PLATFORM"] = "SNP_profiling"

			sampleInfo[str[3].trim()] = dataMap
			sampleIdMapping[str[3].trim()] = str[2].trim()
			samples[str[2].trim()] = 1
		    }
		}
		index++
	    }
	}else{
	    logger.warn("Cannot find " + sampleInfoFile.toString())
	}
	return sampleInfo
    }

    /**
     * temporary used to populate de_subject_sample_mapping table
     * 
     * @return
     */
    Map createGsmMappingFromFile(){

	Map gsmMapping = [:]
	Map sampleSubjectMapping = subjectSampleMapping.getSubjectSampleMapping()

	Map dataMap = [:]
	sampleIdMapping = [:]

	String gsm1 = " ", gsm2 = " ", gsm3 = " "
	String [] str
	if(gsmMappingFile.exists()){

	    logger.info("Reading " + gsmMappingFile.toString())

	    int index = 0
	    gsmMappingFile.eachLine{
		if((it.indexOf("study_id") == -1) && (it.indexOf("GPL2004_2005") >= 0)){
		    logger.info it
		    str = it.split("\t")
		    if(str.size() != 9){
			logger.warn("Line: " + index + " missing column(s) in: " + gsmMappingFile.toString())
			logger.info index + ":  " + str.size() + ":  " + it
		    } else{
			def patientNum = sampleSubjectMapping[str[2].trim()]
			dataMap["PATIENT_ID"] = patientNum
			dataMap["SAMPLE_ID"] = patientNum

			dataMap["TRIAL_NAME"] = str[0].trim()
			dataMap["SUBJECT_ID"] = str[2].trim()

			dataMap["SAMPLE_CD"] = str[3].trim()
			dataMap["GPL_ID"] = str[4].trim()

			// sample type & sample concept code
			String sampleType = str[5].trim()
			dataMap["SAMPLE_TYPE"] = sampleType
			if(sampleType.indexOf("Normal") == -1) {
			    dataMap["CONCEPT_CODE"] = 958943
			    dataMap["SAMPLE_TYPE_CD"] = 958943
			}
			else{
			    dataMap["CONCEPT_CODE"] = 958942
			    dataMap["SAMPLE_TYPE_CD"] = 958943
			}

			dataMap["CATEGORY_CD"] = str[8].trim()
			dataMap["SOURCE_CD"] = "STD"
			dataMap["PLATFORM"] = "SNP_profiling"
			dataMap["PLATFORM_CD"] = 958940
			dataMap["ASSAY_ID"] = index

			dataMap["DATA_UID"] = dataMap["CONCEPT_CODE"] + "-" + dataMap["PATIENT_ID"]

			sampleIdMapping[str[3].trim()] = str[2].trim()
			//subjectSampleMapping.insertSubjectSampleMapping(dataMap)
		    }
		    index++
		}
	    }
	}else{
	    logger.warn("Cannot find " + gsmMappingFile.toString())
	}

	return gsmMapping
    }

    Map loadSamplePatientMap(){
	Map samplePatientMap = [:]
	if(patientMap.equals(null) || patientMap.size() ==0){
	    logger.error "patientMap is null"
	}else{
	    sampleIdMapping.each{k, v ->
		if(patientMap[v].equals(null))
		    logger.info k + "\t" + v
		samplePatientMap[k] = patientMap[v]
	    }
	}
	return samplePatientMap
    }

    Map getSampleTypeMap(){
	return sampleTypeMap
    }

    Map getSamples() {
	return samples
    }

    Map getExptPatientMapping(){
	return exptPatientMapping
    }

    Map getSampleIdMapping(){
	return sampleIdMapping
    }

    Map getExptGsmMapping(){
	return exptGsmMapping
    }

    Map getSamplePatientMap(){
	loadSamplePatientMap()
	return samplePatientMap
    }

    void setSql(Sql deapp){
	this.deapp = deapp
    }

    void setPatientMap(Map patientMap){
	this.patientMap = patientMap
    }

    void setExptGsmMappingFile(File exptGsmMappingFile){
	this.exptGsmMappingFile = exptGsmMappingFile
    }

    void setSampleInfoFile(File sampleInfoFile){
	this.sampleInfoFile = sampleInfoFile
    }

    void setSubjectSampleMapping(SubjectSampleMapping subjectSampleMapping){
	this.subjectSampleMapping = subjectSampleMapping
    }
}
