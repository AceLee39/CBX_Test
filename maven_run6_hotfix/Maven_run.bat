echo off
copy /y ".\settings\jetty9env.xml" "C:\Users\ace.li\CBX\WorkSpaces6_hotfix\CBX_Business6_hotfix\settings\jetty9env.xml"
rem copy /y ".\settings\jetty-mvn-debug.cmd" "..\..\CBX_Business"


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
del  /q /a /f /s "C:\Users\ace.li\CBX\WorkSpaces6_hotfix\CBX_Business6_hotfix\logs"

cd "C:\Users\ace.li\CBX\WorkSpaces6_hotfix\CBX_Business6_hotfix"
jetty-debug.cmd