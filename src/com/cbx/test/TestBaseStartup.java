// Copyright (c) 1998-2017 Core Solutions Limited. All rights reserved.
// ============================================================================
// CURRENT VERSION CNT.5.0.1
// ============================================================================
// CHANGE LOG
// CNT.5.0.1 : 2017-XX-XX, Ace.Li, creation
// ============================================================================

package com.cbx.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.core.cbx.security.AuthenticationUtil;
import com.core.cbx.security.exception.AuthenticationException;
import com.core.cbx.security.user.CntUser;
import com.core.cbx.task.TaskModel;

/**
 * @author Ace.Li
 *
 */
public class TestBaseStartup extends BaseTest {
    private static final ExecutorService eService = Executors.newSingleThreadExecutor();
    /**
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        TaskModel.backendLogin();
        AuthenticationUtil.runInSystemTempContext("/", "/",
                new AuthenticationUtil.ProcessWithException<Void,Exception>() {
            @Override
            public Void run() throws Exception {
                final Future future = eService.submit(new Runnable() {
                @Override
                public void run() {
                          try {
                              AuthenticationUtil.switchUser("ace_api");
                              final CntUser user = AuthenticationUtil.getUser();
                              log.info(user);
                          } catch (final Exception e) {
                              log.info(e);
                          } finally {
                              try {
                                AuthenticationUtil.exitSwitchUser();
                            } catch (final AuthenticationException e) {
                                e.printStackTrace();
                            }
                          }
               }});
               future.get();
               return null;
        }});
    }
}
