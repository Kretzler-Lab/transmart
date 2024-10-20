--
-- Name: am_tag_display_vw; Type: VIEW; Schema: amapp; Owner: -
--
CREATE VIEW am_tag_display_vw AS
    (((((SELECT DISTINCT tass.subject_uid
			 , tass.tag_item_id
			 , tval.value AS display_value
			 , tass.object_type
			 , tass.object_uid
			 , obj_uid.am_data_id AS object_id
	   FROM ((am_tag_association tass
		  JOIN am_data_uid obj_uid
			  ON (((tass.object_uid)::text = (obj_uid.unique_id)::text)))
		  JOIN am_tag_value tval
			  ON ((obj_uid.am_data_id = tval.tag_value_id)))
	  UNION
	 SELECT DISTINCT tass.subject_uid
			 , tass.tag_item_id
			 , bio_val.code_name AS display_value
			 , tass.object_type, tass.object_uid
			 , obj_uid.bio_data_id AS object_id
	   FROM ((am_tag_association tass
		  JOIN biomart.bio_data_uid obj_uid
			  ON (((tass.object_uid)::text = (obj_uid.unique_id)::text)))
		  JOIN biomart.bio_concept_code bio_val
			  ON ((obj_uid.bio_data_id = bio_val.bio_concept_code_id))))
	 UNION
	SELECT DISTINCT tass.subject_uid
			, tass.tag_item_id
			, bio_val.disease AS display_value
			, tass.object_type
			, tass.object_uid
			, obj_uid.bio_data_id AS object_id
	   FROM ((am_tag_association tass
		  JOIN biomart.bio_data_uid obj_uid
			  ON (((tass.object_uid)::text = (obj_uid.unique_id)::text)))
		  JOIN biomart.bio_disease bio_val
			  ON ((obj_uid.bio_data_id = bio_val.bio_disease_id))))
	 UNION
	SELECT DISTINCT tass.subject_uid
			, tass.tag_item_id
			, CASE WHEN ((ati.code_type_name)::text = 'PLATFORM_NAME'::text)
			           THEN (((((((bio_val.platform_type)::text || '/'::text) || (bio_val.platform_technology)::text) || '/'::text) || (bio_val.platform_vendor)::text) || '/'::text) || (bio_val.platform_name)::text)
			       WHEN ((ati.code_type_name)::text = 'VENDOR'::text)
			           THEN (bio_val.platform_vendor)::text
			       WHEN ((ati.code_type_name)::text = 'MEASUREMENT_TYPE'::text)
			           THEN (bio_val.platform_type)::text
			       WHEN ((ati.code_type_name)::text = 'TECHNOLOGY'::text)
			           THEN (bio_val.platform_technology)::text
			       ELSE
				   NULL::text
			       END AS display_value
			, tass.object_type
			, tass.object_uid
			, obj_uid.bio_data_id AS object_id
	   FROM (((am_tag_association tass
		   JOIN biomart.bio_data_uid obj_uid
			   ON (((tass.object_uid)::text = (obj_uid.unique_id)::text)))
		   JOIN biomart.bio_assay_platform bio_val
			   ON ((obj_uid.bio_data_id = bio_val.bio_assay_platform_id)))
		   JOIN am_tag_item ati
			   ON ((ati.tag_item_id = tass.tag_item_id))))
	 UNION
	SELECT DISTINCT tass.subject_uid
			, tass.tag_item_id
			, bio_val.code_name AS display_value
			, tass.object_type
			, tass.object_uid
			, obj_uid.bio_data_id AS object_id
			FROM ((am_tag_association tass
			       JOIN biomart.bio_data_uid obj_uid
				       ON (((tass.object_uid)::text = (obj_uid.unique_id)::text)))
				  JOIN biomart.bio_compound bio_val
					  ON ((obj_uid.bio_data_id = bio_val.bio_compound_id))))
	 UNION
	SELECT DISTINCT tass.subject_uid
			, tass.tag_item_id
			, bio_val.bio_marker_name AS display_value
			, tass.object_type
			, tass.object_uid
			, obj_uid.bio_data_id AS object_id
			FROM ((am_tag_association tass
			       JOIN biomart.bio_data_uid obj_uid
				       ON (((tass.object_uid)::text = (obj_uid.unique_id)::text)))
			       JOIN biomart.bio_marker bio_val
				       ON ((obj_uid.bio_data_id = bio_val.bio_marker_id))))
    UNION
    SELECT DISTINCT tass.subject_uid
		    , tass.tag_item_id
		    , bio_val.obs_name AS display_value
		    , tass.object_type
		    , tass.object_uid
		    , obj_uid.bio_data_id AS object_id
      FROM ((am_tag_association tass
	     JOIN biomart.bio_data_uid obj_uid
		     ON (((tass.object_uid)::text = (obj_uid.unique_id)::text)))
	     JOIN biomart.bio_observation bio_val
		     ON ((obj_uid.bio_data_id = bio_val.bio_observation_id)));

