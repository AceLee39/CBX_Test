// Copyright (c) 1998-2017 Core Solutions Limited. All rights reserved.
// ============================================================================
// CURRENT VERSION CNT.5.0.1
// ============================================================================
// CHANGE LOG
// CNT.5.0.1 : 2017-XX-XX, Ace.Li, creation
// ============================================================================

package com.cbx.test.hq;

import com.cbx.test.BaseTest;
import com.core.cbx.command.entity.Command;
import com.core.cbx.command.producer.CommandProducer;
import com.core.cbx.data.DynamicEntityModel;
import com.core.cbx.data.entity.DynamicEntity;
import com.core.cbx.data.exception.DataException;
import com.core.cbx.data.serializer.SerializationManager;
import com.core.cbx.notification.constants.NotificationConstants;
import com.core.cbx.security.exception.AuthenticationException;

/**
 * @author Ace.Li
 *
 */
public class TestHq extends BaseTest {
    public static void main(final String[] args) throws DataException, AuthenticationException {
        login();
        final Thread t = new Thread(new TestHQRunner(NotificationConstants.COMMAND_TYPE_ASYNC_NOTIFICATION));
        t.start();
        final Thread t1 = new Thread(new TestHQRunner("SendDoc"));
        t1.start();
        final Thread t2 = new Thread(new TestHQRunner("BuildEntityVersionAndHistory"));
        t2.start();
        final Thread t3 = new Thread(new TestHQRunner("MetaDataHistory"));
        t3.start();
        final Thread t4 = new Thread(new TestHQRunner("EmailNotification"));
        t4.start();
        final Thread t5 = new Thread(new TestHQRunner("BuildEntityRevision"));
        t5.start();
        final Thread t6 = new Thread(new TestHQRunner("CustomizedEvent"));
        t6.start();
    }

    public static class TestHQRunner implements Runnable {
        String commandType;
        public TestHQRunner(final String commandType) {
            this.commandType = commandType;
        }

        @Override
        public void run() {
            final String id="9c5b6f7830694001a9616273049421fa";
            DynamicEntity item = null;
            byte[] serializeToBase64String = null;
            try {
                item = DynamicEntityModel.getFullEntity("Item", id);
                serializeToBase64String = SerializationManager.serialize(item);
            } catch (final DataException e1) {
            }
            for (int i = 0; i < 500; i++) {
                log.info("Begin send notification count : " + i);
                try {
                    final Command command = new Command();
                    command.setCommandType(commandType);
                    command.addPayload(serializeToBase64String);
                    sendNotification(command);
                } catch (final DataException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static void sendNotification(final Command command) throws DataException {
        log.info("Begin send notification parameter to backend=========================================");
        //final List<String> payloads = new ArrayList<String>();
        //final byte[] param =  SerializationUtils.serialize(parameter);
        //payloads.add(Base64.encodeBase64String(param));
        //command.setPayloads(payloads);
        //command.setDomainId(AuthenticationUtil.getUserCurrentDomainId());
       // command.setHubDomainId(AuthenticationUtil.getUserWorkingDomainId());
        CommandProducer.getInstance().send(command);
    }
}
