SELECT
  NULLIF(1, 1) AS CONSTRAINT_CATALOG,
  STRIP(SYSIBM.SYSCHECKS.CREATOR) AS CONSTRAINT_SCHEMA,
  STRIP(SYSIBM.SYSCHECKS.CHECKNAME) AS CONSTRAINT_NAME,
  SYSIBM.SYSCHECKS.CHECKCONDITION AS CHECK_CLAUSE
FROM
  SYSIBM.SYSCHECKS
ORDER BY
  SYSIBM.SYSCHECKS.CREATOR,
  SYSIBM.SYSCHECKS.CHECKNAME
WITH UR
