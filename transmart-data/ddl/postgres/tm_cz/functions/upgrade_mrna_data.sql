--
-- Name: upgrade_mrna_data(bigint); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE FUNCTION upgrade_mrna_data(currentjobid bigint DEFAULT 0::bigint) RETURNS void
    LANGUAGE plpgsql
    AS $_$
DECLARE

  
	--Audit variables
	newJobFlag 	numeric(1);
	databaseName 	varchar(100);
	procedureName varchar(100);
	jobID 		integer;
	stepCt 		integer;
	rowCt           integer;
  
	gexStudy	varchar(200);
	gexSource	varchar(200);
	pExists		integer;
	tText		varchar(2000);
  
	gexCt integer;
	gexSize integer;
	gex_study_array deapp.de_subject_sample_mapping[] = array(select row (trial_name, source_cd) from (select distinct trial_name
		  ,coalesce(source_cd,'STD') as source_cd
		  from de_subject_sample_mapping
		  where platform = 'MRNA_AFFYMETRIX'
		  order by trial_name
		  	,source_cd) AS dssm);
  
	-- JEA@20120602	New


BEGIN
    gexSize = array_length(gex_study_array,1);
    stepCt := 0;
	
	--Set Audit Parameters
	newJobFlag := 0; -- False (Default)
	jobID := currentJobID;

	databaseName := 'TM_CZ';
	procedureName := 'UPGRADE_MRNA_DATA';

	--Audit JOB Initialization
	--If Job ID does not exist, then this is a single procedure run and we need to create it
	IF(coalesce(jobID::text, '') = '' or jobID < 1)
	THEN
		newJobFlag := 1; -- True
		perform cz_start_audit (procedureName, databaseName, jobID);
	END IF;
	
	stepCt := stepCt + 1;
	perform cz_write_audit(jobId,databaseName,procedureName,'Start upgrade_mrna_data',0,stepCt,'Done');
    COMMIT;
	
	--	get trial_names for all gex data
	

	
	stepCt := stepCt + 1; get diagnostics rowCt := ROW_COUNT;
	perform cz_write_audit(jobId,databaseName,procedureName,'Bulk Collect trial_names',rowCt,stepCt,'Done');
	gexCt := 0;
	for i in 0 .. (gexSize - 1)
	loop
		gexStudy := gex_study_array[i].trial_name;
		gexSource := gex_study_array[i].source_cd;
		
		--	check if new table is partitioned and if partition exists
		
		select count(*) into pExists
		from all_tables
		where table_name = 'DE_SUBJECT_MICROARRAY_DATA_NEW'
		  and partitioned = 'YES';
		  
		if pExists > 0 then
			select count(*) into pExists
			from all_tab_partitions
			where table_name = 'DE_SUBJECT_MICROARRAY_DATA_NEW'
			  and partition_name = gexStudy || ':' || gexSource;
			  
			if pExists = 0 then
				tText := 'alter table deapp.de_subject_microarray_data_new add PARTITION "' || gexStudy || ':' || gexSource || 
						'"  VALUES (' || '''' || gexStudy || ':' || gexSource || '''' || ') ' ||
						   'NOLOGGING COMPRESS TABLESPACE "TRANSMART" ';
				EXECUTE(tText);
				stepCt := stepCt + 1;
				perform cz_write_audit(jobId,databaseName,procedureName,'Added ' || gexStudy || ':' || gexSource || ' partition to de_subject_microarray_data_new',0,stepCt,'Done');
			end if;
		end if;
		
		insert into deapp.de_subject_microarray_data_new
		(trial_source
		,trial_name
		,probeset_id
		,assay_id
		,patient_id
		,raw_intensity
		,log_intensity
		,zscore
		)
		select sm.trial_name || ':' || coalesce(sm.source_cd,'STD')
			  ,sm.trial_name
			  ,sd.probeset_id
			  ,sm.assay_id
			  ,sm.patient_id
			  ,sd.raw_intensity
			  ,sd.log_intensity
			  ,sd.zscore
		from de_subject_sample_mapping sm
			,de_subject_microarray_data sd
		where sm.trial_name = gexStudy
		  and sm.source_cd = gexSource
		  and sm.platform = 'MRNA_AFFYMETRIX'
		  and sm.assay_id = sd.assay_id;
		  
		stepCt := stepCt + 1; get diagnostics rowCt := ROW_COUNT;
		perform cz_write_audit(jobId,databaseName,procedureName,'Inserted ' || gexStudy || ':' || gexSource || ' to new table',rowCt,stepCt,'Done');
			
	end loop;
		
	--	drop indexes on de_subject_microarray_data
	
	perform i2b2_mrna_index_maint('DROP',null,jobId);
	
	--	rename existing de_subject_microarray_data to _old
	
	alter table deapp.de_subject_microarray_data rename to de_subject_microarray_data_old;
	
	stepCt := stepCt + 1;
	perform cz_write_audit(jobId,databaseName,procedureName,'Rename old de_subject_microarray_data',0,stepCt,'Done');
		
	--	rename _new to de_subject_microarray_data
	
	alter table deapp.de_subject_microarray_data_new rename to de_subject_microarray_data;
	
	stepCt := stepCt + 1;
	perform cz_write_audit(jobId,databaseName,procedureName,'Rename old de_subject_microarray_data',0,stepCt,'Done');
		
	--	add indexes to de_subject_microarray_data
	
	perform i2b2_mrna_index_maint('ADD',null,jobId);
			
	stepCt := stepCt + 1;
	perform cz_write_audit(jobId,databaseName,procedureName,'End i2b2_audit',0,stepCt,'Done');
	
    COMMIT;
	--Cleanup OVERALL JOB if this proc is being run standalone
	IF newJobFlag = 1
	THEN
		perform cz_end_audit (jobID, 'SUCCESS');
	END IF;

	EXCEPTION
	WHEN OTHERS THEN
		--Handle errors.
		perform cz_error_handler(jobId, procedureName, SQLSTATE, SQLERRM);
		--End Proc
		perform cz_end_audit (jobID, 'FAIL');
	
END;

 
$_$;
