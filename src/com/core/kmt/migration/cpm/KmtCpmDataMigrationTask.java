// Copyright (c) 1998-2017 Core Solutions Limited. All rights reserved.
// ============================================================================
// CURRENT VERSION KMT.6.4.2P.01.0
// ============================================================================
// CHANGE LOG
// KMT.6.4.2P.01.0 : 2017-06-30, hanson.ou, KME-1286
// KMT.6.4.1P.01.0 : 2017-06-02, hanson.ou, KME-1128
// KMT.6.2.0P.01.0 : 2017-03-13, hanson.ou, KME-416
// KMT.6.2.0P.01.0 : 2017-03-03, andy.li, created
// ============================================================================
package com.core.kmt.migration.cpm;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.cbx.ws.rest.client.cpm.RestDispatcher;
import com.cbx.ws.rest.client.cpm.RestDispatcherFactory;
import com.cbx.ws.rest.jaxrs.exception.RestAPIException;
import com.core.cbx.action.actionContext.InitializeCpm;
import com.core.cbx.action.exception.ActionException;
import com.core.cbx.command.handler.LocalTransactionCommand;
import com.core.cbx.common.logging.CNTLogger;
import com.core.cbx.common.logging.LogFactory;
import com.core.cbx.cpm.exception.CpmActionException;
import com.core.cbx.data.DynamicEntityCacheManager;
import com.core.cbx.data.DynamicEntityModel;
import com.core.cbx.data.cache.GenericCacheManager;
import com.core.cbx.data.codelist.service.CodelistManager;
import com.core.cbx.data.constants.Cpm;
import com.core.cbx.data.constants.CpmTask;
import com.core.cbx.data.constants.CpmTempl;
import com.core.cbx.data.constants.ExceptionConstants;
import com.core.cbx.data.constants.Vpo;
import com.core.cbx.data.def.EntityDefManager;
import com.core.cbx.data.entity.DynamicEntity;
import com.core.cbx.data.entity.EntityConstants;
import com.core.cbx.data.exception.DataException;
import com.core.cbx.data.search.Criterion;
import com.core.cbx.data.search.Restriction;
import com.core.cbx.database.DaoFactory;
import com.core.cbx.security.AuthenticationUtil;
import com.core.cbx.security.acl.AuthorizationUtil;
import com.core.cbx.startup.BaseStartUp;
import com.core.cbx.transaction.TransactionManager;
import com.core.cbx.transaction.TransactionUnit;
import com.core.cbx.util.CpmUtil;

public class KmtCpmDataMigrationTask implements LocalTransactionCommand {
    private static final CNTLogger LOGGER = LogFactory.getLogger(KmtCpmDataMigrationTask.class);

    private static final String migration_user = "migration@KMT";
    private static final String migration_user_password = "cbx@2016";


    private static RestDispatcher restClient;

    private static Map<String, DynamicEntity> vpoMap = new HashMap<String, DynamicEntity>();
    private static Map<String, DynamicEntity> localCpmTemplateMap = new HashMap<String, DynamicEntity>();
    private static Map<String, DynamicEntity> localCpmTaskStatusMap = new HashMap<String, DynamicEntity>();

    public static void main(final String[] args) throws Exception {
        try {

            final String inputBatchNo;
            final String inputRowLimit;
            if (args.length < 2) {
                LOGGER.info("00000000000000000  args length is less then 2, should be: [batchNo] [rowLimit]");
                System.exit(-1);
                return;
            }

            inputBatchNo = args[0];
            inputRowLimit = args[1];

            init();

            LOGGER.info("#################### KmtCpmDataMigrationTask is initiated");

            final List<DynamicEntity> cpmInitials = loadCpmInitial(inputBatchNo, inputRowLimit);

            for (final DynamicEntity cpmInitial : cpmInitials) {
                TransactionManager.runInTransaction(new TransactionUnit() {
                    @Override
                    public void run() throws Exception {
                        DynamicEntity cpmDoc = null;
                        boolean updateFlag = false;

                        cpmDoc = initialCpm(cpmInitial);
                        if (cpmDoc != null) {
                            updateFlag = updateCpmTask(cpmInitial, cpmDoc);
                        }
                        if (cpmDoc != null && updateFlag) {
                            updateMigrationFlag(cpmInitial, "DONE");
                        } else {
                            updateMigrationFlag(cpmInitial, "FAIL");
                        }
                    }
                });
                ;

            }

        } catch (final Throwable e) {
            LOGGER.fatal("Exception", "Error in KmtCpmDataMigrationTask", e);
            AuthenticationUtil.logout();
            System.exit(-1);
        } finally {
            LOGGER.info("######################## KmtCpmDataMigrationTask END!");
            AuthenticationUtil.logout();
            System.exit(-1);
        }
    }

    private static void init() throws Exception {
        final String isDisableDistributedCache = System
                .getProperty(GenericCacheManager.SYS_PROP_DISABLE_DISTRIBUTED_CACHE);
        if (StringUtils.isEmpty(isDisableDistributedCache)) {
            // Disable distributed cache by default for backend jobs
            System.setProperty(GenericCacheManager.SYS_PROP_DISABLE_DISTRIBUTED_CACHE, String.valueOf(true));
        }
        final String isDisableEntityCache = System.getProperty(DynamicEntityCacheManager.SYS_PROP_DISABLE_ENTITY_CACHE);
        if (StringUtils.isEmpty(isDisableEntityCache)) {
            // Disable entity cache by default for backend jobs
            System.setProperty(DynamicEntityCacheManager.SYS_PROP_DISABLE_ENTITY_CACHE, String.valueOf(true));
        }

        final String isBackendProcess = System.getProperty(AuthorizationUtil.SYS_PROP_BACKEND_PROCESS);
        if (StringUtils.isEmpty(isBackendProcess)) {
            System.setProperty(AuthorizationUtil.SYS_PROP_BACKEND_PROCESS, String.valueOf(true));
        }

        BaseStartUp.run();

        try {
            new ClassPathXmlApplicationContext("classpath*:spring/*.xml");
        } catch (final Exception e) {
            LOGGER.fatal("Init Spring Context", "Error in lauching job.0", e);
            System.exit(-1);
        }

        AuthenticationUtil.login(migration_user, migration_user_password);
        restClient = RestDispatcherFactory.createDefaultClient();

    }

    private static void updateMigrationFlag(final DynamicEntity cpmInitial, final String flag) throws DataException {
        final String cpmInitialId = cpmInitial.getId();
        final String batchNo = (String) cpmInitial.get(MigCpmInitial.BATCH_NO);
        final String refNo = (String) cpmInitial.get(MigCpmInitial.REF_NO);
        final String parentRefNo = (String) cpmInitial.get(MigCpmInitial.PARENT_REF_NO);

        final Map<String, Object> params1 = new HashMap<String, Object>();
        params1.put("cpmInitialId", cpmInitialId);
        params1.put("flag", flag);
        DynamicEntityModel.execUpdateQuery(
                "com.core.kmt.migration.data.mapper.MigrationMapper.updateMigCpmInitialFlag", params1);

        final Map<String, Object> params2 = new HashMap<String, Object>();
        params2.put("batchNo", batchNo);
        params2.put("refNo", refNo);
        params2.put("parentRefNo", parentRefNo);
        params2.put("flag", flag);
        DynamicEntityModel.execUpdateQuery(
                "com.core.kmt.migration.data.mapper.MigrationMapper.updateMigCpmTaskValueFlag", params2);

        LOGGER.info("######################## updateMigrationFlag DONE!");
    }

    private static List<DynamicEntity> loadCpmInitial(final String inputBatchNo, final String inputRowLimit)
            throws DataException {
        final Map<String, Object> params1 = new HashMap<String, Object>();
        params1.put(MigCpmInitial.BATCH_NO, inputBatchNo);
        params1.put(MigCpmInitial.MIGRATION_FLAG, StringUtils.EMPTY);
        params1.put("orderBy", "ci.MODULE, ci.REF_NO ASC");

        Connection conn = null;
        SqlSession session = null;
        conn = DaoFactory.getInstance().getConnection();
        session = DaoFactory.getSession(conn);

        final List<DynamicEntity> entities = session.selectList(
                "com.core.kmt.migration.data.mapper.MigrationMapper.listCpmInitial", params1,
                new RowBounds(0, Integer.valueOf(inputRowLimit)));

        LOGGER.info("1111111111111111 entities size : " + entities.size());
        return entities;
    }

    protected static DynamicEntity loadCpm(final String cpmId) {
        DynamicEntity cpm = null;
        try {
            cpm = restClient.getCpm(cpmId);
        } catch (final RestAPIException e) {
            LOGGER.fatal("Exception", "Error in KmtCpmDataMigrationTask.loadCpm()", e);
            e.printStackTrace();
        }
        return cpm;
    }

    private static DynamicEntity initialCpm(final DynamicEntity cpmInitial) throws DataException, RestAPIException,
            ActionException {
        Boolean isCpmInitialized = false;
        DynamicEntity cpmDoc = null;
        DynamicEntity currectCpmDoc = null;
        String refNo = null;
        DynamicEntity doc = null;
        try {
            final String module = (String) cpmInitial.get(MigCpmInitial.MODULE);
            if (StringUtils.equals(Vpo.ENTITY_NAME_VPO, module)) {
                refNo = (String) cpmInitial.get(MigCpmInitial.PARENT_REF_NO);
                if (vpoMap.get(refNo) != null) {
                    doc = vpoMap.get(refNo);
                } else {
                    doc = DynamicEntityModel.getLatestEntityByRefNo(module, refNo, true);
                }
                vpoMap.put(refNo, doc);
            } else {
                refNo = (String) cpmInitial.get(MigCpmInitial.REF_NO);
                doc = DynamicEntityModel.getLatestEntityByRefNo(module, refNo, true);
            }
            final String cpmName = (String) cpmInitial.get(MigCpmInitial.CPM_NAME);
            final DynamicEntity cpmTempl = findCpmTemplateByName(cpmName);
            if (cpmTempl == null) {
                LOGGER.error("Fail to initial Cpm", "Cannot load back the cpmTemplate by name: " + cpmName);
                return cpmDoc;
            }

            if ((doc.getBoolean("isCpmInitialized") == true)) {
                isCpmInitialized = true;
            }

            if (isCpmInitialized) {
                cpmDoc = loadCpmDoc(module, cpmInitial, cpmTempl, refNo, doc);
                if (cpmDoc != null) {
                    return cpmDoc;
                }
            }

            final InitializeCpm actionContext = new InitializeCpm(doc, cpmTempl);

            final List<DynamicEntity> applyEntities = CpmUtil.validateApplyEntities(actionContext);
            if (CollectionUtils.isEmpty(applyEntities)) {
                LOGGER.error("Fail to initial Cpm", "applyEntities is empty with actionContext: " + actionContext);
                return cpmDoc;
            }
            for (final DynamicEntity entity : applyEntities) {
                try {
                    cpmDoc = CpmUtil.createCpmDoc(cpmTempl, doc, entity);
                } catch (final CpmActionException cpmEx) {
                    LOGGER.error("Fail to initial Cpm", "CpmUtil.createCpmDoc failed! witn cpmName:" + cpmName
                            + ", doc:" + refNo + ", apply entity:" + entity);
                    cpmEx.printStackTrace();
                    return cpmDoc;
                }
                final String cpmId = restClient.initCpm(cpmDoc);
                cpmDoc.put(Cpm.ID, cpmId);
                cpmDoc.put(Cpm.CPM_ID, cpmId);
                buildCpmCache(doc, cpmDoc, cpmId);

                if (StringUtils.equals(Vpo.ENTITY_NAME_VPO_SHIP_DTL, entity.getEntityName())) {
                    if (StringUtils.equals((String) cpmInitial.get(MigCpmInitial.REF_NO), entity.getReference())) {
                        if (StringUtils.equals(cpmDoc.getString(Cpm.REF_DOC_REF_NO),
                                entity.getString(EntityConstants.PTY_DOCUMENT_UNIQUE_NO))) {
                            currectCpmDoc = cpmDoc;
                        }
                    }
                } else {
                    currectCpmDoc = cpmDoc;
                }
            }
            doc.put(EntityConstants.PTY_IS_CPM_INITIALIZED, Boolean.TRUE);
            DynamicEntityModel.saveMetaData(doc);
            // ExternalAccessModel.sendIsCpmInitializedToExternal(doc);
        } catch (final Exception e) {
            LOGGER.error("Cannot initial CPM template!!!", e.getMessage());
        }
        LOGGER.info("################## initialCpm successfully! with cpmInitial :" + cpmInitial);
        return currectCpmDoc;
    }

    private static DynamicEntity loadCpmDoc(final String module, final DynamicEntity cpmInitial,
            final DynamicEntity cpmTempl, final String refNo, final DynamicEntity doc) throws ActionException {
        DynamicEntity cpmDoc = null;
        final String fieldPath = ObjectUtils.toString(cpmTempl.get("applyLevel"));
        final List<DynamicEntity> applyEntities = CpmUtil.getCpmCacheDocs("vpo", refNo, fieldPath);
        final Collection<DynamicEntity> vpoShipDtls = doc.getEntityCollection(Vpo.VPO_SHIP_DTL);
        for (final DynamicEntity entity : applyEntities) {
            for (final DynamicEntity vpoShipDtl : vpoShipDtls) {
                if (StringUtils.equals((String) cpmInitial.get(MigCpmInitial.REF_NO), vpoShipDtl.getReference())) {
                    if (StringUtils.equals(entity.getString(Cpm.REF_DOC_REF_NO),
                            vpoShipDtl.getString(EntityConstants.PTY_DOCUMENT_UNIQUE_NO))) {
                        cpmDoc = entity;
                        return cpmDoc;
                    }
                }
            }
        }
        return cpmDoc;
    }

    private static void buildCpmCache(final DynamicEntity doc, final DynamicEntity cpmDoc, final String cpmId)
            throws ActionException, RestAPIException {
        try {
            CpmUtil.buildCpmCache(cpmDoc);
            CpmUtil.initializeCpmAssignee(cpmDoc, doc);
        } catch (final DataException e) {
            restClient.deleteCpm(cpmId);
            throw new ActionException(ExceptionConstants.DATA_EXCEPTION_000001, e);
        }
    }

    private static DynamicEntity findCpmTemplateByName(final String cpmTemplateName) throws DataException {
        DynamicEntity cpmTempl = null;
        if (localCpmTemplateMap.get(cpmTemplateName) != null) {
            cpmTempl = localCpmTemplateMap.get(cpmTemplateName);
            return cpmTempl;
        }
        final Criterion criterion = new Criterion(CpmTempl.ENTITY_NAME_CPM_TEMPL);
        criterion.addRestriction(Restriction.eq(CpmTempl.NAME, cpmTemplateName));
        criterion.addRestriction(Restriction.eq(CpmTempl.IS_LATEST, true));
        criterion.addRestriction(Restriction.eq(CpmTempl.DOC_STATUS, "active"));
        cpmTempl = DynamicEntityModel.findUniqueBy(criterion, true);
        if (cpmTempl == null) {
            LOGGER.error("Cannot find match CPM template!!", "Template name: " + cpmTemplateName);
        } else {
            localCpmTemplateMap.put(cpmTemplateName, cpmTempl);
        }
        return cpmTempl;
    }

    private static boolean updateCpmTask(final DynamicEntity cpmInitial, final DynamicEntity cpmDoc)
            throws DataException, RestAPIException {
        final List<DynamicEntity> cpms = new ArrayList<DynamicEntity>();
        boolean updateSuccess = false;
        final String batchNo = (String) cpmInitial.get(MigCpmInitial.BATCH_NO);
        final String refNo = (String) cpmInitial.get(MigCpmInitial.REF_NO);
        final String parentRefNo = (String) cpmInitial.get(MigCpmInitial.PARENT_REF_NO);
        final String module = (String) cpmInitial.get(MigCpmInitial.MODULE);
        final Boolean isInactive = (Boolean)cpmInitial.get(MigCpmInitial.INACTIVE);
        final Map<String, Object> params2 = new HashMap<String, Object>();
        params2.put(MigCpmTaskValue.BATCH_NO, batchNo);
        params2.put(MigCpmTaskValue.MODULE, module);
        params2.put(MigCpmTaskValue.REF_NO, refNo);
        final List<DynamicEntity> cpmTaskValues = DynamicEntityModel.findByQuery(
                "com.core.kmt.migration.data.mapper.MigrationMapper.listCpmTaskValue", params2);

        LOGGER.info("22222222222222222 cpmTaskValues: " + cpmTaskValues);

        // final Map<String, Object> params3 = new HashMap<String, Object>();
        // params3.put(Cpm.DOC_REF_NO, refNo);
        // params3.put(Cpm.REF_DOC_REF_NO, parentRefNo);
        //
        // final List<DynamicEntity> cpmDoc = DynamicEntityModel.findByQuery(
        // "com.core.kmt.migration.data.mapper.MigrationMapper.findCpmIdByDocRefNo", params3);

        // LOGGER.info("333333333333333333 cpmDoc: " + cpmDoc);
        // check whether cpmDoc has CpmTasks, if yes, no need to call REST API to load back the cpm Object. -> no
        // CpmTasks, need to do this part

        final String cpmId = (String) cpmDoc.get(Cpm.CPM_ID);
        final DynamicEntity cpm = loadCpm(cpmId);
        LOGGER.info("444444444444444444 loadCpm by REST API: ");

        if (cpm == null) {
            LOGGER.error("Failed to update the Cpm Tasks!", "Cannot find the CPM with cpmId: " + cpmId);
            return updateSuccess;
        }

        final Collection<DynamicEntity> cpmTasks = cpm.getEntityCollection(Cpm.CPM_TASKS);

        for (final DynamicEntity cpmTaskValue : cpmTaskValues) {
            final String taskName = (String) cpmTaskValue.get(MigCpmTaskValue.TASK_NAME);
            for (final DynamicEntity cpmTask : cpmTasks) {
                final String taskTaskTaskName = cpmTask.getString(CpmTask.TASK_NAME);
                if (StringUtils.equals(taskTaskTaskName, taskName)) {
                    LOGGER.info("55555555555555555555555 Found Match CpmTask!" + cpmTask);

                    final Object actStartDate = cpmTaskValue.get(MigCpmTaskValue.ACT_START_DATE);
                    final Object actEndDate = cpmTaskValue.get(MigCpmTaskValue.ACT_END_DATE);
                    final String description = (String) cpmTaskValue.get(MigCpmTaskValue.DESCRIPTION);
                    final String comment = (String) cpmTaskValue.get(MigCpmTaskValue.COMMENT);
                    final String statusCode = (String) cpmTaskValue.get(MigCpmTaskValue.STATUS);
                    DynamicEntity statusCodelist = null;

                    if (localCpmTaskStatusMap.get(statusCode) != null) {
                        statusCodelist = localCpmTaskStatusMap.get(statusCode);
                    } else {
                        statusCodelist = CodelistManager.loadCodelistItem("CPM_TASK_STATUS",
                                AuthenticationUtil.getUserCurrentDomainId(), StringUtils.lowerCase(statusCode));
                        localCpmTaskStatusMap.put(statusCode, statusCodelist);
                    }

                    if (actStartDate != null) {
                        cpmTask.put(CpmTask.ACTUAL_START, actStartDate);
                    }
                    if (actEndDate != null) {
                        cpmTask.put(CpmTask.ACTUAL_END, actEndDate);
                    }
                    if (statusCodelist != null) {
                        cpmTask.put(CpmTask.STATUS, statusCodelist);
                    }
                    if (StringUtils.isNotEmpty(description)) {
                        cpmTask.put(CpmTask.DESCRIPTION, description);
                    }
                    if (StringUtils.isNotEmpty(comment)) {
                        cpmTask.put(CpmTask.REASON_DESCRIPTION, comment);
                    }
                    CpmUtil.setColorCode(cpmTask); // -> yes, need to call setColorCode to refresh color

                }
            }
        }
        cpms.add(cpm);

        restClient.updateCpm(cpms);

        LOGGER.info("666666666666666666666666 Update Cpm by REST API: restClient.updateCpm(cpms). ");

        if (isInactive !=null && isInactive) {
            restClient.inactive(cpmId);
            LOGGER.info("777777777777777777777777 Update Cpm by REST API: restClient.inactive(cpmId). ");
        }

        updateSuccess = true;
        return updateSuccess;
    }

    private static boolean isExistingCpmDoc(final DynamicEntity vpo) throws DataException {
        Boolean needToReInitialized = false;
        final String docRefNo = vpo.getReference();
        final String entityName = vpo.getEntityName();
        final Collection<DynamicEntity> shimentItems = vpo.getEntityCollection(Vpo.VPO_SHIP_DTL);
        final String module = EntityDefManager.getModuleCodeByEntityName(entityName);
        final Criterion criterion = new Criterion(Cpm.ENTITY_NAME_CPM_DOC);
        criterion.addRestriction(Restriction.eq(Cpm.MODULE, module));
        criterion.addRestriction(Restriction.eq(Cpm.HUB_DOMAIN_ID, AuthenticationUtil.getUserWorkingDomainId()));
        criterion.addRestriction(Restriction.eq(Cpm.DOC_REF_NO, docRefNo));
        criterion.addRestriction(Restriction.eq(Cpm.IS_LATEST, true));
        criterion.addRestriction(Restriction.eq(Cpm.IS_DELETED, Boolean.FALSE));
        final List<DynamicEntity> cpmDocs = DynamicEntityModel.findEntities(criterion, false);

        if (shimentItems.size() != cpmDocs.size()) {
            needToReInitialized = true;
        }
        return needToReInitialized;
    }
}
