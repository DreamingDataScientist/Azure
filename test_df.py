# Databricks notebook source
# DBFS(Data Bricks File System)를 사용하여 저장소 계정 탑재 
# DBFS란 클러스터 파일 시스템 자체와 저장소가 연결됨. 클러스터에 엑세스하는 모든 응용 프로그램에서도 연결된 저장소를 사용할 수 있음.

#dbutils.fs.ls는 이미 default로 연동되어 있는 DBFS의 DIR를 보여준다.
display(dbutils.fs.ls("/")) 

# display(dbutils.fs.ls("/mnt/"))

###### 중요 #######
# 한번 mount되어 /mnt 디렉토리가 생성이 되면 다른 경로의 스토리지를 마운트 할 수 없다
# 그럴 때에는 unmount를 해야 한다! (에러 해결)
dbutils.fs.unmount("/mnt/") 

# COMMAND ----------

# 파일 시스템 새로고침
dbutils.fs.refreshMounts()

# COMMAND ----------

# 1. client.id는 application id 값
# 2. credential은 처음 생성한 앱 등록할 때 보여지는 key값
# 3. active directory 에 속성에 보여지는 directory id 값
configs = {"dfs.adls.oauth2.access.token.provider.type": "ClientCredential",
"dfs.adls.oauth2.client.id": "c6855336-523c-4d61-aa8c-02c9cdf1ae8b",
"dfs.adls.oauth2.credential": "uhUELB4+vi5xknyBgmDYziwMD83quag/P5jdHx263r0=",
"dfs.adls.oauth2.refresh.url": "https://login.microsoftonline.com/5e8844e0-6e01-48eb-abc9-98058f26e855/oauth2/token"}

# COMMAND ----------

dbutils.fs.mount(
  source = "adl://dbrtestmj.azuredatalakestore.net",
  mount_point = "/mnt",
  extra_configs = configs
)

# COMMAND ----------

display(dbutils.fs.ls("dbfs:/mnt/ml/"))

train = spark.read.text("/mnt/ml/PUBG_train.csv")
test = spark.read.text("/mnt/ml/PUBG_test.csv")

# COMMAND ----------

from pyspark.ml.regression import LinearRegression
from pyspark.ml.feature import VectorAssembler
from pyspark.ml.feature import StandardScaler
from pyspark.ml import Pipeline
from pyspark.sql.functions import *

# COMMAND ----------

train_sqlcon = sqlContext.read.format("com.databricks.spark.csv") \
  .option("header", "true")\
  .option("inferSchema", "true")\
  .load("/mnt/ml/PUBG_train.csv")

# COMMAND ----------

# 데이터 확인
train_sqlcon.show(5)

train.show(5)

print(train.columns)

# COMMAND ----------

train_sqlcon.cache()
train_sqlcon.count()

# COMMAND ----------

# data row에 na값은 다 없애주기 
# data에 na값이 있는 열은 없음

train_sqlcon = train_sqlcon.dropna()
train_sqlcon.count()

# COMMAND ----------

train_sqlcon.registerTempTable("data_geo")

# COMMAND ----------

train_sqlcon.take(1)
train_sqlcon.printSchema()

# COMMAND ----------

display(train_sqlcon.describe())
# train_sqlcon.describe().toPandas().transpose()

# COMMAND ----------

display(train_sqlcon)

# COMMAND ----------

display(train_sqlcon.select("groupId"))

# COMMAND ----------

pdtrain = train_sqlcon.toPandas()
pdtrain.head()

# COMMAND ----------

import seaborn as sns
sns.set()
  
ax = pdtrain.plot.hist()
display(ax.figure)

# COMMAND ----------

display(train_sqlcon.groupBy("numGroups").count())

# COMMAND ----------

display(train_sqlcon.filter(train_sqlcon.winPlacePerc == 1).sort(train_sqlcon.winPlacePerc, ascending=False))

# COMMAND ----------

display(train_sqlcon.filter(train_sqlcon.winPlacePerc <= 0.3).sort(train_sqlcon.winPlacePerc, ascending=False))

# COMMAND ----------

features = train_sqlcon.columns[0:-1]
print(features)

# COMMAND ----------

pubg_data = train_sqlcon.select(col('winPlacePerc').alias("label"), *features)
print(pubg_data.printSchema())
print(pubg_data.show(3))

# COMMAND ----------

(training, test) = pubg_data.randomSplit([.7, .3])

# COMMAND ----------

# 1. 모든 피쳐들 값을 vector type이어야 한다. - vector assembler란 주어진 열의 목록을 단일 벡터로 결합하는 변환기이다. 
# 회귀, decision tree와 같은 ML 모델을 교육하기 위해서는 벡터로 결합해야 함.
from pyspark.ml.feature import VectorAssembler
VectorAssembler = VectorAssembler(inputCols=features, outputCol="unscaled_features")

# COMMAND ----------

VectorAssembler

# COMMAND ----------

# 2. numeric data일 때는 피쳐들을 scale한다. 
StandardScaler = StandardScaler(inputCol = "unscaled_features", outputCol="features")

# 3. Linear Regression modeling
lr = LinearRegression(maxIter=10, regParam=.01)

stages = [VectorAssembler, StandardScaler, lr]
pipeline = Pipeline(stages=stages)

# COMMAND ----------

model = pipeline.fit(training)
prediction = model.transform(test)

# COMMAND ----------

prediction.show(5)

# COMMAND ----------

from pyspark.ml.evaluation import RegressionEvaluator
eval = RegressionEvaluator(labelCol="label", predictionCol="prediction", metricName="rmse")

# Root Mean Square Error
rmse = eval.evaluate(prediction)
print("RMSE: %.3f" % rmse)

# Mean Square Error
mse = eval.evaluate(prediction, {eval.metricName: "mse"})
print("MSE: %.3f" % mse)

# Mean Absolute Error
mae = eval.evaluate(prediction, {eval.metricName: "mae"})
print("MAE: %.3f" % mae)

# r2 - coefficient of determination
r2 = eval.evaluate(prediction, {eval.metricName: "r2"})
print("r2: %.3f" %r2)

# COMMAND ----------


