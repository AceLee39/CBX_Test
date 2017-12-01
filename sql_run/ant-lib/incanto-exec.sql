set pages 21 lines 80 term on echo off verify off feedb on
whenever oserror exit failure
whenever sqlerror exit sql.sqlcode

-- Use the propertyset for substitution:
--     cbx.db.user->cbx_db_user, cbx.db.pwd->cbx_db_pass, cbx.db.local->cbx_db_local
-- e.g.
-- connect &&cbx_db_user/&&cbx_db_pwd@&&cbx_db_local

spool "&2" append

start "&1"
