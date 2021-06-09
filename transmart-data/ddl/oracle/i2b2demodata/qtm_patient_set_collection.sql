--
-- Type: TABLE; Owner: I2B2DEMODATA; Name: QTM_PATIENT_SET_COLLECTION
--
 CREATE TABLE "I2B2DEMODATA"."QTM_PATIENT_SET_COLLECTION"
  (	"PATIENT_SET_COLL_ID" NUMBER(10,0) NOT NULL ENABLE,
"RESULT_INSTANCE_ID" NUMBER(5,0),
"SET_INDEX" NUMBER(10,0),
"PATIENT_NUM" NUMBER(38,0),
 PRIMARY KEY ("PATIENT_SET_COLL_ID")
 USING INDEX
 TABLESPACE "I2B2_INDEX"  ENABLE
  ) SEGMENT CREATION DEFERRED
 TABLESPACE "I2B2" ;
--
-- Type: REF_CONSTRAINT; Owner: I2B2DEMODATA; Name: QTM_FK_PSC_RI
--
ALTER TABLE "I2B2DEMODATA"."QTM_PATIENT_SET_COLLECTION" ADD CONSTRAINT "QTM_FK_PSC_RI" FOREIGN KEY ("RESULT_INSTANCE_ID")
 REFERENCES "I2B2DEMODATA"."QTM_QUERY_RESULT_INSTANCE" ("RESULT_INSTANCE_ID") ENABLE;
--
-- Type: INDEX; Owner: I2B2DEMODATA; Name: QTM_IDX_QPSC_RIID
--
CREATE INDEX "I2B2DEMODATA"."QTM_IDX_QPSC_RIID" ON "I2B2DEMODATA"."QTM_PATIENT_SET_COLLECTION" ("RESULT_INSTANCE_ID")
TABLESPACE "I2B2_INDEX" ;

--
-- Type: SEQUENCE; Owner: I2B2DEMODATA; Name: QTM_SQ_QPR_PCID
--
CREATE SEQUENCE  "I2B2DEMODATA"."QTM_SQ_QPR_PCID"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;


-- no trigger in i2b2
--
-- Type: TRIGGER; Owner: I2B2DEMODATA; Name: TR_QTM_PSC_PSC_ID
--
---  CREATE OR REPLACE TRIGGER "I2B2DEMODATA"."TR_QTM_PSC_PSC_ID"
---   before insert on "I2B2DEMODATA"."QTM_PATIENT_SET_COLLECTION"
---   for each row
---begin
---   if inserting then
---      if :NEW."PATIENT_SET_COLL_ID" is null then
---         select QTM_SQ_QPR_PCID.nextval into :NEW."PATIENT_SET_COLL_ID" from dual;
---      end if;
---   end if;
---end;
---/
---ALTER TRIGGER "I2B2DEMODATA"."TR_QTM_PSC_PSC_ID" ENABLE;