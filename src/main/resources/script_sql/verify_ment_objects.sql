-- Verifica que el modelo MENT_* de ibMsg está completo (tablas + SEQ_MENT_MSG).
-- Perfil referencia: BSV-CALIDAD (o local-extranet tras create/load).
--   run_sqlcl.sh --credentials .../credentials.json --profile BSV-CALIDAD \
--     src/main/resources/script_sql/verify_ment_objects.sql target/verify_ment.log

SET ECHO ON
SET FEEDBACK ON
SET PAGESIZE 200
SET LINESIZE 200
SET SQLBLANKLINES ON
WHENEVER SQLERROR EXIT SQL.SQLCODE

PROMPT === Usuario conectado ===
SELECT USER AS connected_user, SYS_CONTEXT('USERENV','DB_NAME') AS db_name FROM dual;

PROMPT === Tablas MENT_% (owner EXTRANET) ===
SELECT table_name, num_rows, last_analyzed
  FROM all_tables
 WHERE owner = 'EXTRANET'
   AND table_name LIKE 'MENT_%'
 ORDER BY table_name;

PROMPT === Secuencias MENT_% / SEQ_MENT_% ===
SELECT sequence_name, min_value, max_value, increment_by, last_number
  FROM all_sequences
 WHERE sequence_owner = 'EXTRANET'
   AND (sequence_name LIKE 'MENT_%' OR sequence_name LIKE 'SEQ_MENT%')
 ORDER BY sequence_name;

PROMPT === Objetos esperados por ibMsg (tablas) ===
WITH expected AS (
  SELECT 'MENT_PUSH_MSG' AS obj_name FROM dual UNION ALL
  SELECT 'MENT_TRY_PUSH_MSG' FROM dual UNION ALL
  SELECT 'MENT_PARAM_PUSH_MSG' FROM dual UNION ALL
  SELECT 'MENT_ADDRESSEE_PUSH_MSG' FROM dual UNION ALL
  SELECT 'MENT_ATTACHMENT_PUSH_MSG' FROM dual UNION ALL
  SELECT 'MENT_MENSAJE' FROM dual UNION ALL
  SELECT 'MENT_CHANNEL' FROM dual
)
SELECT e.obj_name,
       CASE WHEN t.table_name IS NOT NULL THEN 'OK' ELSE 'FALTA' END AS estado
  FROM expected e
  LEFT JOIN all_tables t
    ON t.owner = 'EXTRANET' AND t.table_name = e.obj_name
 ORDER BY e.obj_name;

PROMPT === Secuencia SEQ_MENT_MSG ===
SELECT CASE WHEN COUNT(*) > 0 THEN 'OK' ELSE 'FALTA' END AS seq_ment_msg
  FROM all_sequences
 WHERE sequence_owner = 'EXTRANET' AND sequence_name = 'SEQ_MENT_MSG';

PROMPT === Columnas tablas nucleo (push/cola) ===
SELECT table_name, column_name, data_type, data_length, nullable
  FROM all_tab_columns
 WHERE owner = 'EXTRANET'
   AND table_name IN (
         'MENT_PUSH_MSG','MENT_TRY_PUSH_MSG','MENT_PARAM_PUSH_MSG',
         'MENT_ADDRESSEE_PUSH_MSG','MENT_ATTACHMENT_PUSH_MSG'
       )
 ORDER BY table_name, column_id;

PROMPT === Columnas MENT_MENSAJE y MENT_CHANNEL ===
SELECT table_name, column_name, data_type, data_length, nullable
  FROM all_tab_columns
 WHERE owner = 'EXTRANET'
   AND table_name IN ('MENT_MENSAJE','MENT_CHANNEL')
 ORDER BY table_name, column_id;

PROMPT === Conteos de filas ===
SELECT COUNT(*) AS total_push FROM extranet.ment_push_msg;
SELECT COUNT(*) AS total_try FROM extranet.ment_try_push_msg;
SELECT COUNT(*) AS total_mensaje FROM extranet.ment_mensaje;
SELECT COUNT(*) AS total_channel FROM extranet.ment_channel;

EXIT
