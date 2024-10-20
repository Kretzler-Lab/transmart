--
-- Type: FUNCTION; Owner: TM_CZ; Name: TEXT_PARSER
--
CREATE OR REPLACE FUNCTION TM_CZ.TEXT_PARSER (
    text_to_parse IN VARCHAR2,
    text_delimiter IN VARCHAR2
)
    --Custom Collection type returned
    RETURN varchar_table

IS
    start_pos NUMBER;
    last_pos   NUMBER;
    string_length INTEGER;
    string_tokens VARCHAR2(32676);
    counter INTEGER;
    token_value VARCHAR2(1000);

    list_values varchar_table;

BEGIN
    -------------------------------------------------------------------------------
    -- Populates a temp_token table with parsed values for any comma separated list.
    -- Requires a type so that multiple records can exist for different uses.
    -- KCR@20090106 - First rev.
    -- Copyright ¿ 2009 Recombinant Data Corp.
    -------------------------------------------------------------------------------
    --Add a delimiter to the end of the string so we dont lose last value
    string_tokens := text_to_parse || text_delimiter;

    --Initialize the collection
    list_values := varchar_table() ;

    --get length of string
    string_length := length(string_tokens);

    --set start and end for first token
    start_pos := 1;
    last_pos   := instr(string_tokens,text_delimiter,1,1);
    counter := 1;

    LOOP
	--Get substring
	token_value := substr(string_tokens, start_pos, last_pos - start_pos);
	--add values to collection
	list_values.EXTEND;
	list_values(list_Values.LAST):= token_value;

	--Check to see if we are done
	IF last_pos = string_length THEN
	    EXIT;
	ELSE
	    -- Increment Start Pos and End Pos
	    start_pos := last_pos + 1;
	    --increment counter
	    counter := counter + 1;
	    last_pos := instr(string_tokens, text_delimiter,1, counter);

	END IF;
    END LOOP;

    return list_values;

END text_parser;
/
 
