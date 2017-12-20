// Copyright (c) 1998-2017 Core Solutions Limited. All rights reserved.
// ============================================================================
// CURRENT VERSION CNT.5.0.1
// ============================================================================
// CHANGE LOG
// CNT.5.0.1 : 2017-XX-XX, Ace.Li, creation
// ============================================================================

package com.cbx.test.email;

import com.core.cbx.common.util.MailUtil;
import com.core.cbx.util.MailUtilFactory;

/**
 * @author Ace.Li
 *
 */
public class TestSendEmail {
    public static void main(final String[] args) throws Exception {
        final MailUtil mailUtil = MailUtilFactory.getMailUtilInstance();
        mailUtil.setSubject("test");
        mailUtil.setContent("test");
        mailUtil.setToAddress("ace.li@coresolutions.com");
        mailUtil.sendEmail();
    }
}
