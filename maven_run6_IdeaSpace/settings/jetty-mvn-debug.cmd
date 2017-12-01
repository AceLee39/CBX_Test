echo off
setlocal
set MAVEN_OPTS=-Dorg.eclipse.jetty.annotations.maxWait=120 -Xdebug -Xmx2048m -XX:MaxPermSize=256M -Djava.rmi.server.hostname=0.0.0.0 -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9091 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=4000,server=y,suspend=n
mvn compile jetty:run -Pdev,jettyLog
endlocal
