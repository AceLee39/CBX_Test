// Copyright (c) 1998-2017 Core Solutions Limited. All rights reserved.
// ============================================================================
// CURRENT VERSION CNT.5.0.1
// ============================================================================
// CHANGE LOG
// CNT.5.0.1 : 2017-XX-XX, Ace.Li, creation
// ============================================================================

package com.core.cbx.poi;

import java.util.List;

import com.cbx.test.BaseTest;
import com.core.cbx.data.DynamicEntityModel;
import com.core.cbx.data.def.EntityDefManager;
import com.core.cbx.data.def.entity.EntityDefinition;
import com.core.cbx.data.def.entity.FieldDefinition;
import com.core.cbx.data.entity.DynamicEntity;
import com.core.cbx.data.exception.DataException;

/**
 * @author Ace.Li
 *
 */
public class TestPoi extends BaseTest {

    public static void main(final String[] args) throws DataException {
        final EntityDefinition rfiDef = EntityDefManager.getLatestEntityDefinition("RfiItem");
        final List<FieldDefinition> fieldDefs = rfiDef.getFieldDefinitions();
        final DynamicEntity test = DynamicEntityModel.createDefaultEntity("RfiItem");
        for (final FieldDefinition fiedlDef : fieldDefs) {
            test.put(fiedlDef.getFieldId(), fiedlDef.getFieldId().hashCode());
        }
        System.out.println(test.entrySet());
    }
}
