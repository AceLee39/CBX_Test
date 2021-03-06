
CREATE OR REPLACE PROCEDURE sp_create_domain (
--This procedure will help you to create a domain under the parent domain
--In parameters: domain id and parent domain id
   in_parent_domin_id varchar2,
   in_domain_id varchar2
)
AS
    v_domain_name varchar2(200) := in_parent_domin_id || in_domain_id;
BEGIN

    Insert into CNT_DOMAIN (
      ID, REVISION, ENTITY_VERSION, PARENT_ID, DESCRIPTION,
      PARENT_PATH, STATUS, DOC_STATUS,
      CREATE_USER, CREATED_ON, DOMAIN_ID, HUB_DOMAIN_ID,LEVELS, NAME, REF_NO,IS_FOR_REFERENCE
    ) SELECT IN_DOMAIN_ID, 0, 1, IN_PARENT_DOMIN_ID, IN_DOMAIN_ID,
      IN_PARENT_DOMIN_ID, 'enabled', 'active',
      'system', SYSDATE, IN_PARENT_DOMIN_ID,IN_PARENT_DOMIN_ID, 2, v_domain_name,IN_DOMAIN_ID,'0'
    FROM dual
    WHERE NOT EXISTS (
      SELECT 1 FROM cnt_domain WHERE id = in_domain_id
    );

    INSERT INTO CNT_USER_ACS_RULE (ID, REVISION, ENTITY_VERSION,DOMAIN_ID,
    HUB_DOMAIN_ID, IS_FOR_REFERENCE,VERSION, DOC_STATUS, CREATE_USER,
    update_user,CREATED_ON, UPDATED_ON,is_cpm_initialized,
    TYPE,MEMBER_ID, MEMBER_TYPE,IP, REF_NO)
    SELECT SYS_GUID(),0,1,IN_DOMAIN_ID,IN_DOMAIN_ID,'0',0,'active','system','system',
    SYSDATE,SYSDATE,'0','1','$ALL$','0','ANY','1_$ALL$_0'
    FROM DUAL
    WHERE NOT EXISTS (
    SELECT 1 FROM CNT_USER_ACS_RULE WHERE DOMAIN_ID = IN_DOMAIN_ID
    );

    INSERT INTO CNT_COMPANY_PROFILE (ID,REVISION,ENTITY_VERSION,DOMAIN_ID,HUB_DOMAIN_ID,IS_FOR_REFERENCE,VERSION,DOC_STATUS,EDITING_STATUS,IS_CPM_INITIALIZED,IS_LATEST,CREATE_USER,CREATE_USER_NAME,UPDATE_USER,UPDATE_USER_NAME,CREATED_ON,UPDATED_ON,COMPANY_CODE,REF_NO,BUSINESS_REF_NO,BUSINESS_NAME,SHORT_NAME,COUNTRY,COUNTRY_NAME,COUNTRY_VER,ADDRESS1,FIRST_NAME,EMAIL)
        VALUES (SYS_GUID(),1,1,IN_DOMAIN_ID,IN_DOMAIN_ID,0,0,'active','draft',0,1,'system','system','system','system',SYSDATE,SYSDATE,
          'TCP' || to_char(sysdate,'YY') || '-00000'|| to_char((select count(1)+1 from CNT_COMPANY_PROFILE)),
          'TCP' || to_char(sysdate,'YY') || '-00000'|| to_char((select count(1)+1 from CNT_COMPANY_PROFILE)),
          'TCP' || to_char(sysdate,'YY') || '-00000'|| to_char((select count(1)+1 from CNT_COMPANY_PROFILE)),IN_DOMAIN_ID,null,null,null,null,null,null,null);

    INSERT INTO CTM_COMPANY_PROFILE (ID, DOMAIN_ID, REF_ENTITY_NAME) values (
    (SELECT ID FROM CNT_COMPANY_PROFILE WHERE DOMAIN_ID=IN_DOMAIN_ID AND IS_LATEST='1'),
    IN_DOMAIN_ID, 'CompanyProfile');

EXCEPTION
   WHEN OTHERS
   THEN
   RAISE_APPLICATION_ERROR(-20001, SQLERRM);
END;
/




CALL SP_CREATE_DOMAIN('/','ACEVENDOR');
CALL SP_CREATE_NEW_DOMAIN_CONFIG('RD1','ACEVENDOR');
Call SP_NEW_DOMAIN_DEFAULT_CONFIGS('RD1','ACEVENDOR');
CALL SP_CREATE_NEW_DOMAIN_USER('ACEVENDOR','ADMIN@ACEVENDOR');

--p@ssw0rd#Cbx!
