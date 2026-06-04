-- Borra el modelo MENT_* completo (tablas + SEQ_MENT_MSG).
-- Perfil: local-extranet. Ejecutar antes de create_ment_model.sql si se recrea el esquema.
--   run_sqlcl.sh --profile local-extranet .../delete_ment_model.sql

SET ECHO ON
SET FEEDBACK ON
SET SERVEROUTPUT ON
SET SQLBLANKLINES ON

WHENEVER SQLERROR EXIT SQL.SQLCODE

PROMPT === Eliminando modelo MENT_* ===

BEGIN
  FOR t IN (
    SELECT table_name
      FROM user_tables
     WHERE table_name IN (
           'MENT_TRY_PUSH_MSG',
           'MENT_PARAM_PUSH_MSG',
           'MENT_ADDRESSEE_PUSH_MSG',
           'MENT_ATTACHMENT_PUSH_MSG',
           'MENT_PUSH_MSG',
           'MENT_MENSAJE',
           'MENT_CHANNEL'
         )
     ORDER BY DECODE(table_name,
           'MENT_TRY_PUSH_MSG', 1,
           'MENT_PARAM_PUSH_MSG', 2,
           'MENT_ADDRESSEE_PUSH_MSG', 3,
           'MENT_ATTACHMENT_PUSH_MSG', 4,
           'MENT_PUSH_MSG', 5,
           'MENT_MENSAJE', 6,
           'MENT_CHANNEL', 7,
           99)
  ) LOOP
    EXECUTE IMMEDIATE 'DROP TABLE ' || t.table_name || ' CASCADE CONSTRAINTS PURGE';
    DBMS_OUTPUT.PUT_LINE('Dropped table ' || t.table_name);
  END LOOP;
END;
/

BEGIN
  EXECUTE IMMEDIATE 'DROP SEQUENCE SEQ_MENT_MSG';
  DBMS_OUTPUT.PUT_LINE('Dropped sequence SEQ_MENT_MSG');
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -2289 THEN
      RAISE;
    END IF;
END;
/

PROMPT === Modelo MENT_* eliminado ===

EXIT
