// Copyright (c) 1998-2017 Core Solutions Limited. All rights reserved.
// ============================================================================
// CURRENT VERSION CNT.5.0.1
// ============================================================================
// CHANGE LOG
// CNT.5.0.1 : 2017-XX-XX, Ace.Li, creation
// ============================================================================

package com.cbx.test.freemarker;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.core.cbx.common.type.DateTime;
import com.core.cbx.data.exception.DataException;
import com.core.cbx.notification.method.DomainDateTimeMethod;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * @author Ace.Li
 *
 */
public class TestFreeMarker {
    private static final String FREEMAKER_TEMPLATE_NAME = "strTemplate";
    private static final String FREEMAKER_REQ_DOC = "reqDoc";
    private static final String FREEMAKER_REF_DOCS = "refDocs";
    public static void main(final String[] args) throws DataException {
        final Map<String, Object> params = new HashMap<>();
        params.put("sentOn", "2017-11-24 19:00:11.1111111");
        params.put("sentOn1", "2017-11-24 00:06:00");
        params.put("sentOn2", "2017-11-24");
        final Map<String, Object> reqDoc = new  HashMap<>();
        final String dateTimeFormat = "YYYY-MM-DD hh:mm:ss";
        reqDoc.put("updatedOn", DateTime.now().format(dateTimeFormat));
        final String source = "<#assign updatedOn=addTimeOffset(reqDoc.updatedOn)>"
                + ""
                + "\nSent on:${addTimeOffset(sentOn).format(\"YYYY-MM-DD\")!''}"
                + "\nupdated on:${(updatedOn)!\"\"}"
                + "\nSent on1:${addTimeOffset(sentOn,\"YYYY-MM-DD\")!''}"
                + "\nSent on1:${addTimeOffset(sentOn1,\"YYYY-MM-DD\")!''}"
                + "\nSent on2:${addTimeOffset(sentOn2,\"YYYY-MM-DD hh:mm:ss\")!''}"
                + "\nSent on3:${addTimeOffset(sentOn,\"MM/DD/YYYY hh:mm:ss\")!''}";
        final String ddd = resolveText(source, reqDoc, params);
        System.out.println(ddd);
    }

    private static String resolveText(final String source, final Map<String, Object>  reqDoc,
            final Map<String, Object> params) throws DataException {
        StringWriter writer = null;
        try {
            final Configuration conf = new Configuration();
            final Template template = new Template(FREEMAKER_TEMPLATE_NAME, new StringReader(source), conf);

            final Map<String, Object> root = new HashMap<String, Object>();
            if (params != null) {
                root.putAll(params);
            }
            if (reqDoc != null) {
                root.put(FREEMAKER_REQ_DOC, reqDoc);
            }
            root.put("addTimeOffset", new DomainDateTimeMethod("UTC+8:00"));
            writer = new StringWriter();
            template.process(root, writer);
            return writer.toString();
        } catch (final TemplateException e) {
            throw new DataException("070065", "Notification -- TemplateException at resolveText.", e);
        } catch (final IOException e) {
            throw new DataException("070066", "Notification -- IOException at resolveText.", e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }



}
