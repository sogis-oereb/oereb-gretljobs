SELECT t_id AS t_id, -1::int8 AS t_basket, 'dm01vch24lv95dliegenschaften_liegenschaft'::varchar(60) AS t_type, NULL::varchar(200) AS t_ili_tid,
  nummerteilgrundstueck, geometrie, flaechenmass, liegenschaft_von AS liegenschaft_von
FROM agi_dm01avso24.liegenschaften_liegenschaft;
