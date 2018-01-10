echo off
SET domainId=%~1

	if "%domainId%"=="" (
		set domainId=/
	)
copy /y genPGSQL_RefDataSql.cmd "C:\Users\ace.li\CBX\WorkSpaces\CBXB"
copy /y pgsql-generator.properties "C:\Users\ace.li\CBX\WorkSpaces\CBXB\settings"
copy /y pgsql-build.xml "C:\Users\ace.li\CBX\WorkSpaces\CBXB\pgsql-build.xml"
cd "C:\Users\ace.li\CBX\WorkSpaces\CBXB"
genPGSQL_RefDataSql.cmd %domainId%
copy /y "C:\Users\ace.li\CBX\WorkSpaces\CBXB\output" "C:\Users\ace.li\CBX\WorkSpaces\CBX_Test\cbxb_run"
