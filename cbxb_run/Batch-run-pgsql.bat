echo off
copy /y pgsql-generator.properties "C:\Users\ace.li\CBX\WorkSpaces\CBXB\settings"
copy /y pgsql-build.xml "C:\Users\ace.li\CBX\WorkSpaces\CBXB"
cd "C:\Users\ace.li\CBX\WorkSpaces\CBXB"
call "C:\Users\ace.li\CBX\DeveloperTool\apache-ant-1.8.1/bin/ant.bat" -lib lib -buildfile pgsql-build.xml genBatch
