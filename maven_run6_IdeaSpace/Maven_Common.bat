echo off
copy /y ".\settings\pom\common_pom.xml" "..\..\CBX_Common\pom.xml"
cd "..\..\CBX_Common"
mvn compile install -DskipTests=true