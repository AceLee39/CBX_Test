// Copyright (c) 1998-2015 Core Solutions Limited. All rights reserved.

package com.core.cbx.importer.xml;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.cbx.constants.GlobeConstants;
import com.cbx.test.BaseTest;

/**
 * @author Ace.Li
 *
 */
public class RebuildUploadXmlCheckSumUtils extends BaseTest {
    private static final String META_END = "</meta>";
    private static final String ADMIN_MODULE_XML_END = "</CBXAdminConfigurations>";
    public static void main(final String[] args) throws Exception {
        final String xml = IOUtils.toString(new FileInputStream(new File(getValueFromConfig(GlobeConstants.UPLOAD_XML_PATH))));
        final String checksum = calculateAdminModuleChecksum(xml);
        FileUtils.writeStringToFile(new File(
                getValueFromConfig(GlobeConstants.CHECKSUM_OUTPUT)), checksum, Charsets.UTF_8.toString());
        System.out.println(checksum);
    }

    public static String calculateAdminModuleChecksum(final String xml) {
        final String modules = StringUtils.substringBetween(xml, META_END, ADMIN_MODULE_XML_END);
        return DigestUtils.sha256Hex(modules.getBytes(Charsets.UTF_8));
    }

}
