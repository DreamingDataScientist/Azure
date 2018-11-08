# Databricks notebook source
# 1. 데이터 불러오기
display(dbutils.fs.ls("/mnt/ml/"))

# COMMAND ----------

# 2. 데이터 PARSE 모듈 생성하기
# ml 관련 모듈 import하기
from pyspark.mllib.regression import LabeledPoint, LinearRegressionWithSGD
from pyspark.mllib.evaluation import RegressionMetrics

# parse하기 위해 parse 함수 생성
def parsePoint(line):
    values = [float(x) for x in line.replace(',', ' ').split(' ')]
    return LabeledPoint(values[0], values[1:])

# COMMAND ----------

# 3. HEADER 포함 불러오기 FILTER로!
train_sc = sc.textFile("/mnt/ml/PUBG_train.csv")

train_header = train_sc.first()
print(train_header)

train_sc = train_sc.filter(lambda row:row != train_header)
print(train_sc.first())

# COMMAND ----------

# 4. RDD 데이터 TRAIN, TEST SPLIT
parseData = train_sc.map(parsePoint)
(trainingData, testData) = parseData.randomSplit([0.7, 0.3], seed=100)

# COMMAND ----------

# 5. MODEL 불러오기
model = LinearRegressionWithSGD.train(trainingData)

# COMMAND ----------

# 6. 모델평가하기
Preds = testData.map(lambda p: (float(model.predict(p.features)), p.label))
MSE = Preds.map(lambda (v, p): (v - p)**2).reduce(lambda x, y: x + y) / Preds.count()

#  여기서 잘 못된 것은 feature값과 종속변수를 나누지 않았음 ㅎㅎ
print("Mean Squared Error = " + str(MSE))
print("\n")

# COMMAND ----------

testData.take(10)

# COMMAND ----------


