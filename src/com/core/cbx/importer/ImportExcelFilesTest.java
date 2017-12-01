// Copyright (c) 1998-2013 Core Solutions Limited. All rights reserved.
// ============================================================================
// CURRENT VERSION CNT.5.0.1
// ============================================================================
// CHANGE LOG
// CNT.5.0.61 : 2013-10-25, lorin.liu, creation, CNT-11188
// ============================================================================

package com.core.cbx.importer;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cbx.constants.GlobeConstants;
import com.cbx.test.BaseTest;
import com.core.cbx.action.ActionDispatcher;
import com.core.cbx.action.actionContext.ImportRawData;

/**
 * @author lorin.liu
 */
public class ImportExcelFilesTest extends BaseTest{

    private static final Log log = LogFactory.getLog(ImportExcelFilesTest.class);

    /**
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        try {
            login();
            final File importFile = new File(getValueFromConfig(GlobeConstants.IMPORT_FILE_PATH));
            for (int i = 0; i < 12; i++) {
                final ImportRawData ctx = new ImportRawData(importFile, "EXCEL", importFile.getName());
                ActionDispatcher.execute(ctx);
            }
        } catch (final Exception e) {
            log.info("", e);
            e.printStackTrace();
        } finally {
            log.info("Generation End ! ");
            System.exit(-1);
        }
    }

}
