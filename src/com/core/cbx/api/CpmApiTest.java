// Copyright (c) 1998-2017 Core Solutions Limited. All rights reserved.
// ============================================================================
// CURRENT VERSION CNT.5.0.1
// ============================================================================
// CHANGE LOG
// CNT.5.0.1 : 2017-XX-XX, Ace.Li, creation
// ============================================================================

package com.core.cbx.api;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.util.StringUtils;

import com.cbx.test.BaseTest;
import com.cbx.ws.rest.client.cpm.CbxApacheRestClient;
import com.core.cbx.security.AuthenticationUtil;

/**
 * @author Ace.Li
 *
 */
public class CpmApiTest extends BaseTest {
    /**
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        final CbxApacheRestClient client = new CbxApacheRestClient();
        tempLogin("RD2", "RD2", new AuthenticationUtil.ProcessWithException<Void, Exception>() {
            @Override
            public Void run() throws Exception {
                //final String cpmId = "99468834-872e-46f3-89ba-1102885bcc63";
                //final String cpmJ = client.getCpm(cpmId);
                boolean processing = true;
                while(processing) {
                    final List<String> cpms = new ArrayList<String>();
                    String cpm = null;
                    try {
                        final File file = new File("C:\\Users\\ace.li\\CBX\\Temp\\Api\\2.49_cpm.json");
                        cpm = FileUtils.readFileToString(file);
                        for (int i = 1; i < 2; i++) {
                            final String dateStr = buildDateStr(Math.round(10));
                            //final String cpmStr = StringUtils.replace(cpm, "2016-10-07", dateStr);
                            //final String cpmStr = StringUtils.replace(cpm, "2017-09-02", dateStr);//20
                            //final String cpmStr = StringUtils.replace(cpm, "2017-10-01", dateStr);//50
                            final int num = (int) (Math.random()*10);
                            final String cpmStr = StringUtils.replace(cpm, ":1,", ":"+num+",");//
                            cpms.add(cpmStr);
                        }
                        client.updateCpms(cpms);
                        Thread.sleep(1000);
                        log.info("Sleep...");
                    } catch (final Throwable e) {
                        processing = false;
                        Thread.sleep(10000);
                        log.info("Sleep...", e);
                    } finally {
                        //System.exit(0);
                        //FileUtils.deleteQuietly(file);
                        //return null;
                    }
                }
                return null;
            }

            private String buildDateStr(final int i) {
                if (i<10) {
                    return "2017-09-0" + i;
                } else {
                    return "2017-09-" + i;
                }
            }
        });
        log.info("finished...");
    }
}
