// Copyright (c) 1998-2016 Core Solutions Limited. All rights reserved.
// ============================================================================
// CURRENT VERSION CNT.5.15
// ============================================================================
// CHANGE LOG
// CNT.5.15 : 2016-06-17, paul.lin, CNT-17427
// CNT.5.11.1 : 2016-03-01 Nick.Fu CNT-21612
// CNT.5.10.0 : 2015-08-26, paul.lin, CNT-19096
// CNT.5.0.074 : 2014-12-30, randy.huang, CNT-14835
// CNT.5.0.63 : 2013-12-12, randy.huang, CNT-11882
// CNT.5.0.062 : 2013-11-18, randy.huang, CNT-10612
// CNT.5.0.59 : 2013-08-26, randy.huang, CNT-10617
// CNT.5.0.042 : 2013-07-22, mark.lin, CNT-9721
// CNT.5.0.032.1 : 2012-12-05, randy.huang, CNT-7194
// CNT.5.0.1 : 2012-XX-XX, Jimmy.Chen, creation
// ============================================================================
package com.core.cbx.command;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.transaction.UserTransaction;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import com.core.cbx.command.handler.CommandHandler;
import com.core.cbx.command.handler.RouterHandler;
import com.core.cbx.command.listener.CommandMessageListener;
import com.core.cbx.command.thread.HeartbeatThread;
import com.core.cbx.command.thread.ShutdownHookThread;
import com.core.cbx.common.exception.SystemException;
import com.core.cbx.common.logging.CNTLogger;
import com.core.cbx.common.logging.LogFactory;
import com.core.cbx.common.type.DateTime;
import com.core.cbx.data.DynamicEntityModel;
import com.core.cbx.data.constants.CommandListener;
import com.core.cbx.data.constants.ExceptionConstants;
import com.core.cbx.data.entity.DynamicEntity;
import com.core.cbx.data.entity.EntityConstants;
import com.core.cbx.data.exception.DataException;
import com.core.cbx.data.search.Criterion;
import com.core.cbx.data.search.Restriction;
import com.core.cbx.jms.JmsModel;
import com.core.cbx.task.TaskModel;
import com.core.cbx.transaction.TransactionManager;

/**
 * @author Jimmy.Chen
 */
public class ListenerManager {

    public static final String ROUTER = "$ROUTER";
    public static final String ALL_HANDLER = "$ALL_HANDLERS";
    private static volatile ListenerManager instance;
    private static final CNTLogger LOGGER = LogFactory.getLogger(ListenerManager.class);
    private static final String SETTING_DOMAINS = "settingDomains";
    private static final String ROOT = "/";
    private static final String ROOT_TO_DOMAIN_DEDICATED_SERVER = "routeToDomainServer";
    private static final String COMMAND_ROUTER_ID = "CommandRouterListener00000000ace";

    private DynamicEntity listenerEntity;
    private Map<String, DynamicEntity> handlerEntitys;
    private CommandHandler routerHandler;
    private Map<String, CommandHandler> listenerHandlers;
    private boolean containedRootDomain = false;

    protected ListenerManager() { }

    public static ListenerManager getInstance() {
        if (instance == null) {
            synchronized (ListenerManager.class) {
                if (instance == null) {
                    instance = new ListenerManager();
                }
            }
        }
        return instance;
    }

    public void init(final String listenerName, final List<String> handlerNames, final List<String> domainIds)
            throws DataException {
        if (StringUtils.isEmpty(listenerName)) {
            throw new IllegalArgumentException("Listener name can not be blank.");
        }
        if (!acquireListenerLock(listenerName, domainIds)) {
            LOGGER.error("01070031", "There is another command listener with the same name is running.");
            throw new DataException("01070031", "There is another command listener with the same name is running.");
        }

        checkIsContainRootDomain(domainIds);

        List<DynamicEntity> commandHandlers = null;
        final List<String> handlerNameList = getHandlerNames(handlerNames);
        if (handlerNames.contains(ALL_HANDLER)) {
            // load all handlers by default (if not provided in parameter)
            final Criterion criterion = new Criterion(CommandListener.ENTITY_NAME_COMMAND_HANDLER);
            if (!containedRootDomain) {
                criterion.addRestriction(Restriction.eq(ROOT_TO_DOMAIN_DEDICATED_SERVER, true));
            }
            commandHandlers = DynamicEntityModel.findEntities(criterion, false);
        } else if (!CollectionUtils.isEmpty(handlerNameList)) {
            commandHandlers = new ArrayList<DynamicEntity>();
            for (final String handlerName : handlerNameList) {
                final DynamicEntity commandHandler = DynamicEntityModel.findUniqueEntityBy(
                        CommandListener.ENTITY_NAME_COMMAND_HANDLER, CommandListener.COMMAND_TYPE, handlerName, false);
                if (commandHandler == null) {
                    throw new IllegalArgumentException("Can not find the habdler.");
                }
                final boolean isRouteToSpecialDomain = commandHandler.getBoolean(ROOT_TO_DOMAIN_DEDICATED_SERVER);
                if (containedRootDomain || isRouteToSpecialDomain) {
                    commandHandlers.add(commandHandler);
                }
            }
        }

        if (!CollectionUtils.isEmpty(commandHandlers)) {
            this.listenerHandlers = new HashMap<String, CommandHandler>();
            this.handlerEntitys = new HashMap<String, DynamicEntity>();
            for (final DynamicEntity entity : commandHandlers) {
                final String commandType = ObjectUtils.toString(entity.get(CommandListener.COMMAND_TYPE));
                this.handlerEntitys.put(commandType, entity);
                final String implementation = ObjectUtils.toString(entity.get(CommandListener.IMPLEMENTATION));
                try {
                    final CommandHandler commandHandler = (CommandHandler) Class.forName(implementation).newInstance();
                    this.listenerHandlers.put(commandType, commandHandler);
                } catch (final InstantiationException e) {
                    LOGGER.error("01070032", "InstantiationException.", e);
                    throw new DataException("01070032", "InstantiationException.", e);
                } catch (final IllegalAccessException e) {
                    LOGGER.error("01070033", "IllegalAccessException.", e);
                    throw new DataException("01070033", "IllegalAccessException.", e);
                } catch (final ClassNotFoundException e) {
                    LOGGER.error("01070034", "ClassNotFoundException.", e);
                    throw new DataException("01070034", "ClassNotFoundException.", e);
                }
            }
        }

        // Register shutdown hook thread
        Runtime.getRuntime().addShutdownHook(new ShutdownHookThread());
    }

    private boolean checkIsContainRootDomain(final List<String> domainIds) {
        if (CollectionUtils.isNotEmpty(domainIds) && domainIds.contains(ROOT)) {
            containedRootDomain = true;
        }
        return containedRootDomain;
    }

    private List<String> getHandlerNames(final List<String> handlerNames) {
        final List<String> names = new ArrayList<String>();
        if (CollectionUtils.isNotEmpty(handlerNames)) {
            if (!handlerNames.contains(ALL_HANDLER)) {
                for (final String handlerName : handlerNames) {
                    if (!StringUtils.equals(handlerName, ROUTER)) {
                        names.add(handlerName);
                    }
                }
            }

        }
        return names;
    }

    public void start() throws DataException {
        this.startRouter();
        this.startHandlers();
    }

    public void start(final List<String> handlerNames) throws DataException {
        if (handlerNames != null && handlerNames.contains(ROUTER) && containedRootDomain) {
            this.startRouter();
        }
        this.startHandlers();
    }

    /**
     * Destroy the command handler.
     * @param commandHandler
     * @throws DataException
     */
    private void destroyCommandHandler(final CommandHandler commandHandler) throws DataException {
        try {
            commandHandler.destroy();
        } catch (final SystemException e) {
            throw new DataException(ExceptionConstants.DATA_EXCEPTION_000001,
                    "Fail to destroy the command handler.[" + commandHandler.getClass().getSimpleName() + "]", e);
        }
    }

    public void stop() throws DataException {
        if (routerHandler != null) {
            LOGGER.info("Stopping command router");
            if (routerHandler != null) {
                destroyCommandHandler(routerHandler);
            } else {
                LOGGER.debug("Not found router");
            }
            JmsModel.stopMessageListener(RouterHandler.QUEUE_ALIAS);
        }

        if (listenerHandlers != null) {
            LOGGER.info("Stopping command handlers");
            for (final String handlerNames : listenerHandlers.keySet()) {
                JmsModel.stopMessageListener(handlerNames);
                final CommandHandler listenhandler = listenerHandlers.get(handlerNames);
                if (listenhandler != null) {
                    destroyCommandHandler(listenhandler);
                }
            }
        }

        if (listenerEntity != null) {
            LOGGER.info("Stopping command listener");
            UserTransaction utx = null;
            try {
                utx = TransactionManager.getUserTransaction();
                TransactionManager.begin(utx);
                listenerEntity.put(CommandListener.STATUS, CommandConstants.STATUS_DOWN);
                listenerEntity.put(CommandListener.LAST_STOPPED_ON, DateTime.now());
                listenerEntity.put(CommandListener.UPDATED_ON, DateTime.now());
                TaskModel.updateCommandListener(listenerEntity);
                TransactionManager.commit(utx);
                LOGGER.info(String.format("Command listener %1$s is stopped.",
                        listenerEntity.getString(CommandListener.LISTENER_NAME)));
            } catch (final Exception e) {
                TransactionManager.rollback(utx);
                LOGGER.error("stop()", "Error in stopping listener.", e);
                LOGGER.info(String.format("Command listener %1$s is stopped with error.",
                        listenerEntity.getString(CommandListener.LISTENER_NAME)));
            }
        }
    }

    public void sleepForever() throws DataException {
        try {
            LOGGER.info("Put current thread into sleep status");
            Thread.sleep(Long.MAX_VALUE);
        } catch (final InterruptedException e) {
            LOGGER.error("01070035", "InterruptedException.", e);
            throw new DataException("01070035", "InterruptedException.", e);
        }
    }

    protected boolean acquireListenerLock(final String listenerName, final List<String> domainIds)
            throws DataException {
        // Step 1: Check if there are another listener with the same name running
        LOGGER.info("Starting listener: " + listenerName);
        final Criterion criterion = new Criterion(CommandListener.ENTITY_NAME_COMMAND_LISTENER);
        criterion.addRestriction(Restriction.eq(CommandListener.LISTENER_NAME, listenerName));
        DynamicEntity listenerEntity = DynamicEntityModel.findUniqueBy(criterion, false);
        if (listenerEntity != null) {
            final Long status = (Long) listenerEntity.get(CommandListener.STATUS);
            final DateTime lastHeartbeat = (DateTime) listenerEntity.get(CommandListener.LAST_HEARTBEAT);
            final long heartbeatInterval = CommandQueueModel.getSystemHeartbeatInterval();
            final DateTime now = DateTime.now();
            LOGGER.info("Checking listener DB status");
            LOGGER.info("DB status: " + status);
            LOGGER.info("DB last heartbeat: " + lastHeartbeat);
            LOGGER.info("now: " + now);
            if (lastHeartbeat != null) {
                LOGGER.info("lastHeartbeat.numSecondsFrom(now): " + lastHeartbeat.numSecondsFrom(now));
            }
            if (CommandConstants.STATUS_UP == status && lastHeartbeat != null
                    && lastHeartbeat.numSecondsFrom(now) < heartbeatInterval) {
                LOGGER.info("Status is up and last heartbeat is not expired");
                return false;
            }
        }

        // Step2:
        UserTransaction utx = null;
        try {
            utx = TransactionManager.getUserTransaction();
            TransactionManager.begin(utx);
            if (listenerEntity != null) {
                DynamicEntityModel.deleteEntity(listenerEntity);
            }
            listenerEntity = DynamicEntityModel.createDynamicEntity(CommandListener.ENTITY_NAME_COMMAND_LISTENER);
            listenerEntity.put(CommandListener.LISTENER_NAME, listenerName);
            listenerEntity.put(CommandListener.STATUS, CommandConstants.STATUS_UP);
            listenerEntity.put(CommandListener.LAST_STARTED_ON, DateTime.now());

            try {
                listenerEntity.put(CommandListener.HOST_NAME, InetAddress.getLocalHost().getHostName());
                listenerEntity.put(CommandListener.HOST_ADDR, InetAddress.getLocalHost().getHostAddress());
            } catch (final UnknownHostException e) {
                LOGGER.error("01070037", "UnknownHostException. Unable to get host name and address", e);
            }
            DynamicEntityModel.saveEntity(listenerEntity);
            this.listenerEntity = listenerEntity;
            this.listenerEntity.put(SETTING_DOMAINS, domainIds);
            TransactionManager.commit(utx);
        } catch (final Exception e) {
            TransactionManager.rollback(utx);
            LOGGER.error("01070038", "acquireListenerLock exception.", e);
            throw new DataException("01070038", "acquireListenerLock exception.", e);
        }

        // start the heartbeat thread for command listener
        LOGGER.info("Start heartbeat thread for listener: " + listenerName + "[id: " + listenerEntity.getId() + "]");
        new HeartbeatThread(listenerEntity).start();
        LOGGER.info("Listener started: " + listenerName);
        return true;
    }

    @SuppressWarnings("unchecked")
    protected void startRouter() throws DataException {
        // Step 1: check if there are routers running
        LOGGER.info("Starting router");
        final Criterion criterion = new Criterion(CommandListener.ENTITY_NAME_COMMAND_ROUTER);
        criterion.addRestriction(Restriction.eq(EntityConstants.PTY_ID, COMMAND_ROUTER_ID));
        DynamicEntity routerEntity = DynamicEntityModel.findUniqueBy(criterion, false);
        if (routerEntity != null) {
            final Long status = (Long) routerEntity.get(CommandListener.STATUS);
            final DateTime lastHeartbeat = (DateTime) routerEntity.get(CommandListener.LAST_HEARTBEAT);
            final long heartbeatInterval = CommandQueueModel.getSystemHeartbeatInterval();
            final DateTime now = DateTime.now();
            LOGGER.info("Checking router DB status");
            LOGGER.info("DB status: " + status);
            LOGGER.info("DB last heartbeat: " + lastHeartbeat);
            LOGGER.info("now: " + now);
            if (lastHeartbeat != null) {
                LOGGER.info("lastHeartbeat.numSecondsFrom(now): " + lastHeartbeat.numSecondsFrom(now));
            }
            if (CommandConstants.STATUS_UP == status && lastHeartbeat != null
                    && lastHeartbeat.numSecondsFrom(now) < heartbeatInterval) {
                LOGGER.warn("01070031", "There is another command router instance is running.");
                return;
            }
        }

        // Step 2: If router is down or heartbeat expired, create a new router.
        UserTransaction utx = null;
        try {
            utx = TransactionManager.getUserTransaction();
            TransactionManager.begin(utx);
            if (routerEntity != null) {
                DynamicEntityModel.deleteEntity(routerEntity);
            }

            routerEntity = DynamicEntityModel.createDynamicEntity(CommandListener.ENTITY_NAME_COMMAND_ROUTER);
            routerEntity.put(EntityConstants.PTY_ID, COMMAND_ROUTER_ID);
            if (this.listenerEntity != null) {
                routerEntity.put(CommandListener.LISTENER_NAME, this.listenerEntity.get(CommandListener.LISTENER_NAME));
            }
            routerEntity.put(CommandListener.STATUS, CommandConstants.STATUS_UP);
            routerEntity.put(CommandListener.ENABLE, 1);
            routerEntity.put(CommandListener.LAST_STARTED_ON, DateTime.now());
            try {
                routerEntity.put(CommandListener.HOST_NAME, InetAddress.getLocalHost().getHostName());
                routerEntity.put(CommandListener.HOST_ADDR, InetAddress.getLocalHost().getHostAddress());
            } catch (final UnknownHostException e) {
                LOGGER.error("01070037", "UnknownHostException.", e);
            }

            DynamicEntityModel.saveEntity(routerEntity);
            TransactionManager.commit(utx);
        } catch (final Exception e) {
            TransactionManager.rollback(utx);
            LOGGER.error("01070039", "startRouter exception.", e);
            throw new DataException("01070039", "startRouter exception.", e);
        }

        // Step 3: After successfully got the lock (inserted the router record to DB),
        // Create the router JMS listener
        LOGGER.info("Acquired the router lock");
        try {
            utx = TransactionManager.getUserTransaction();
            TransactionManager.begin(utx);

            this.routerHandler = new RouterHandler();
            this.routerHandler.init(listenerEntity, routerEntity);
            TransactionManager.commit(utx);
        } catch (final Exception e) {
            TransactionManager.rollback(utx);
            LOGGER.error("01070039", "startRouter exception.", e);
            throw new DataException("01070039", "startRouter exception.", e);
        }

        List<String> settingDomain = null;
        if (listenerEntity != null) {
            settingDomain = (List<String>) listenerEntity.get(SETTING_DOMAINS);
        }
        final CommandMessageListener commandMessageListener = new CommandMessageListener(routerHandler,
                RouterHandler.QUEUE_ALIAS, settingDomain);
        CommandQueueModel.startCommandMessageListener(commandMessageListener);
        LOGGER.info("Router started");
    }

    @SuppressWarnings("unchecked")
    protected void startHandlers() throws DataException {
        if (this.listenerHandlers != null) {
            UserTransaction utx = null;
            try {
                utx = TransactionManager.getUserTransaction();
                TransactionManager.begin(utx);

                for (final Entry<String, CommandHandler> entry : this.listenerHandlers.entrySet()) {
                    final String key = entry.getKey();
                    final CommandHandler commandHandler = entry.getValue();
                    LOGGER.info("Init command handler: " + key);
                    final DynamicEntity handlerEntity = handlerEntitys.get(key);
                    commandHandler.init(listenerEntity, handlerEntity);
                }

                TransactionManager.commit(utx);
            } catch (final Exception e) {
                TransactionManager.rollback(utx);
                LOGGER.error("01070039", "startRouter exception.", e);
                throw new DataException("01070039", "startRouter exception.", e);
            }

            List<String> settingDomain = null;
            if (listenerEntity != null) {
                settingDomain = (List<String>) listenerEntity.get(SETTING_DOMAINS);
            }
            for (final Entry<String, CommandHandler> entry : this.listenerHandlers.entrySet()) {
                final CommandHandler commandHandler = entry.getValue();
                final String key = entry.getKey();
                final DynamicEntity handlerEntity = handlerEntitys.get(key);
                final CommandMessageListener commandMessageListener = new CommandMessageListener(commandHandler,
                        ObjectUtils.toString(handlerEntity.get(CommandListener.COMMAND_TYPE)), settingDomain);
                LOGGER.debug("Init JMS listener form command handler: " + key);
                CommandQueueModel.startCommandMessageListener(commandMessageListener);
            }
        }
    }
}
