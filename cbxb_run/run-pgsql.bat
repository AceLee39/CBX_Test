echo off
copy /y pgsql-generator.properties "C:\Users\ace.li\CBX\WorkSpaces\CBXB\settings"
copy /y pgsql-build.xml "C:\Users\ace.li\CBX\WorkSpaces\CBXB\pgsql-build.xml"
cd "C:\Users\ace.li\CBX\WorkSpaces\CBXB"
run-pgsql.cmd
copy /y "C:\Users\ace.li\CBX\WorkSpaces\CBXB\output" "C:\Users\ace.li\CBX\WorkSpaces\CBX_Test\cbxb_run"
