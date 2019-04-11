--
-- Name: stg_subject_rbm_data_raw; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE stg_subject_rbm_data_raw (
    trial_name character varying(100),
    antigen_name character varying(100),
    value_text character varying(100),
    value_number int,
    timepoint character varying(100),
    assay_id character varying(100),
    sample_id character varying(100),
    subject_id character varying(100),
    site_id character varying(100)
);