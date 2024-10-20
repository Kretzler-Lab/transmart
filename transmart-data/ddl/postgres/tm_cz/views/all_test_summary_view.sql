--
-- Name: all_test_summary_view; Type: VIEW; Schema: tm_cz; Owner: -
--
CREATE VIEW tm_cz.all_test_summary_view AS
    SELECT a.test_run_id
	   , a.test_run_name
	   , to_char(a.start_date, 'DD/MM/YYYY HH24:MI:SS'::text) AS start_date
	   , to_char(a.end_date, 'DD/MM/YYYY HH24:MI:SS'::text) AS end_date
	   , a.status, d.test_category
	   , d.test_sub_category1
	   , d.test_sub_category2
	   , sum(CASE WHEN ((b.status)::text = 'PASS'::text) THEN 1 ELSE 0 END) AS pass
	   , sum(CASE WHEN ((b.status)::text = 'WARNING'::text) THEN 1 ELSE 0 END) AS warning
	   , sum(CASE WHEN ((b.status)::text = 'FAIL'::text) THEN 1 ELSE 0 END) AS fail
	   , sum(CASE WHEN ((b.status)::text = 'ERROR'::text) THEN 1 ELSE 0 END) AS error
	   , count(b.status) AS total
	   , c.version_name AS db_version
      FROM (((tm_cz.az_test_run a
	      JOIN tm_cz.az_test_step_run b
		      ON ((a.test_run_id = b.test_run_id)))
	      JOIN tm_cz.cz_dw_version c
		      ON ((c.dw_version_id = a.dw_version_id)))
	      JOIN tm_cz.cz_test_category d
		      ON ((d.test_category_id = a.test_category_id)))
     GROUP BY a.test_run_id
	      , a.test_run_name
	      , to_char(a.start_date, 'DD/MM/YYYY HH24:MI:SS'::text)
	      , to_char(a.end_date, 'DD/MM/YYYY HH24:MI:SS'::text)
	      , a.status
	      , d.test_category
	      , d.test_sub_category1
	      , d.test_sub_category2
	      , c.version_name;

