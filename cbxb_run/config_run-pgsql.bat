echo off
copy /y pgsql-generator.properties "..\..\CBXB\settings"
copy /y pgsql-build_config.xml "..\..\CBXB\pgsql-build.xml"
cd "..\..\CBXB"
call "ant.bat" -lib lib -buildfile pgsql-build.xml gen-system-config
