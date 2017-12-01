package com.core.cbx.importer;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.core.cbx.action.ActionDispatcher;
import com.core.cbx.action.actionContext.AsyncImportRawData;
import com.core.cbx.action.exception.ActionException;
import com.core.cbx.conf.exception.ConfigException;
import com.core.cbx.security.AuthenticationUtil;
import com.core.cbx.startup.BaseStartUp;

public class ExcelRawDataImporterTest2
{
  private static boolean initSprintSecurity = false;

  private static final Log log = LogFactory.getLog(ExcelRawDataImporterTest2.class);

  static
  {
    try
    {
      BaseStartUp.run();
      initSpringSecurity();
    }
    catch (final ConfigException e) {
      throw new RuntimeException("Init base start up failure", e);
    }
  }

  protected static void initSpringSecurity()
  {
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

  public static void main(final String[] args)
    throws Exception
  {
    if (args.length < 4) {
      log.error("Please add arguments as: <login_user> <pwd> <module>  <filepath|folderpath> <duplicatedTimes>");
      return;
    }
    final String username = args[0];
    final String pwd = args[1];
    final String module = args[2];
    final String filePath = args[3];
    final int duplicatedTimes = Integer.valueOf(args[4]).intValue();

    log.info("arg: username>>" + username + ", pwd>>" + pwd + ", module>>" + module + ", filepath>>" + filePath);
    try {
      AuthenticationUtil.login(username, pwd);

      for (int x = 0; x < duplicatedTimes; x++)
      {
        log.info("==================================================");
        log.info("==================DuplicatedTimes:" + x + "===========");
        log.info("==================================================");

        final File file = new File(filePath);
        if (!file.exists()) {
          log.error(">>>filePath not exists");
          return;
        }

        if (!file.isDirectory())
        {
          processOneFile(module, file);

          log.info("Finished!>>>" + filePath);
          log.debug(">>>>>>>>>>>" + filePath);
        }
        else
        {
          final File[] fs = file.listFiles();
          for (int i = 0; i < fs.length; i++) {
            final File fx = fs[i];
            processOneFile(module, fx);
            log.info("Finished!>>>" + i + ">>" + fx.getAbsolutePath());
            log.debug(">>>>>>>>>>>" + i + ">>" + fx.getAbsolutePath());
          }
        }
      }
    }
    catch (final Throwable e)
    {
      e.printStackTrace();
      log.error("ERROR", e);
    } finally {
      log.info("Generation End ! ");
      System.exit(-1);
    }
  }

  private static void processOneFile(final String module, final File file)
    throws ActionException
  {
    final String fileType = "EXCEL";
    final String progressId = null;

    final AsyncImportRawData context = new AsyncImportRawData(file,
      "EXCEL", module, progressId,
      file.getName());
    try {
      ActionDispatcher.execute(context);
    } catch (final Exception e) {
      log.error("AsyncImportRawData result for:" + file.getName() + ">>>" + context.getLogMessage(), e);
    }
    log.info("AsyncImportRawData result for:" + file.getName() + ">>>" + context.getResultValue(""));
  }
}
