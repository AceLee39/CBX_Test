echo off
setlocal
set MAVEN_DEBUG_OPTS=-Xdebug -Xmx4096M -XX:PermSize=128M -XX:MaxPermSize=512M -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=4000,server=y,suspend=n
mvn jetty:run -Pdev,jettyLog -Dorg.eclipse.jetty.annotations.maxWait=240
endlocal
