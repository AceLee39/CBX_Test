echo off
copy /y pgsql-generator.properties "C:\Users\ace.li\CBX\WorkSpaces\CBXB\settings"
cd "C:\Users\ace.li\CBX\WorkSpaces\CBXB"
run-pgsql.cmd
copy /y "C:\Users\ace.li\CBX\WorkSpaces\CBXB\output" "C:\Users\ace.li\CBX\WorkSpaces\CBX_Test\cbxb_run"
