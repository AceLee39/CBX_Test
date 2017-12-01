echo off
copy /y jettyenv.xml "..\..\CBX_Business_review\settings"
copy /y log4j.xml "..\..\CBX_Business_review\src\main\resources"
copy /y jetty-debug.cmd "..\..\CBX_Business_review"
copy /y DBSetting.properties "..\..\CBX_General_review\src\main\resources"
del  /q /a /f /s "..\..\CBX_General_review\logs"
del  /q /a /f /s "..\..\CBX_General_review\build\test-classes\mapper"
del  /q /a /f /s "..\..\CBX_Business_review\src\main\java\com\core\cbx\data\constants\Attachment.java"

copy /y zk.xml "..\..\CBX_Business_review\src\main\webapp\WEB-INF"
copy /y SimpleIdGenerator.java "..\..\CBX_Business_review\src\main\java"
echo copy /y log4j.properties "..\..\CBX_Business_review\src\main\resources"
cd "..\..\CBX_Business_review"
jetty-debug.cmd