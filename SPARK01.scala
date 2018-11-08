// Databricks notebook source
sc

// COMMAND ----------

 // DBFS
display(dbutils.fs.ls("/FileStore/tables/")) 

// COMMAND ----------

// Lazy 
val msazure = sc.textFile("/FileStore/tables/word.txt")

// COMMAND ----------

// Action 실행 - Job 실행
msazure.take(5).foreach(println)

// COMMAND ----------

//공백으로 단어를 나누어서 array로 넣어주고 k/v로 mapping되며 동일한 key값을 더해줌.
val msazureword = msazure.flatMap(line => line.split(" ")).map(word => (word, 1)).reduceByKey(_ + _) 

// COMMAND ----------

msazureword.collect()

// COMMAND ----------

msazureword.cache()

// COMMAND ----------

//sorting
val msazurewordsort = msazureword.map(a => a.swap).sortByKey(ascending = false).map(a => a.swap).take(50)

// COMMAND ----------


