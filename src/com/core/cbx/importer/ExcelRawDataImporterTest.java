// Copyright (c) 1998-2013 Core Solutions Limited. All rights reserved.
// ============================================================================
// CURRENT VERSION CNT.5.0.1
// ============================================================================
// CHANGE LOG
// CNT.5.0.61 : 2013-10-25, lorin.liu, creation, CNT-11188
// ============================================================================

package com.core.cbx.importer;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cbx.constants.GlobeConstants;
import com.cbx.test.BaseTest;
import com.core.cbx.action.ActionDispatcher;
import com.core.cbx.action.actionContext.AsyncImportRawData;
import com.core.cbx.security.AuthenticationUtil;

/**
 * @author lorin.liu
 */
public class ExcelRawDataImporterTest extends BaseTest {
    private static final Log log = LogFactory.getLog(ExcelRawDataImporterTest.class);


    /**
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        try {
            login();
            if (StringUtils.equalsIgnoreCase(getValueFromConfig(GlobeConstants.IMPORT_IS_READ_FOLDER, "true"), "true")) {
                importReadFolder();
            } else {
                final File importFile = new File(getValueFromConfig(GlobeConstants.IMPORT_FILE_PATH));
                final String entityName = getValueFromConfig(GlobeConstants.IMPORT_ENTITY_NAME);
                final int count = 1;
                for (int i = 0; i < count; i++) {
                    final AsyncImportRawData impor1 = new AsyncImportRawData(importFile, "Excel", entityName, null);
                    ActionDispatcher.execute(impor1);
                }
            }
        } catch (final Throwable e) {
            log.info("Generation End ! ", e);
        } finally {
            AuthenticationUtil.logout();
            log.info("Generation End ! ");
            System.exit(-1);
        }
    }


    /**
     *
     */
    private static void importReadFolder() throws Exception{
        boolean isDeleted = Boolean.TRUE;
        while(true) {
            Thread.sleep(10000);
            final File importFile1 = readImportFile();
            if (importFile1 == null) {
                log.info("sleep ========================= ");
                continue;
            }
            final String fileName = importFile1.getName();
            log.info("fileName ===== " + fileName);
            try {
                if (isDeleted) {
                    final String entityName = getValueFromConfig(GlobeConstants.IMPORT_ENTITY_NAME);
                    final int count = 1;
                    for (int i = 0; i < count; i++) {
                        final AsyncImportRawData impor1 = new AsyncImportRawData(importFile1, "Excel", entityName, null);
                        ActionDispatcher.execute(impor1);
                    }
                }
            } catch (final Exception e) {
                log.error("error : ", e);
            } finally {
                Thread.sleep(10000);
                isDeleted = removeFile(fileName);
                if (!isDeleted) {
                    Thread.sleep(10000);
                    log.info("Fail to remove fileName ===== " + fileName);
                    log.info("Please remove it by hand fileName ===== " + fileName);
                }
            }
        }
    }


    /**
     * @param fileName
     * @throws InterruptedException
     */
    private static boolean removeFile(final String fileName) {
        final File file = new File(getValueFromConfig(GlobeConstants.IMPORT_READ_FOLDER) + "/" + fileName);
        log.info("remove fileName ===== " + file.getAbsolutePath());
        if (file.exists()) {
            return FileUtils.deleteQuietly(file);
        }
        return false;
    }


    /**
     * @return
     */
    private static File readImportFile() {
        final File folder = new File(getValueFromConfig(GlobeConstants.IMPORT_READ_FOLDER));
        if (!folder.exists()) {
            folder.mkdirs();
        }
        final File[] files = folder.listFiles();
        if (files.length == 0) {
            return null;
        }
        final File file = files[0];
        return file;
    }
}
