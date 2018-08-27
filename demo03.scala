// Databricks notebook source
// Data Lake Storage 연결을 위해 Azure Active Directory 서비스 사용자에 대해 지정 값으로 바꾸기
spark.conf.set("dfs.adls.oauth2.access.token.provider.type", "ClientCredential")
 spark.conf.set("dfs.adls.oauth2.client.id", "c52c8cc5-7e66-4378-ac79-f00ec41dc0f7") //Application ID - Active Directory에 app registrations에 Application ID있음.
 spark.conf.set("dfs.adls.oauth2.credential", "+ZmhnmvnYOnDARg03z1QjllCawlBuMtPfsQX22kedNo=") // App registration에 datalakestorage와 연결한 settings keys의 value값(초기만 볼 수 있음)
 spark.conf.set("dfs.adls.oauth2.refresh.url", "https://login.microsoftonline.com/d646b43b-7348-4200-b89c-086fc797aab3/oauth2/token")//Tenant-ID(Directory ID) - Properties안에 있음

// COMMAND ----------

// Data Lake Storage에 있는 small_radio_json.json 파일을 databricks spark cluster로 추출하기.
val df = spark.read.json("adl://dbkmj.azuredatalakestore.net/mystorefordatabricks/small_radio_json.json")

// COMMAND ----------

df.show()

// COMMAND ----------

val specificColumns = df.select("firstname", "lastname", "gender", "location", "level")
specificColumns.show()

// COMMAND ----------

// levle column 명을 level이었는 명을 subscription_type으로 바꾸기.
val renamedColumn = specificColumns.withColumnRenamed("level", "subscription_type") 
renamedColumn.show()

// COMMAND ----------

//scala null값 삭제하기
val renamedColumn2 = renamedColumn.na.drop()
renamedColumn2.show(20)

// COMMAND ----------

renamedColumn2.describe()

// COMMAND ----------

// Azure Databricks에서 Azure Storage 계정에 액세스 하기 위한 구성을 입력.
val blobStorage = "dbkstorage.blob.core.windows.net"
val blobContainer = "dbk"
val blobAccessKey= "+eD+HpUdpCDWqIc1LnSL8rhQ5VZ120RRzhplUwF/SHGfF0cqqVpPY7fLbvwPEPRB0gkUCMmYKFa8e38j4ei43w=="

// COMMAND ----------

// Azure Databricks와 Azure SQL Data Warehouse 간에 데이터 이동하는데 사용되는 임시 폴더를 지정
//val tempDir = "wasbs://" + blobContainer + "@" + blobStorage + "/tempdir"
val tempDir = "wasbs://dbk@dbkstorage.blob.core.windows.net/tempdir"

// COMMAND ----------

// Azure Blob Storage 액세스 키를 구성에 저장.
val acntInfo = "fs.azure.account.key.dbkstorage.blob.core.windows.net" 
sc.hadoopConfiguration.set(acntInfo, blobAccessKey)

// COMMAND ----------

// Azure SQL Data Warehouse 인스턴스에 연결하기 위한 값 셋팅
val sqlDwUrl = "jdbc:sqlserver://datawhserver.database.windows.net:1433;database=datawh;user=minji@datawhserver;password=QWEqwe123!;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;"
val sqlDwUrlSmall = "jdbc:sqlserver://" + dwServer + ".database.windows.net:" + dwJdbcPort + ";database=" + dwDatabase + ";user=" + dwUser+";password=" + dwPass

// COMMAND ----------

// 위에서 변환한 DF인 renamedColumnsDf를 SQL 데이터 베이스 웨어하우스 테이블로 로드함.
// sampleTable이라는 테이블 생성 (스키마생성)
// Azure SQL DW에는 마스터 키가 필요함. SQL Server Management Studio에서 create master ket 명령을 실행하여 마스터 키를 만들 수 임ㅅ음.

spark.conf.set(
   "spark.sql.parquet.writeLegacyFormat",
   "true")

 renamedColumn.write
     .format("com.databricks.spark.sqldw")
     .option("url", sqlDwUrl) 
     .option("dbtable", "SampleTable")
     .option( "forward_spark_azure_storage_credentials","True")
     .option("tempdir", tempDir)
     .mode("overwrite")
     .save()

// COMMAND ----------

renamedColumn.show()
