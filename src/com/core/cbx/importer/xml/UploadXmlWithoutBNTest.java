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
            final String xmlFile = IOUtils.toString(new FileInputStream(
                    new File(getValueFromConfig(GlobeConstants.UPLOAD_XML_PATH))));
            ModuleImporter.getInstance().processImportByXmlFile(xmlFile);
            log.info("Upload successfully");
        } catch (final Throwable e) {
            log.info("Error ! ", e);
        } finally {
            log.info("Generation End ! ");
            System.exit(-1);
        }
    }
}
