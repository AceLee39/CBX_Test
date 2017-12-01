// Copyright (c) 1998-2015 Core Solutions Limited. All rights reserved.
// ============================================================================
// CURRENT VERSION CNT.5.11
// ============================================================================
// CHANGE LOG
// CNT.5.11 : 2015-12-15, brady.lin, CNT-19151
// CNT.5.10.GA : 2015-08-27, denny.deng, CNT-19105
// ============================================================================

package com.core.cbx.task.handler;

import javax.jms.Connection;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;

import com.core.cbx.command.CommandConstants;
import com.core.cbx.command.handler.CommandHandlerImp;
import com.core.cbx.command.listener.CommandMessageListener;
import com.core.cbx.conf.service.SystemConfigManager;
import com.core.cbx.data.constants.ExceptionConstants;
import com.core.cbx.jms.JMSUtility;
import com.core.cbx.jms.JmsFactory;
import com.core.cbx.task.TaskConstants;

/**
 *
 * @author denny.deng
 *
 */
public class CommandMessageSchedulerHandler extends ScheduledHandlerImpl {

    @Override
    protected void processJob() {
    logger.debug("begin to execute CommandMessageSchedulerHandler...");
    final String[] args = (String[]) context.get(TaskConstants.ARGS);
    if (args.length < 4) {
        logger.debug("agrs length should be 4, args=" + args.length);
        return;
    }
    logger.debug("domainId=" + args[1] + ", commandHandlerClassName=" + args[2] + ", commandType=" + args[3]);
    final String commandHandlerClassName = args[2];
    final String commandType = args[3];
    CommandHandlerImp commandHandler = null;

    try {
        final Class<?> commandHandlerClz = ClassUtils.getClass(commandHandlerClassName);
        commandHandler = (CommandHandlerImp) commandHandlerClz.newInstance();
    } catch (final ClassNotFoundException e) {
        logger.error(ExceptionConstants.DATA_EXCEPTION_000001, "Can not find class for " + commandHandlerClassName, e);
    } catch (final InstantiationException e1) {
        logger.error(ExceptionConstants.DATA_EXCEPTION_000001, "Can not initialize instacne for "
                + commandHandlerClassName, e1);
    } catch (final IllegalAccessException e2) {
        logger.error(ExceptionConstants.DATA_EXCEPTION_000001, "Can not initialize instacne for "
                + commandHandlerClassName, e2);
    }

    if (commandHandler == null || StringUtils.isBlank(commandType)) {
        logger.debug("commandHandler or commandTpye is null");
        return;
    }
    Connection conn = null;
    Session session = null;
    try {
        conn = JmsFactory.getInstance().getConnection();
        session = conn.createSession(true, Session.SESSION_TRANSACTED);
        final String prefix = SystemConfigManager.getInstance()
                .getConfigValue(CommandConstants.SYS_CONFIG_MQ_NAME_PREFIX);
        final Queue queue = session.createQueue(prefix + commandType);
        final MessageConsumer consumer = session.createConsumer(queue);
        conn.start();
        logger.debug("listening to " + prefix + commandType);
        while (true) {
            final Message m = consumer.receiveNoWait();
            if (m != null) {
                logger.debug("Reading message: " + m.toString());
                final CommandMessageListener msgListener
                    = new CommandMessageListener(commandHandler, commandType, session, this.getDomainIds());
                msgListener.setRequireTempLogin(Boolean.FALSE);
                msgListener.setAlive(true);
                msgListener.onMessage(m);
            } else {
                Thread.sleep(10000);
            }
        }
    } catch (final Exception e) {
        JMSUtility.rollback(session);
        logger.error("01070011", e.getMessage(), e);
  } finally {
      logger.debug("execute CommandMessageSchedulerHandler end...");
      JMSUtility.close(conn, session);
    }
  }
}
