// Databricks notebook source
sc

// COMMAND ----------

// DBFS(Data Bricks File System)를 사용하여 저장소 계정 탑재 
// DBFS란 클러스터 파일 시스템 자체와 저장소가 연결됨. 클러스터에 엑세스하는 모든 응용 프로그램에서도 연결된 저장소를 사용할 수 있음.
// containername = "spark"
// storagteaccountname = "testmjhaha9354"
// accoujtkey = "HNnw0CdPGdTYBpk3L+zfdrtEoAcjQiHuJG2pChoFus5dsb7GaKkee8Qp5wx9nWElTiWnbk/H3yj0Vqgqc62Txw=="

dbutils.fs.mount(
 source = "wasbs://spark@testmjhaha9354.blob.core.windows.net/",
 mountPoint= "/mnt",
 extraConfigs = Map("fs.azure.account.key.testmjhaha9354.blob.core.windows.net" -> "HNnw0CdPGdTYBpk3L+zfdrtEoAcjQiHuJG2pChoFus5dsb7GaKkee8Qp5wx9nWElTiWnbk/H3yj0Vqgqc62Txw=="))

// COMMAND ----------

//RDD 형태 로드
val sc_tmp = sc.textFile("wasbs://spark@testmjhaha9354.blob.core.windows.net/mnt/PUBG_train (3).csv")

// spark.sql.DataFrame 형태 로드
val spark_tmp = spark.read.csv("wasbs://spark@testmjhaha9354.blob.core.windows.net/mnt/PUBG_train (3).csv")

sc_tmp.first()
spark_tmp.first()

// COMMAND ----------

//sql로 Table형태로 로드

%sql
create table pubg_data USING csv options (path "wasbs://spark@testmjhaha9354.blob.core.windows.net/mnt/PUBG_train (3).csv")

// COMMAND ----------

val pubg_rows = sc_tmp.map(x => x.split(","))
pubg_rows.first()

// map으로 열을 각 할당할 때에는 22개가 최대임 -> 번거로움..ㅠㅠ
// val pubg_df = pubg_rows.map(x => (x(0), x(1), x(2), x(3), x(4), x(5), x(6), x(7), x(8), x(9), x(10), x(11), x(12), x(13), x(14), x(15), x(16), x(17), x(18), x(19), x(20),x(21) , x(22), x(23), x(24), x(25)))
// val pubgdf = pubg_df.toDF()

val pubgRDD_df = pubg_rows.map(x => (x(0), x(1), x(2), x(3), x(4), x(5), x(6), x(7), x(8), x(9), x(10), x(11), x(12), x(13), x(14), x(15), x(16), x(17), x(18), x(19), x(20),x(21)))

val pubgRDDdf = pubgRDD_df.toDF()
pubgRDDdf.show(10)

// RDD를 Dataframe으로 변경할 때는 toDF()를 쓰면 된다.
val pubg_df = pubgRDDdf.toDF("Id", "groupId", "matchId", "assists", "boosts", "damageDealt", "DBNOs", "headshotKills", "heals", "killPlace", "killPoints", "kills", "killStreaks", "longestKill", "maxPlace", "numGroups", "revives", "rideDistance", "roadKills", "weaponsAcquired", "winPoints", "winPlacePerc")
pubg_df.printSchema()

pubg_df.show(10)

// COMMAND ----------

// MAGIC %sql
// MAGIC select * from pubg_data limit 10

// COMMAND ----------

 spark.conf.set("dfs.adls.oauth2.access.token.provider.type", "ClientCredential")
 spark.conf.set("dfs.adls.oauth2.client.id", "3fcabce8-ce4a-4b9f-a66f-f6ef9f7afe0f") //<APPLICATION-ID>
 spark.conf.set("dfs.adls.oauth2.credential", "DjdrDC5kmdcQl0L9qUsb0FUI9MJtVUYKCxeJLFHnHxk=") //<AUTHENTICATION-KEY>
 spark.conf.set("dfs.adls.oauth2.refresh.url", "https://login.microsoftonline.com/d646b43b-7348-4200-b89c-086fc797aab3/oauth2/token") //<TENANT-ID>

// COMMAND ----------

// 1. spark.sql.dataframe 형태 
// spark.read.csv를 할 경우에 colnames가 자꾸 1행으로 가버림.
val PUBG_train = spark.read.csv("adl://sparktestmj.azuredatalakestore.net/spark/PUBG_train.csv")

// spark.read.format으로 header = True를 해주면 됨.
val PUBG_train_foramt = spark.read.format("csv").option("header", "true").load("adl://sparktestmj.azuredatalakestore.net/spark/PUBG_train.csv")

// sqlContext.read로도 가능하다.
val df = sqlContext.read
    .format("com.databricks.spark.csv")
    .option("header", "true") 
    .load("adl://sparktestmj.azuredatalakestore.net/spark/PUBG_train.csv"); 

// 2. spark.rdd.RDD 형태
//RDD형태인 SC로 데이터 로드하기
val PUBG_train_sc = sc.textFile("adl://sparktestmj.azuredatalakestore.net/spark/PUBG_train.csv")

PUBG_train.first()

// COMMAND ----------

val pubg_rdd_todf = PUBG_train_sc.toDF("Id", "groupId", "matchId", "assists", "boosts", "damageDealt", "DBNOs", "headshotKills", "heals", "killPlace", "killPoints", "kills", "killStreaks", "longestKill", "maxPlace", "numGroups", "revives", "rideDistance", "roadKills", "weaponsAcquired", "winPoints", "winPlacePerc")

pubg_rdd_todf.show(10)

// COMMAND ----------

PUBG_train_foramt.show(10)

// COMMAND ----------

df.show(10)
