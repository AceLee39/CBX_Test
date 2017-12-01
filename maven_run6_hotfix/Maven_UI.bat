echo off
copy /y ".\settings\pom\ui_pom.xml" "..\..\CBX_UI\pom.xml"
cd "..\..\CBX_UI"
mvn compile install -DskipTests=true