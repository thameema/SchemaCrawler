SELECT
  NULLIF(1, 1) AS CONSTRAINT_CATALOG,
  STRIP(SYSIBM.SYSKEYCOLUSE.TBCREATOR) AS CONSTRAINT_SCHEMA,
  STRIP(SYSIBM.SYSKEYCOLUSE.CONSTNAME) AS CONSTRAINT_NAME,
  NULLIF(1, 1) AS TABLE_CATALOG,
  STRIP(SYSIBM.SYSKEYCOLUSE.TBCREATOR) AS TABLE_SCHEMA,
  STRIP(SYSIBM.SYSKEYCOLUSE.TBNAME) AS TABLE_NAME,
  STRIP(SYSIBM.SYSKEYCOLUSE.COLNAME) AS COLUMN_NAME,
  STRIP(SYSIBM.SYSKEYCOLUSE.COLSEQ) AS ORDINAL_POSITION
FROM
  SYSIBM.SYSKEYCOLUSE
UNION
SELECT
  NULLIF(1, 1) AS CONSTRAINT_CATALOG,
  STRIP(SYSIBM.SYSCHECKDEP.TBOWNER) AS CONSTRAINT_SCHEMA,
  STRIP(SYSIBM.SYSCHECKDEP.CHECKNAME) AS CONSTRAINT_NAME,
  NULLIF(1, 1) AS TABLE_CATALOG,
  STRIP(SYSIBM.SYSCHECKDEP.TBOWNER) AS TABLE_SCHEMA,
  STRIP(SYSIBM.SYSCHECKDEP.TBNAME) AS TABLE_NAME,
  STRIP(SYSIBM.SYSCHECKDEP.COLNAME) AS COLUMN_NAME,
  '1' AS ORDINAL_POSITION
FROM
  SYSIBM.SYSCHECKDEP
WITH UR


