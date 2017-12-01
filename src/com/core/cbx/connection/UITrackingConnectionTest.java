// Copyright (c) 1998-2017 Core Solutions Limited. All rights reserved.
// ============================================================================
// CURRENT VERSION CNT.5.0.1
// ============================================================================
// CHANGE LOG
// CNT.5.0.1 : 2017-XX-XX, Ace.Li, creation
// ============================================================================

package com.core.cbx.connection;

import com.cbx.test.BaseTest;
import com.core.cbx.security.exception.AuthenticationException;
import com.core.cbx.tracking.UITrackingManager;

/**
 * @author Ace.Li
 *
 */
public class UITrackingConnectionTest extends BaseTest {

    public static void main(final String[] args) {
        try {
            login();
            for (int i=0; i<200;i++) {
                log.info("round : " + i);
                UITrackingManager.trackUIAction("test", 20);
            }
        } catch (final AuthenticationException e) {
            e.printStackTrace();
        }
    }
}
