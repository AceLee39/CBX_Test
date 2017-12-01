// Copyright (c) 1998-2017 Core Solutions Limited. All rights reserved.
// ============================================================================
// CURRENT VERSION CNT.5.0.1
// ============================================================================
// CHANGE LOG
// CNT.5.0.1 : 2017-XX-XX, Ace.Li, creation
// ============================================================================

package com.cbx.test.thread;

import java.sql.Connection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import com.cbx.test.BaseTest;
import com.core.cbx.concurrent.ExecutorServiceManager;
import com.core.cbx.data.exception.DataException;
import com.core.cbx.database.DBUtility;
import com.core.cbx.database.DaoFactory;
import com.core.cbx.transaction.TransactionManager;
import com.core.cbx.transaction.TransactionUnit;

/**
 * @author Ace.Li
 *
 */
public class ThreadTransactionTest extends BaseTest {
    private static final ExecutorService metaDataAsyncService = Executors.newSingleThreadExecutor();
    private static boolean isRegister = false;
    /**
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        login();
        TransactionManager.runInTransaction(new TransactionUnit() {

            public void run() throws Exception {
                Connection conn = null;
                boolean newTx1 = false;
                UserTransaction utx1 = null;
                try {
                    utx1 = TransactionManager.getUserTransaction();
                    newTx1 = (utx1 != null && utx1.getStatus() != Status.STATUS_ACTIVE);
                    System.out.println(newTx1);
                    conn = DaoFactory.getInstance().getConnection();
                } catch (final DataException e) {
                    e.printStackTrace();
                } finally {
                    DBUtility.close(conn);
                }
                metaDataAsyncService.execute(new Runnable() {
                    public void run() {
                        Connection conn = null;
                        final boolean newTx1 = false;
                        final UserTransaction utx1 = null;
                        try {
                            //utx1 = TransactionManager.getUserTransaction();
                            //newTx1 = (utx1 != null && utx1.getStatus() != Status.STATUS_ACTIVE);
                            System.out.println(newTx1);
                            conn = DaoFactory.getInstance().getConnection();
                        } catch (final Exception e) {
                            e.printStackTrace();
                        } finally {
                            DBUtility.close(conn);
                        }
                        System.out.println("thread end");
                    }
                });
                if (!isRegister) {
                    ExecutorServiceManager.getInstance().registerExecutorService(metaDataAsyncService);
                    isRegister = Boolean.TRUE;
                }
            }
        });
        System.out.println("end");
    }

}
