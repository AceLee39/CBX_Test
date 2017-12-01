// Copyright (c) 1998-2017 Core Solutions Limited. All rights reserved.
// ============================================================================
// CURRENT VERSION CNT.5.0.1
// ============================================================================
// CHANGE LOG
// CNT.5.0.1 : 2017-XX-XX, Ace.Li, creation
// ============================================================================

package com.cbx.test;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.cbx.constants.GlobeConstants;
import com.core.cbx.conf.exception.ConfigException;
import com.core.cbx.security.AuthenticationUtil;
import com.core.cbx.security.AuthenticationUtil.ProcessWithException;
import com.core.cbx.security.acl.AuthorizationUtil;
import com.core.cbx.security.exception.AuthenticationException;
import com.core.cbx.startup.BaseStartUp;

/**
 * @author Ace.Li
 *
 */
public class BaseTest {
    private static boolean initSprintSecurity = false;
    protected static boolean notNeedLogin = Boolean.FALSE;
    private static final Properties PROPERTIES = new Properties();
    protected static final Log log = LogFactory.getLog(BaseTest.class);
    static {
        try {
            final String isBackendProcess = System.getProperty(AuthorizationUtil.SYS_PROP_BACKEND_PROCESS);
            if (StringUtils.isEmpty(isBackendProcess)) {
                System.setProperty(AuthorizationUtil.SYS_PROP_BACKEND_PROCESS, String.valueOf(true));
            }
            BaseStartUp.run();
            initSpringSecurity();
            PROPERTIES.load(BaseTest.class.getClassLoader().getResourceAsStream(GlobeConstants.CONFIG));
        } catch (final IOException e) {
            throw new RuntimeException("Can load config properties", e);
        } catch (final ConfigException e) {
            throw new RuntimeException("fail to base startup", e);
        }
    }
    protected static void initSpringSecurity() {
        if (initSprintSecurity) {
            return;
        }
        initSprintSecurity = true;
        try {
            new FileSystemXmlApplicationContext("classpath:spring/security-context.xml");
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    protected static String getValueFromConfig(final String key) {
        return PROPERTIES.getProperty(key);
    }

    protected static String getValueFromConfig(final String key, final String defaultValue) {
        String value = getValueFromConfig(key);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    protected static void login() throws AuthenticationException {
        login(getValueFromConfig(GlobeConstants.LOGIN_USER),
                getValueFromConfig(GlobeConstants.LOGIN_PWD));
    }

    protected static void login(final String userName, final String password) throws AuthenticationException {
        AuthenticationUtil.login(userName, password);
    }

    protected static void tempLogin(final String domainId, final String hubDomainId, final ProcessWithException process) throws Exception {
        AuthenticationUtil.runInSystemTempContext(domainId, hubDomainId, process);
    }
}
