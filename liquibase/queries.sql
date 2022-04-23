SELECT t0.ID,
       t0."TYPE",
       t0.CREATION_DATE,
       t0.UPDATE_DATE,
       t0."ORDER",
       t0.PARENT_ID,
       t1.NAME,
       t1."VALUE",
FROM ACTION t0
         INNER JOIN ACTION_SETTINGS t1 ON
    t0.ID = t1.ID;

SELECT "TYPE", sum(1) as "COUNT"
FROM ACTION GROUP BY "TYPE" ORDER BY 2;



