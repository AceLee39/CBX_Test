echo off
setlocal
set ANT_OPTS=-Xdebug -Xmx2024m -XX:MaxPermSize=256M -Djava.rmi.server.hostname=0.0.0.0 -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9091 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=4000,server=y,suspend=n
ant jetty.run 2> error.log
endlocal