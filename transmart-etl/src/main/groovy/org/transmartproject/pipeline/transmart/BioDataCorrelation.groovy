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
  

package org.transmartproject.pipeline.transmart


import groovy.sql.Sql;
import groovy.sql.GroovyRowResult
import groovy.util.logging.Slf4j

import org.transmartproject.pipeline.util.Util

@Slf4j('logger')
class BioDataCorrelation {

    Sql biomart
    long bioDataCorrelDescrId
    String organism, source

    void loadBioDataCorrelation(File goa, Map geneId){

	String qry = """ insert into bio_data_correlation(
				bio_data_id, asso_bio_data_id, bio_data_correl_descr_id)
			 select distinct p.bio_marker_id,
				g.bio_marker_id,
				c.bio_data_correl_descr_id
			 from bio_marker p, bio_marker g, bio_data_correl_descr c
			 where p.bio_marker_type = 'PATHWAY' and g.bio_marker_type = 'GENE'
			     and p.primary_external_id = ? and g.primary_external_id = ?
			     and c.correlation='PATHWAY GENE' and g.organism=? """
	if(goa.size()>0){

	    logger.info ("Start loading ${goa.toString()} into BIO_DATA_CORRELATION ...")

	    goa.eachLine{
		String [] str = it.split("\t")
		//logger.info "insert (${str[0]}, ${str[1]}, $organism) into BIO_DATA_CORRELATION ..."
		biomart.execute(qry, [
		    str[0],
		    (String) geneId[str[1].toUpperCase()],
		    organism
		])
	    }
	} else {
	    logger.info "Cannot open the file:" + goa.toString()
	}
    }

    void loadBioDataCorrelation(File bdc){

        Boolean isPostgres = Util.isPostgres()
        String qry;

        if(isPostgres) {
	    qry = """ insert into bio_data_correlation(
		           bio_data_id, asso_bio_data_id, bio_data_correl_descr_id)
		      select p.bio_marker_id, 
			     g.bio_marker_id, 
			     c.bio_data_correl_descr_id
		      from bio_marker p, bio_marker g, bio_data_correl_descr c
		      where p.bio_marker_type = 'PATHWAY' and g.bio_marker_type = 'GENE'
			  and p.primary_external_id = ? and g.primary_external_id = ? 
			  and c.correlation='PATHWAY GENE' and g.organism=? 
		      except
                      select bio_data_id, asso_bio_data_id, bio_data_correl_descr_id 
			 from bio_data_correlation """
        }else{
	    qry = """ insert into bio_data_correlation(
		          bio_data_id, asso_bio_data_id, bio_data_correl_descr_id)
		      select p.bio_marker_id, 
		             g.bio_marker_id, 
			     c.bio_data_correl_descr_id
		      from bio_marker p, bio_marker g, bio_data_correl_descr c
		      where p.bio_marker_type = 'PATHWAY' and g.bio_marker_type = 'GENE'
			  and p.primary_external_id = ? and g.primary_external_id = ?
			  and c.correlation='PATHWAY GENE' and g.organism=? 
		      minus
                      select bio_data_id, asso_bio_data_id, bio_data_correl_descr_id 
		      from bio_data_correlation """
        }
	if(bdc.size()>0){
	    bdc.eachLine{
		String [] str = it.split("\t")
		logger.info "insert (${str[0]}, ${str[1]}, $organism) into BIO_DATA_CORRELATION ..."
		biomart.execute(qry, [str[0], str[1], organism])
	    }
	} else {
	    logger.info "Cannot open the file:" + bdc.toString()
	}
    }

    void loadBioDataCorrelation(Sql biomartuser, String pathwayDataTable){

        logger.info ("Start populating bio_data_correlation using table ${pathwayDataTable} ...")

//      Boolean isPostgres = Util.isPostgres()

        String qryOrg
        String qryData
        String qryDesc
        String qryPath
        String qryGene
        String qryExists
        String qryInsert

        long descId
        long geneId
        long dePathwayId
        String organism

        qryOrg    =   "select distinct organism from ${pathwayDataTable}"
        qryData   = """select pathway, gene_symbol from ${pathwayDataTable}
                       where organism=? order by pathway"""
        qryDesc   = """select bio_data_correl_descr_id from biomart.bio_data_correl_descr
                       where correlation = 'PATHWAY GENE'"""
        qryPath   = """select bio_marker_id from biomart.bio_marker
                       where primary_external_id=? and bio_marker_type='PATHWAY' and organism=?"""
        qryGene   = """select bio_marker_id from biomart.bio_marker
                       where bio_marker_name=? and bio_marker_type='GENE' and organism=?"""
        qryInsert = """insert into biomart.bio_data_correlation
	                  (bio_data_id, asso_bio_data_id, bio_data_correl_descr_id)
                          values(?,?,?)"""
        qryExists = """select count(*) from biomart.bio_data_correlation
                       where bio_data_id=? and asso_bio_data_id=? and bio_data_correl_descr_id=?"""

//      qry = """ insert into biomart.bio_data_correlation(
//			bio_data_id, asso_bio_data_id, bio_data_correl_descr_id)
//			select p.bio_marker_id, g.bio_marker_id, c.bio_data_correl_descr_id
//			from biomart.bio_marker p,
//                           biomart.bio_marker g, 
//			     biomart.bio_data_correl_descr c
//			where p.bio_marker_type = 'PATHWAY' and g.bio_marker_type = 'GENE'
//			    and p.primary_external_id = ? and g.bio_marker_name = ?
//			    and c.correlation='PATHWAY GENE' and g.organism=? 
//			    and p.organism=? 
//			except
//			select bio_data_id, asso_bio_data_id, bio_data_correl_descr_id
//			from biomart.bio_data_correlation"""

        String lastPath = " "
            
        GroovyRowResult descResult = biomartuser.firstRow(qryDesc)
        descId = descResult[0]

        biomart.withTransaction {
            biomartuser.eachRow(qryOrg) { qo ->
                organism = qo.organism.toUpperCase()
                biomart.withBatch(1000, qryInsert, { ps ->
                    biomartuser.eachRow(qryData, [organism]) { qd ->
                        if(qd.pathway != lastPath) {
                            lastPath = qd.Pathway

                            GroovyRowResult rowResult = biomartuser.firstRow(qryPath, [qd.pathway,organism])
                            if(rowResult != null) {
                                dePathwayId = rowResult[0]
                                logger.info "Pathway '${qd.pathway}' id '${dePathwayId}'"
                            }
                            else {
                                logger.info "Pathway '${qd.pathway}' id not found..."
                                dePathwayId = null
                            }
                        }
                        GroovyRowResult geneResult = biomartuser.firstRow(qryGene, [qd.gene_symbol,organism])
                        if(dePathwayId != null && geneResult != null) {
                            geneId = geneResult[0]
                            GroovyRowResult existResult = biomartuser.firstRow(qryExists, [dePathwayId,geneId,descId])
                            int count = existResult[0]
                            if(count > 0) {
                                logger.info "$dePathwayId:$geneId already exists in BIO_DATA_CORRELATION ..."
                            }
                            else {
                                logger.info "loading '${qd.pathway}' '${qd.gene_symbol}' '${organism}'"
                                ps.addBatch([dePathwayId,geneId,descId])
                            }
                        }
                        else {
                            logger.info "Gene '${qd.gene_symbol}' not found for '${organism}'"
                        }
                    }
                })
            }
        }

        logger.info ("End populating bio_data_correlation using table ${pathwayDataTable} ...")
    }

    void insertBioDataCorrelation(long pathwayMarkerId, long geneMarkerId, long dataCorrelDecrId){
	String qry = "insert into bio_data_correlation(bio_data_id,asso_bio_data_id,bio_data_correl_descr_id) values(?,?,?)"

	if(isBioDataCorrelationExist(pathwayMarkerId, geneMarkerId, dataCorrelDecrId)){
            //logger.info "$pathwayMarkerId:$geneMarkerId:$dataCorrelDecrId already exists in BIO_DATA_CORRELATION ..."
	}else{
	    logger.info "Insert $pathwayMarkerId:$geneMarkerId:$dataCorrelDecrId into BIO_DATA_CORRELATION ..."
	    biomart.execute(qry, [
		pathwayMarkerId,
		geneMarkerId,
		dataCorrelDecrId
	    ])
	}
    }

    boolean isBioDataCorrelationExist(String pathway, String geneId){
	String qry = """ select count(*) 
		         from bio_data_correlation c,
                              bio_marker p, bio_marker g,
                              bio_data_correl_descr d
			 where p.bio_marker_type = 'PATHWAY' and g.bio_marker_type = 'GENE'
			     and p.primary_external_id = ?
                             and g.primary_external_id = ?
			     and d.correlation='PATHWAY GENE'
                             and c.bio_data_id=p.bio_marker_id
			     and c.asso_bio_data_id = g.bio_marker_id """
	def res = biomart.firstRow(qry, [pathway, geneId])
	if(res[0] > 0) return true
	else return false
    }
    boolean isBioDataCorrelationExist(long pathwayMarkerId, long geneMarkerId, long dataCorrelDecrId){
	String qry = """ select count(*) from bio_data_correlation 
		         where bio_data_id=? and asso_bio_data_id=? and bio_data_correl_descr_id=? """
	def res = biomart.firstRow(qry, [
	    pathwayMarkerId,
	    geneMarkerId,
	    dataCorrelDecrId
	])
	if(res[0] > 0) return true
	else return false
    }

    void setBioDataCorrelDescrId(long bioDataCorrelDescrId){
		this.bioDataCorrelDescrId = bioDataCorrelDescrId
    }

    void setOrganism(String organism){
	this.organism = organism
    }

    void setSource(String sourcec){
	this.source = sourcec
    }

    void setBiomart(Sql biomart){
	this.biomart = biomart
    }
}
