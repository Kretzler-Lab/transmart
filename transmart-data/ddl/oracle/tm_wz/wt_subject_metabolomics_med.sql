--
-- Type: TABLE; Owner: TM_WZ; Name: WT_SUBJECT_METABOLOMICS_MED
--
 CREATE TABLE "TM_WZ"."WT_SUBJECT_METABOLOMICS_MED" 
  (	"PROBESET" VARCHAR2(500 BYTE), 
"INTENSITY_VALUE" NUMBER(38,4), 
"LOG_INTENSITY" NUMBER(38,4), 
"ASSAY_ID" NUMBER(18,0), 
"PATIENT_ID" NUMBER(18,0), 
"SAMPLE_ID" NUMBER(18,0), 
"SUBJECT_ID" VARCHAR2(100 BYTE), 
"TRIAL_NAME" VARCHAR2(50 BYTE), 
"TIMEPOINT" VARCHAR2(100 BYTE), 
"PVALUE" FLOAT(126), 
"NUM_CALLS" NUMBER, 
"MEAN_INTENSITY" NUMBER(38,4), 
"STDDEV_INTENSITY" NUMBER(38,4), 
"MEDIAN_INTENSITY" NUMBER(38,4), 
"ZSCORE" NUMBER(38,4)
  ) SEGMENT CREATION IMMEDIATE
 TABLESPACE "TRANSMART" ;
