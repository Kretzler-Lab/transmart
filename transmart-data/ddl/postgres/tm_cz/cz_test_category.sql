--
-- Name: cz_test_category; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE cz_test_category (
    test_category_id int NOT NULL,
    test_category character varying(255),
    test_sub_category1 character varying(255),
    test_sub_category2 character varying(255),
    person_id int
);

--
-- Name: cz_test_category_pk; Type: CONSTRAINT; Schema: tm_cz; Owner: -
--
ALTER TABLE ONLY cz_test_category
    ADD CONSTRAINT cz_test_category_pk PRIMARY KEY (test_category_id);

--
-- Name: tf_trg_cz_test_category_id(); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE FUNCTION tf_trg_cz_test_category_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin            
    if new.test_category_id is null then          
	select nextval('tm_cz.seq_cz') into new.test_category_id ;
    end if;

    return new;
end;
$$;

--
-- Name: trg_cz_test_category_id; Type: TRIGGER; Schema: tm_cz; Owner: -
--
CREATE TRIGGER trg_cz_test_category_id BEFORE INSERT ON cz_test_category FOR EACH ROW EXECUTE PROCEDURE tf_trg_cz_test_category_id();

--
-- Name: cz_test_category_cz_perso_fk1; Type: FK CONSTRAINT; Schema: tm_cz; Owner: -
--
ALTER TABLE ONLY cz_test_category
    ADD CONSTRAINT cz_test_category_cz_perso_fk1 FOREIGN KEY (person_id) REFERENCES cz_person(person_id);

--
-- Name: seq_cz; Type: SEQUENCE; Schema: tm_cz; Owner: -
--
CREATE SEQUENCE seq_cz
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 20;

