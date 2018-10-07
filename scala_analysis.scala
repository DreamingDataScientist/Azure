// Databricks notebook source
sc

// COMMAND ----------

// Azure Data Lake Store는 인증 및 엑세스 제어 목록에 Azure AD - azure active directory를 사용합니다. 
// 액세스 토큰을 얻으려면 Hadoop 클라이언트와 spark가 oauth2가 있는 webHDFS를 지원해야 함. 

spark.conf.set("dfs.adls.oauth2.access.token.provider.type", "ClientCredential")
spark.conf.set("dfs.adls.oauth2.client.id", "3fcabce8-ce4a-4b9f-a66f-f6ef9f7afe0f") //<APPLICATION-ID>
spark.conf.set("dfs.adls.oauth2.credential", "DjdrDC5kmdcQl0L9qUsb0FUI9MJtVUYKCxeJLFHnHxk=") //<AUTHENTICATION-KEY>
spark.conf.set("dfs.adls.oauth2.refresh.url", "https://login.microsoftonline.com/d646b43b-7348-4200-b89c-086fc797aab3/oauth2/token") //<TENANT-ID>

val pubg_train = spark.read.csv("adl://sparktestmj.azuredatalakestore.net/spark/PUBG_train.csv")

val pubg_train_blob = sc.textFile("wasbs://spark@testmjhaha9354.blob.core.windows.net/mnt/PUBG_train (3).csv")

pubg_train.first()
pubg_train_blob.first()
// 왜 sc.textFile로는 안불러져??? 하... 됐다 안됐다하는 것 같음.

// COMMAND ----------

pubg_train_blob.first()

// COMMAND ----------

import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vectors
// import org.apache.spark.mllib.linalg.{Vector, Vectors}

val pubg_train3 = pubg_train_blob.map(x => {
val a = x.toArray
  LabeledPoint(a(a.length-1), Vectors.dense(a.slice(0, a.length-1)))
})

// COMMAND ----------

pubg_train.schema()

// COMMAND ----------


