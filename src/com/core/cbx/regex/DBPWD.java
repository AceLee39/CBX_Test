// Copyright (c) 1998-2017 Core Solutions Limited. All rights reserved.
// ============================================================================
// CURRENT VERSION CNT.5.0.1
// ============================================================================
// CHANGE LOG
// CNT.5.0.1 : 2017-XX-XX, Ace.Li, creation
// ============================================================================

package com.core.cbx.regex;

import com.core.snp.system.util.CbxPasswordUtil;

/**
 * @author Ace.Li
 *
 */
public class DBPWD {

    /**
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        final String encryptedPassword = "p@ssw0rd#Cbx!";
        /*final String userName = "CBX661";
        final String password = AESEncryptorUtil.decrypt(encryptedPassword, userName);
        System.out.println(password);*/



        final String encodedPwd = CbxPasswordUtil
                .encodePassword(encryptedPassword, "SHA256");

        System.out.println(encodedPwd);
    }

}
