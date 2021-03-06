SELECT
  NULLIF(1, 1) AS SYNONYM_CATALOG,
  LTRIM(RTRIM(CREATOR)) AS SYNONYM_SCHEMA,
  LTRIM(RTRIM(NAME)) AS SYNONYM_NAME,
  NULLIF(1, 1) AS REFERENCED_OBJECT_CATALOG,
  LTRIM(RTRIM(TBCREATOR)) AS REFERENCED_OBJECT_SCHEMA,
  LTRIM(RTRIM(TBNAME)) AS REFERENCED_OBJECT_NAME
FROM
  SYSIBM.SYSTABLES
WHERE
  TYPE = 'A'
WITH UR
