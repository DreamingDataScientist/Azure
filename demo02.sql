-- Databricks notebook source
-- sample data인 json파일을 불러오기 .

CREATE TABLE radio_sample_data
USING json
OPTIONS (
 path "/mnt/dbk/small_radio_json.json"
)

-- COMMAND ----------

SELECT * FROM radio_sample_data;

-- COMMAND ----------

select * from radio_sample_data where gender == 'M' and level == 'paid';
