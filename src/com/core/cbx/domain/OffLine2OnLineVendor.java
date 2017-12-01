package com.core.cbx.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;

import com.cbx.constants.GlobeConstants;
import com.cbx.test.BaseTest;
import com.core.cbx.action.workerAction.ActivateVendorWorkerAction;
import com.core.cbx.common.exception.SystemException;
import com.core.cbx.common.logging.CNTLogger;
import com.core.cbx.common.logging.LogFactory;
import com.core.cbx.common.type.DateTime;
import com.core.cbx.security.acl.AuthorizationUtil;

/**
 * @author calvin.yang
 */
public class OffLine2OnLineVendor extends BaseTest {
    private static final CNTLogger LOGGER  = LogFactory.getLogger(OffLine2OnLineVendor.class);


    /**
     * @param args
     */
    public static void main(final String[] args) {
        LOGGER.info("Usage: java com.core.cbx.task.VendorMigrationLauncher "
                + "<project domain id> <project domain administrator user login ID> <vendor ID>");

        final String domainId = getValueFromConfig(GlobeConstants.PROJECT_DOMAIN_ID);
        final String adminUserLoginID = getValueFromConfig(GlobeConstants.ADMIN_USER_LOGIN_ID);
        final String vendorId = getValueFromConfig(GlobeConstants.VENDOR_ID);

        long t1 = DateTime.now().getMilliseconds(TimeZone.getDefault());

        LOGGER.info("===============================================================================");
        LOGGER.info("Start to run Vendor Migration utility!");
        LOGGER.info("Vendor Migration started: " + DateTime.now());

        // start up
        try {
            final String isBackendProcess = System.getProperty(AuthorizationUtil.SYS_PROP_BACKEND_PROCESS);
            if (StringUtils.isEmpty(isBackendProcess)) {
                System.setProperty(AuthorizationUtil.SYS_PROP_BACKEND_PROCESS, String.valueOf(true));
            }
            runSingleVendorMigration(domainId, adminUserLoginID, vendorId);
        } catch (final Throwable e) {
            LOGGER.fatal("Exception", "Error in running Vendor Migration", e);
        } finally {
            final Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
            for (final Thread thread : threads.keySet()) {
                if ("sendVendorPassword".equals(thread.getName())) {
                    try {
                        LOGGER.info("===============================================================");
                        LOGGER.info("================= start sending password email ================");
                        thread.join();
                        LOGGER.info("================== send password email over! ==================");
                    } catch (final InterruptedException e) {
                        LOGGER.warn("InterruptedException", "Thread has been interrupted.", e);
                    }
                }
            }
            t1 = DateTime.now().getMilliseconds(TimeZone.getDefault()) - t1;
            LOGGER.info("Vendor Migration utility finished completely!");
            LOGGER.info("Vendor Migration ended: " + DateTime.now());
            LOGGER.info("Vendor Migration used: " + t1 + "ms");
            LOGGER.info("===============================================================================");
            System.exit(0);
        }
    }

    private static void runSingleVendorMigration(final String domainId,
            final String adminUserLoginID, final String vendorId) throws SystemException {
        final Map<String, Object> context = new HashMap<String, Object>();
        context.put(GlobeConstants.PROJECT_DOMAIN_ID, domainId);
        context.put(GlobeConstants.ADMIN_USER_LOGIN_ID, adminUserLoginID);
        context.put(GlobeConstants.VENDOR_ID, vendorId);
        final ActivateVendorWorkerAction worker = new ActivateVendorWorkerAction(context);
        worker.execute();
    }
}
