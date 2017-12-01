	------------------------------------- BEGIN CNT-33801 ------------------------------------
DO $BODY$
BEGIN
  EXECUTE DROP_INDEX('IDX_CPM_MILESTONE_CPM_CODE');
END $BODY$ LANGUAGE PLPGSQL;

DO $BODY$
DECLARE
  CREATE_SQL VARCHAR(1000);
     BEGIN
        CREATE_SQL := 'create index IDX_CPM_MILESTONE_CPM_CODE on CNT_CPM_MILESTONE(MILESTONE_CODE);';
        EXECUTE  CREATE_SQL;
     EXCEPTION
        WHEN OTHERS THEN
        RAISE NOTICE 'Fail to insert index IDX_CPM_MILESTONE_CPM_CODE';
 END $BODY$ LANGUAGE PLPGSQL;
------------------------------------- BEGIN CNT-33801 ------------------------------------