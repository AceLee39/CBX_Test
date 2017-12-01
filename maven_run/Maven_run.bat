echo off
copy /y ".\settings\jetty9env.xml" "..\..\CBX_Business\settings"
copy /y ".\settings\jetty-mvn-debug.cmd" "..\..\CBX_Business"
copy /y ".\settings\log4j.xml" "..\..\CBX_Business\src\main\resources"
rem copy /y ".\settings\domain_log4j.properties" "..\..\CBX_Business\src\main\resources"
rem copy /y ".\settings\log4j.properties" "..\..\CBX_Business\src\main\resources"
rem copy /y ".\settings\zk.xml" "..\..\CBX_Business\src\main\webapp\WEB-INF"
rem copy /y ".\settings\SimpleIdGenerator.java" "..\..\CBX_Business\src\main\java"
rem del  /q /a /f /s "..\..\CBX_Business\src\main\java\com\core\cbx\data\constants\Attachment.java"
del  /q /a /f /s "..\..\CBX_Business\logs"
rem del  /q /a /f /s "..\..\CBX_Business\src\main\webapp\WEB-INF\lib\cbx*"

rem copy /y ".\settings\ListenerManager.java" "..\..\CBX_Core\src\main\java\com\core\cbx\command"

rem copy /y ".\settings\CommandMessageSchedulerHandler.java" "..\..\CBX_General\src\main\java\com\core\cbx\task\handler"
rem del  /q /a /f /s "..\..\CBX_General\src\test\resources\mapper"
rem del  /q /a /f /s "..\..\CBX_General\target\test-classes\mapper"

rem copy pom 
rem copy /y ".\settings\pom\biz_pom.xml" "..\..\CBX_Business\pom.xml"
rem copy /y ".\settings\pom\general_pom.xml" "..\..\CBX_General\pom.xml"
rem copy /y ".\settings\pom\ui_pom.xml" "..\..\CBX_UI\pom.xml"
rem copy /y ".\settings\pom\core_pom.xml" "..\..\CBX_Core\pom.xml"
rem copy /y ".\settings\pom\common_pom.xml" "..\..\CBX_Common\pom.xml"

rem copy /y ".\settings\DBSetting.properties" "..\resources"
rem copy /y ".\settings\log4j.xml" "..\resources"
rem copy /y ".\settings\domain_log4j.properties" "..\resources"
rem copy /y ".\settings\log4j.properties" "..\resources"
rem del  /q /a /f /s "..\logs"

cd "..\..\CBX_Business"
jetty-mvn-debug.cmd