echo off
rem copy /y ".\settings\ListenerManager.java" "..\..\CBX_Core\src\main\java\com\core\cbx\command"
rem copy /y ".\settings\pom\core_pom.xml" "..\..\CBX_Core\pom.xml"
cd "..\..\CBX_Core"
mvn compile install -DskipTests=true