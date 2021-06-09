--
-- Type: TABLE; Owner: I2B2DEMODATA; Name: QTM_QUERY_MASTER
--
 CREATE TABLE "I2B2DEMODATA"."QTM_QUERY_MASTER"
  (	"QUERY_MASTER_ID" NUMBER(5,0) NOT NULL ENABLE,
"NAME" VARCHAR2(250 BYTE) NOT NULL ENABLE,
"USER_ID" VARCHAR2(50 BYTE) NOT NULL ENABLE,
"GROUP_ID" VARCHAR2(50 BYTE) NOT NULL ENABLE,
"MASTER_TYPE_CD" VARCHAR2(2000 BYTE),
"PLUGIN_ID" NUMBER(10,0),
"CREATE_DATE" DATE NOT NULL ENABLE,
"DELETE_DATE" DATE,
"DELETE_FLAG" VARCHAR2(3 BYTE),
"GENERATED_SQL" CLOB,
"REQUEST_XML" CLOB,
"I2B2_REQUEST_XML" CLOB,
"PM_XML" CLOB,
 PRIMARY KEY ("QUERY_MASTER_ID")
 USING INDEX
 TABLESPACE "I2B2_INDEX"  ENABLE
  ) SEGMENT CREATION DEFERRED
 TABLESPACE "I2B2"
LOB ("GENERATED_SQL") STORE AS SECUREFILE (
 TABLESPACE "I2B2" ENABLE STORAGE IN ROW CHUNK 8192
 NOCACHE LOGGING  NOCOMPRESS  KEEP_DUPLICATES )
LOB ("REQUEST_XML") STORE AS SECUREFILE (
 TABLESPACE "I2B2" ENABLE STORAGE IN ROW CHUNK 8192
 NOCACHE LOGGING  NOCOMPRESS  KEEP_DUPLICATES )
LOB ("I2B2_REQUEST_XML") STORE AS SECUREFILE (
 TABLESPACE "I2B2" ENABLE STORAGE IN ROW CHUNK 8192
 NOCACHE LOGGING  NOCOMPRESS  KEEP_DUPLICATES )
LOB ("PM_XML") STORE AS SECUREFILE (
 TABLESPACE "I2B2" ENABLE STORAGE IN ROW CHUNK 8192
 NOCACHE LOGGING  NOCOMPRESS  KEEP_DUPLICATES ) ;
--
-- Type: INDEX; Owner: I2B2DEMODATA; Name: QTM_IDX_QM_UGID
--
CREATE INDEX "I2B2DEMODATA"."QTM_IDX_QM_UGID" ON "I2B2DEMODATA"."QTM_QUERY_MASTER" ("USER_ID", "GROUP_ID", "MASTER_TYPE_CD")
TABLESPACE "I2B2_INDEX" ;
--
-- Type: SEQUENCE; Owner: I2B2DEMODATA; Name: QTM_SQ_QM_QMID
--
CREATE SEQUENCE  "I2B2DEMODATA"."QTM_SQ_QM_QMID"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

-- no trigger in i2b2

--
-- Type: TRIGGER; Owner: I2B2DEMODATA; Name: TRG_QTM_QM_QM_ID
--
---  CREATE OR REPLACE TRIGGER "I2B2DEMODATA"."TRG_QTM_QM_QM_ID"
---   before insert on "I2B2DEMODATA"."QTM_QUERY_MASTER"
---   for each row
---begin
---   if inserting then
---      if :NEW."QUERY_MASTER_ID" is null then
---         select QTM_SQ_QM_QMID.nextval into :NEW."QUERY_MASTER_ID" from dual;
---      end if;
---   end if;
---end;
---/
---ALTER TRIGGER "I2B2DEMODATA"."TRG_QTM_QM_QM_ID" ENABLE;