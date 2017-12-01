// Copyright (c) 1998-2017 Core Solutions Limited. All rights reserved.
// ============================================================================
// CURRENT VERSION CNT.5.0.1
// ============================================================================
// CHANGE LOG
// CNT.5.0.1 : 2017-XX-XX, Ace.Li, creation
// ============================================================================

package com.cbx.test.serialized.save;

import com.cbx.test.BaseTest;
import com.core.cbx.action.ActionDispatcher;
import com.core.cbx.action.actionContext.LoadDoc;
import com.core.cbx.action.exception.ActionException;
import com.core.cbx.security.exception.AuthenticationException;

/**
 * @author Ace.Li
 *
 */
public class TestSerializedEntitySaver2 extends BaseTest {
    public static void main(final String[] args) throws AuthenticationException, ActionException {
        login("ace_api", "core@123");
        final String id = "bfbe263d3c754f778a50729a6cb6d467";
        final String moduleId = "sourcingRecord";
        final LoadDoc loadSR = new LoadDoc(id, -1, moduleId);
        ActionDispatcher.execute(loadSR);
        log.info("loaddoc succ" + loadSR.getDoc().getReference());
    }
}
