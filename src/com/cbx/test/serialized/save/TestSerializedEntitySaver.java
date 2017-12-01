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
import com.core.cbx.action.actionContext.EditDoc;
import com.core.cbx.action.actionContext.ItemSaveDoc;
import com.core.cbx.action.actionContext.LoadDoc;
import com.core.cbx.action.exception.ActionException;
import com.core.cbx.data.entity.DynamicEntity;
import com.core.cbx.security.exception.AuthenticationException;

/**
 * @author Ace.Li
 *
 */
public class TestSerializedEntitySaver extends BaseTest {
    public static void main(final String[] args) throws AuthenticationException, ActionException {
        login();
        final String id = "814c95632dec46a48b75ff6710c5a85d";
        final String moduleId = "item";
        final LoadDoc loadSR = new LoadDoc(id, -1, moduleId);
        ActionDispatcher.execute(loadSR);
        final DynamicEntity doc = loadSR.getDoc();
        final EditDoc ed = new EditDoc(doc);
        ActionDispatcher.execute(ed);
        final ItemSaveDoc saveSR = new ItemSaveDoc((DynamicEntity) ed.getResultValue(EditDoc.KEY_DOC_ENTITY));
        ActionDispatcher.execute(saveSR);
        log.info("loaddoc succ" + saveSR.getDoc().getReference());
    }
}
