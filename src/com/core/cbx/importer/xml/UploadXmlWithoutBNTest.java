// Copyright (c) 1998-2017 Core Solutions Limited. All rights reserved.
// ============================================================================
// CURRENT VERSION CNT.5.0.1
// ============================================================================
// CHANGE LOG
// CNT.5.0.1 : 2017-XX-XX, Ace.Li, creation
// ============================================================================

package com.core.cbx.importer.xml;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.cbx.constants.GlobeConstants;
import com.cbx.test.BaseTest;
import com.core.cbx.modulesImp.ModuleImporter;

/**
 * @author Ace.Li
 *
 */
public class UploadXmlWithoutBNTest extends BaseTest {

    /**
     * @param args
     */
    public static void main(final String[] args) {
        try {
            login();
            if (StringUtils.equalsIgnoreCase(getValueFromConfig(GlobeConstants.IMPORT_IS_READ_FOLDER, "true"), "true")) {
                importReadFolder();
            } else {
                final String xmlFile = IOUtils.toString(new FileInputStream(
                        new File(getValueFromConfig(GlobeConstants.UPLOAD_XML_PATH))));
                ModuleImporter.getInstance().processImportByXmlFile(xmlFile);
                log.info("Upload successfully");
            }
        } catch (final Throwable e) {
            log.info("Error ! ", e);
        } finally {
            log.info("Generation End ! ");
            System.exit(-1);
        }
    }

    private static void importReadFolder() throws Exception{
        boolean isDeleted = Boolean.TRUE;
        while(true) {
            Thread.sleep(10000);
            final File importFile1 = readFileFromFolder(GlobeConstants.UPLOAD_XML_FOLDER);
            if (importFile1 == null) {
                log.info("sleep ========================= ");
                continue;
            }


            final String fileName = importFile1.getName();
            log.info("fileName ===== " + fileName);
            try {
                if (isDeleted) {
                    final String xmlFile = IOUtils.toString(new FileInputStream(importFile1));
                    ModuleImporter.getInstance().processImportByXmlFile(xmlFile);
                }
            } catch (final Exception e) {
                log.error("error : ", e);
            } finally {
                Thread.sleep(10000);
                isDeleted = removeFileInFolder(fileName, GlobeConstants.UPLOAD_XML_FOLDER);
                if (!isDeleted) {
                    Thread.sleep(10000);
                    log.info("Fail to remove fileName ===== " + fileName);
                    log.info("Please remove it by hand fileName ===== " + fileName);
                }
            }
        }
    }
}
