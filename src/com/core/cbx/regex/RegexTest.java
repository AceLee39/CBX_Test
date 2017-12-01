// Copyright (c) 1998-2017 Core Solutions Limited. All rights reserved.
// ============================================================================
// CURRENT VERSION CNT.5.0.1
// ============================================================================
// CHANGE LOG
// CNT.5.0.1 : 2017-XX-XX, Ace.Li, creation
// ============================================================================

package com.core.cbx.regex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mvel2.MVEL;

import com.core.cbx.common.logging.LogFactory;
import com.core.cbx.common.type.DateTime;
import com.core.cbx.data.exception.DataException;
import com.core.cbx.el.CoreEL;
import com.core.cbx.el.exception.ExpressionException;

/**
 * @author Ace.Li
 *
 */
public class RegexTest {
    private static final String REGEX = "^(/[\\w\\s-]+)+[^/]$";
    private static final String CONST_VALUE = "Value";
    private static final String CONST_NAME = "Name";
    private static final String CONST_FULL_LINEAGE = "FullLineage";
    private static final String CONST_VERSION = "Ver";

    /**
     * @param args
     * @throws ExpressionException
     * @throws DataException
     */
    public static void main(final String[] args) throws ExpressionException, DataException {
        LogFactory.getInstance().init(null);
        com.core.cbx.logging.LogFactory.getInstance().init(null);
        final Map<String, Object> entity = new HashMap();
        final List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        final Map<String, Object> e = new HashMap<String, Object>();
        e.put("test", "i test you");
        //list.add(e);
        final Map<String, Object> e2 = new HashMap<String, Object>();
        e2.put("test", "i test you2");
        //list.add(e2);
        entity.put("items", list);
        entity.put("datetime", "2017-10-30 13:25:49");
        final DateTime now = DateTime.now();
        final String nowstr = now.toString();
        final Map<String, Object> entity1 = new HashMap();
        entity1.put("now", now);
        entity.put("entity", entity1);
        //final ExpressionContext context = new ExpressionContext((DynamicEntity) entity);
        final Object result = MVEL.executeExpression(CoreEL.getCompiledExp("addTimeOffset(entity.now).toString()"+"=='" + nowstr+"'"), entity);
        System.out.println(result);


        //final String bookmark = "6163743d4f70656e53656172636856696577416374696f6e26766965774e616d653d736f757263696e675265636f72644163746976655669657726697341646d696e4d6f64653d66616c7365266175746f5365617263683d66616c7365266175746f416476616e6365645365617263683d74727565266669656c643d6974656d4e6f5f49544d313730362d303030303136266973506f70757042726f777365723d74727565";
        //final String test = new String(ByteArrayHexUtil.hex2byte(bookmark));
        /*final String fullLineage = "/A";
        System.out.println(fullLineage.matches(REGEX));
        final String fieldId = "dataEntity";
        System.out.println(EntityDefModel.constructFieldIdByPostfix(
                fieldId, EntityConstants.PTY_POSTFIX_VER));
        final Object value = "666.0000";
        System.out.println(NumberUtil.toBigDecimal(value).toBigInteger().longValue());

        final Map<String, Object> aaa = new HashMap<String, Object>();
        final Long bb = (Long) aaa.get("1");
        System.err.println(bb);*/
        /*final List<String> test = new ArrayList<String>();
        final List<String> test2 = new ArrayList<String>();
        for(int i=0; i<5; i++) {
            test.add("" + i);
            if (i%2==0) {
                test2.add("" + i);
            }
        }
        //test.removeAll(test2);
        CollectionUtils.removeAll(test, test2);
        System.out.println(test);

        final Map<String, Object> intSet = new HashMap<String, Object>();
        final String itemName = "itemName";
        final String item = "item";
        final String domainId = "domainId";
        final String custHcl3FullCode = "custHcl3FullCode";
        final String commentToVendor = "commentToVendor";
        intSet.put(item, item.hashCode());
        intSet.put(domainId, domainId.hashCode());
        intSet.put(commentToVendor, commentToVendor.hashCode());
        intSet.put(itemName, itemName.hashCode());
        intSet.put(custHcl3FullCode, custHcl3FullCode.hashCode());
        System.out.println(intSet.entrySet());*/

    }

}
