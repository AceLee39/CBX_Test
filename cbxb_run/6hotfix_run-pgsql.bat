echo off
copy /y 6hotfix_pgsql-generator.properties "C:\Users\ace.li\CBX\WorkSpaces\CBXB\settings\pgsql-generator.properties"
cd "C:\Users\ace.li\CBX\WorkSpaces\CBXB"
run-pgsql.cmd
copy /y "C:\Users\ace.li\CBX\WorkSpaces\CBXB\output" "C:\Users\ace.li\CBX\WorkSpaces\CBX_Test\cbxb_run"
