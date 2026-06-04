-- Llenado de MENT_CHANNEL y MENT_MENSAJE desde BSV-CALIDAD (database link).
-- Perfil: local-extranet. Requiere red hacia 192.168.229.156:1521/SEGU.
SET ECHO ON
SET FEEDBACK ON
SET SERVEROUTPUT ON
WHENEVER SQLERROR EXIT SQL.SQLCODE

PROMPT === Creando database link BSV_CALIDAD (si no existe) ===
BEGIN
  EXECUTE IMMEDIATE 'DROP DATABASE LINK BSV_CALIDAD';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -2024 THEN
      RAISE;
    END IF;
END;
/

CREATE DATABASE LINK BSV_CALIDAD
  CONNECT TO extranet IDENTIFIED BY extranet
  USING '(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=192.168.229.156)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=SEGU)))';

PROMPT === Limpiando tablas locales ===
DELETE FROM MENT_TRY_PUSH_MSG;
DELETE FROM MENT_PARAM_PUSH_MSG;
DELETE FROM MENT_ADDRESSEE_PUSH_MSG;
DELETE FROM MENT_ATTACHMENT_PUSH_MSG;
DELETE FROM MENT_PUSH_MSG;
DELETE FROM MENT_MENSAJE;
DELETE FROM MENT_CHANNEL;
COMMIT;

PROMPT === Copiando MENT_CHANNEL ===
INSERT INTO MENT_CHANNEL (
  MECH_NU_CHANNEL, MECH_URL_SERVER, MECH_USER_SERVER, MECH_CLASS, MECH_DEFAULT_TO
)
SELECT MECH_NU_CHANNEL, MECH_URL_SERVER, MECH_USER_SERVER, MECH_CLASS, MECH_DEFAULT_TO
  FROM MENT_CHANNEL@BSV_CALIDAD;

PROMPT === Copiando MENT_MENSAJE (LONG via PL/SQL) ===
DECLARE
  v_cuerpo LONG;
  v_cnt    NUMBER := 0;
BEGIN
  FOR r IN (
    SELECT meme_meca_nu_campana, meme_nu_mensaje, meme_tx_titulo,
           meme_nu_dias_vig, meme_status, meme_fech_status,
           meme_nu_intentos, meme_cd_channel, meme_time_next
      FROM ment_mensaje@BSV_CALIDAD
     ORDER BY meme_meca_nu_campana, meme_nu_mensaje
  ) LOOP
    SELECT meme_tx_cuerpo
      INTO v_cuerpo
      FROM ment_mensaje@BSV_CALIDAD
     WHERE meme_meca_nu_campana = r.meme_meca_nu_campana
       AND meme_nu_mensaje = r.meme_nu_mensaje;

    INSERT INTO ment_mensaje (
      meme_meca_nu_campana, meme_nu_mensaje, meme_tx_titulo, meme_tx_cuerpo,
      meme_nu_dias_vig, meme_status, meme_fech_status, meme_nu_intentos,
      meme_cd_channel, meme_time_next
    ) VALUES (
      r.meme_meca_nu_campana, r.meme_nu_mensaje, r.meme_tx_titulo, v_cuerpo,
      r.meme_nu_dias_vig, r.meme_status, r.meme_fech_status, r.meme_nu_intentos,
      r.meme_cd_channel, r.meme_time_next
    );
    v_cnt := v_cnt + 1;
  END LOOP;
  DBMS_OUTPUT.PUT_LINE('MENT_MENSAJE copiados: ' || v_cnt);
END;
/

COMMIT;

PROMPT === Conteos locales ===
SELECT 'MENT_CHANNEL' AS tabla, COUNT(*) AS filas FROM MENT_CHANNEL
UNION ALL
SELECT 'MENT_MENSAJE', COUNT(*) FROM MENT_MENSAJE;

PROMPT === Conteos en origen (BSV-CALIDAD) ===
SELECT 'MENT_CHANNEL' AS tabla, COUNT(*) AS filas FROM MENT_CHANNEL@BSV_CALIDAD
UNION ALL
SELECT 'MENT_MENSAJE', COUNT(*) FROM MENT_MENSAJE@BSV_CALIDAD;

EXIT
