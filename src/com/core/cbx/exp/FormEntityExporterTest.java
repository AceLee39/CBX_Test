// Copyright (c) 1998-2013 Core Solutions Limited. All rights reserved.
// ============================================================================
// CURRENT VERSION CNT.5.0.1
// ============================================================================
// CHANGE LOG
// CNT.5.0.61 : 2013-10-25, lorin.liu, creation, CNT-11188
// ============================================================================

package com.core.cbx.exp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cbx.constants.GlobeConstants;
import com.cbx.test.BaseTest;
import com.core.cbx.common.type.DateTime;
import com.core.cbx.data.DynamicEntityModel;
import com.core.cbx.data.entity.DynamicEntity;
import com.core.cbx.data.entity.EntityConstants;
import com.core.cbx.data.exception.DataException;

/**
 * @author lorin.liu
 *
 */
public class FormEntityExporterTest extends BaseTest {

    private static final Log log = LogFactory.getLog(FormEntityExporterTest.class);

    public static void main(final String[] args) {
        final long begin = System.currentTimeMillis();

        try{
            login();
            final String entityName = "Vq";
            exportTemplateCreator(entityName);
            //exportTemplateCreatorAll();
            final String refNo = "QTE1705-000002";//QQ1705-000046//VPO1705-000019//ITM1705-000010
            final int size = 1;
            final String refNoKey = "qqNo";
            //exportEntity(entityName, refNo, size, 11, refNoKey);
            exportEntity(entityName, refNo);
        }catch (final Exception e) {
            log.info("Error ", e);
            e.printStackTrace();
        }finally {
            final long end = System.currentTimeMillis();
            log.info("Generation End ! " + (end-begin));
            System.exit(-1);
        }
    }

    private static void exportTemplateCreator(final String entityName) throws DataException {
        final FormTemplateCreator te = new FormTemplateCreator();
        te.exportFormTemplate(new String[]{"/", entityName, "en_US",
                "C:\\Users\\ace.li\\CBX\\WorkSpaces\\CBXB\\conf\\form_export_template.xlsx",
                "C:\\Users\\ace.li\\CBX\\Temp\\" + entityName,
                "0", "1", "2", "1", "2"});
    }

    private static void exportTemplateCreatorAll() throws DataException {
        final FormTemplateCreator te = new FormTemplateCreator();
        te.exportFormTemplate(new String[]{"/", "ALL", "en_US",
                "C:\\Users\\ace.li\\CBX\\WorkSpaces\\CBXB\\conf\\form_export_template.xlsx",
                "C:\\Users\\ace.li\\CBX\\Temp\\All",
                "0", "1", "2", "1", "2"});
    }


    private static void exportEntity(final String entityName, final String refNo) throws DataException {
        final DynamicEntity entity = DynamicEntityModel.getLatestEntityByRefNo(entityName, refNo, Boolean.TRUE);
        final String filePath = getValueFromConfig(GlobeConstants.FILE_PATH);
        final File targetFile = new File(filePath + entityName
                + EntityConstants.SEPARATOR_SLASH + refNo
                + EntityConstants.SEPARATOR_UNDERSCORE + DateTime.now().getMilliseconds(TimeZone.getDefault())
                + ".xlsx");
        final File templateFile = new File(filePath + "/" + entityName + "/"
                + entityName + " Import Raw Data Template.xlsx");
        final FormEntityExporter fee = new FormEntityExporter();
        fee.exportEntity(entity, templateFile, targetFile, null, "/");
    }

    /**
     * @throws DataException
     *
     */
    private static void exportItem() throws DataException {
        final String entityName = "Item";
        final String refNo = "ITM1701-000004";
        exportEntity(entityName, refNo);
    }

    private static void exportEntity(final String entityName, final String refNo, final int size, final String refNoKey) throws DataException {
        exportEntity(entityName, refNo, size, 1, refNoKey);
    }


    private static void exportEntity(final String entityName, final String refNo,
            final int size, final int start, final String refNoKey) throws DataException {

        final DynamicEntity itemEntity = DynamicEntityModel.getLatestEntityByRefNo(entityName, refNo, true);

        final List<DynamicEntity> list = new ArrayList<DynamicEntity>(size);
        final String refNoPrefix = StringUtils.substring(refNo, 0, refNo.length() - 3);
        for (int i = start; i < size + start; i++) {
            final DynamicEntity copy =  DynamicEntityModel.copyForHistory(itemEntity);
            //copy.put("id", UUIDGenerator.getUUID());
            //copy.setNewEnity();
            final String newRefNo = buildRefNo(i, refNoPrefix);
            copy.put("refNo", newRefNo);
            copy.put(refNoKey, newRefNo);
            copy.put("businessRefNo", newRefNo);
            list.add(copy);
        }

        final FormEntityExporter fee = new FormEntityExporter();
        final String filePath = getValueFromConfig(GlobeConstants.FILE_PATH);
        final File targetFile = new File(filePath + entityName
                + EntityConstants.SEPARATOR_SLASH + refNo
                + EntityConstants.SEPARATOR_UNDERSCORE + DateTime.now().getMilliseconds(TimeZone.getDefault())
                + ".xlsx");
        final File templateFile = new File(filePath + entityName + " Import Raw Data Template.xlsx");
        fee.exportEntity(list, templateFile, targetFile, null, "/");
    }
    /**
     * @throws DataException
     *
     */
    private static void genCustExcel() throws DataException {
        final String entityName = "Cust"; //Item"; // "Vpo";//

        final DynamicEntity itemEntity =     DynamicEntityModel.getFullEntity(entityName,
                "55bc4405e6a34be4be05b0e650d594b3");//1720cb57373e493d98180798520ea0a5"); //50d4a2ea9cf94c9ea89073659846fd57"); //"");// );

        final int size = 495;

        final List<DynamicEntity> list = new ArrayList<DynamicEntity>(size);
        final List<DynamicEntity> list2 = new ArrayList<DynamicEntity>(245);
        for (int i = 6; i < size + 6; i++) {
            final DynamicEntity copy =  DynamicEntityModel.copyForHistory(itemEntity);
            //copy.put("id", UUIDGenerator.getUUID());
            //copy.setNewEnity();
            final String refNo = buildRefNo(i, "C000");
            copy.put("refNo", refNo);
            copy.put("custCode", refNo);
            copy.put("businessRefNo", refNo);
            final String businessName = buildBusinessName(i, "Cust000");
            copy.put("businessName", businessName);

            if (i>256) {
                list2.add(copy);
            } else {
                list.add(copy);
            }

        }

        final File targetFile = new File("C:/Users/ace.li/CBX/"+entityName+"_Entity_Output250.xlsx");

        final FormEntityExporter fee = new FormEntityExporter();


        fee.exportEntity(list, targetFile, null, "/");

        final File targetFile2 = new File("C:/Users/ace.li/CBX/"+entityName+"_Entity_Output245.xlsx");
        fee.exportEntity(list2, targetFile2, null, "/");
    }
    /**
     * @param i
     * @return
     */
    private static String buildBusinessName(final int i, final String businessName) {
        if (i<10) {
            return businessName + "00" + i;
        } else if (i<100) {
            return businessName + "0" + i;
        } else {
            return businessName + i;
        }
    }
    /**
     * @param i
     * @return
     */
    private static String buildRefNo(final int i, final String refNo) {
        if (i<10) {
            return refNo + "00" + i;
        } else if (i<100) {
            return refNo + "0" + i;
        } else {
            return refNo + i;
        }
    }

}
