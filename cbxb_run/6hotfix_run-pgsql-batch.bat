echo off
copy /y 6hotfix_pgsql-generator.properties "C:\Users\ace.li\CBX\WorkSpaces\CBXB\settings\pgsql-generator.properties"
copy /y pgsql-build.xml "C:\Users\ace.li\CBX\WorkSpaces\CBXB"
cd "C:\Users\ace.li\CBX\WorkSpaces\CBXB"
call "C:\Users\ace.li\CBX\DeveloperTool\apache-ant-1.8.1/bin/ant.bat" -lib lib -buildfile pgsql-build.xml  genBatch

