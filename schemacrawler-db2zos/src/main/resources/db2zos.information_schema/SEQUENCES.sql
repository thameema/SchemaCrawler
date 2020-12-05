SELECT
  NULLIF(1, 1)
    AS SEQUENCE_CATALOG,
  STRIP(SYSIBM.SYSSEQUENCES.SCHEMA)
    AS SEQUENCE_SCHEMA,
  STRIP(SYSIBM.SYSSEQUENCES.NAME)
    AS SEQUENCE_NAME,
  INCREMENT,
  START AS START_VALUE,
  MINVALUE AS MINIMUM_VALUE,
  MAXVALUE AS MAXIMUM_VALUE,
  CASE WHEN CYCLE = 'Y' THEN 'YES' ELSE 'NO' END AS CYCLE_OPTION,
  SEQUENCEID,
  SEQTYPE,
  MAXASSIGNEDVAL AS NEXTCACHEFIRSTVALUE,
  CACHE,
  ORDER,
  CREATEDTS AS CREATE_TIME,
  ALTEREDTS AS ALTER_TIME,
  REMARKS
FROM
  SYSIBM.SYSSEQUENCES
WHERE
  SYSIBM.SYSSEQUENCES.SEQTYPE = 'S'
ORDER BY
  SYSIBM.SYSSEQUENCES.SCHEMA,
  SYSIBM.SYSSEQUENCES.NAME
WITH UR