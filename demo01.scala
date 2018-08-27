// Databricks notebook source
// DBFS를 사용하여 저장소 계정 탑재 (AZURE에서 권장사항임.)
// Azure Storage 계정 경로는 /mnt/dir~ 로 탑재할 수 있다는 것은 Blob Storage의 모든 경로를 엑세스 하지 않아도 된다라는 것. 
// 즉, /mnt/dir~이후의 디렉토리 사용만 하면 됨.

dbutils.fs.mount(
  source = "wasbs://dbk@dbkstorage.blob.core.windows.net/",
  mountPoint = "/mnt/dbk",
  extraConfigs = Map("fs.azure.account.key.dbkstorage.blob.core.windows.net" -> "+eD+HpUdpCDWqIc1LnSL8rhQ5VZ120RRzhplUwF/SHGfF0cqqVpPY7fLbvwPEPRB0gkUCMmYKFa8e38j4ei43w=="))

// COMMAND ----------

// sample data인 json파일을 불러오기 .

%sql 
CREATE TABLE radio_sample_data
USING json
OPTIONS (
 path "/mnt/dbk/small_radio_json.json"
)

// COMMAND ----------


