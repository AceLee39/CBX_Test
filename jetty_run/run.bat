echo off
copy /y jettyenv.xml "..\..\CBX_Business\settings"
copy /y log4j.xml "..\..\CBX_Business\src\main\resources"
copy /y jetty-debug.cmd "..\..\CBX_Business"
copy /y DBSetting.properties "..\..\CBX_General\src\main\resources"
copy /y ListenerManager.java "..\..\CBX_Core\src\main\java\com\core\cbx\command"

del  /q /a /f /s "..\..\CBX_General\logs"
del  /q /a /f /s "..\..\CBX_General\build\test-classes\mapper"
del  /q /a /f /s "..\..\CBX_Business\src\main\java\com\core\cbx\data\constants\Attachment.java"

copy /y zk.xml "..\..\CBX_Business\src\main\webapp\WEB-INF"
copy /y SimpleIdGenerator.java "..\..\CBX_Business\src\main\java"
echo copy /y log4j.properties "..\..\CBX_Business\src\main\resources"
cd "..\..\CBX_Business"
jetty-debug.cmd