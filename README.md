## Thingsboard Spark Backend

The objective of this implementation of Spark, is to build a model that allows to analyze the data sent by the devices to Thingsboard in real time, and perform a set of actions depending on the case, for this can be extended within it a set of rules to analyze and actions to execute.

This project has 3 nodes each dedicated to a specific topic, temperature, humidity and light intensity, whose function is to average the incoming data of the sensors of each type by crop. In addition, the temperature node is configured to read the information from the humidity and light intensity node from Redis and, with this, execute a rule that detects the "late blight" in order to send another data that generates a pest risk alarm in Thingsboard.
Also the rules can execute models of Spark MlLib, like decision tree.

**Local Mode Instructions:**

Uncomment in all the main classes (SparkKafkaStreamingTemperatureMain,SparkKafkaStreamingLightMain,SparkKafkaStreamingHumidityMain)
*SparkConf conf = new SparkConf().setAppName(APP_NAME); //.setMaster("local");*
Must be
*SparkConf conf = new SparkConf().setAppName(APP_NAME).setMaster("local");*

the next instructions were made for being executed in a local way, it makes more easy the development and deployment in a test enviroment, if you want a cluster mode instructions, that gonna be depending of the implementation of the cluster.

In this case we need a serie of prerequisites, the most important of all a serie of sensors sending data and the configuration of "thingsboard" working sucessfully (Kafka plugin,devices created and configured and rules enabled) 

1. Go to thingsboard-spark-backend directory and execute 

*mvn clean package*

Once it have been compile sucessfully, go to the target folder and there you can find 3 .jar files
if you want to execute the example of "late blight" (Phytophthora infestans) you have to run the next jar files:
  Spark-Light-KafkaStreaming.jar
  Spark-Temperature-KafkaStreaming.jar
  Spark-Humidity-KafkaStreaming.jar
  
2. execute in three terminals:
*java -jar Spark-Light-KafkaStreaming.jar 
java -jar Spark-Humidity-KafkaStreaming.jar 
java -jar Spark-Temperature-KafkaStreaming.jar*
respectively

 **Cluster Mode**
In this example the cluster it gonna have a master and two workers,in the three machine must has been installed spark, for execute the master we need to go to the spark folder (folder where you installed spark) and execute the next command:
*sudo  /home/spark-2.3.1-bin-hadoop2.7/bin/spark-class org.apache.spark.deploy.master.Master*

in another one terminal or computer (worker node), go to the spark folder and execute 
*sudo  /home/spark-2.3.1-bin-hadoop2.7/bin/spark-class org.apache.spark.deploy.worker.Worker spark://10.2.78.160:7077* 

Do it to associate a new worker to the cluster (asociate the number of workers that you need)

In a master terminal execute in the spark folder:
```sh
$ sudo /home/spark-2.3.1-bin-hadoop2.7/bin/spark-submit --class org.thingsboard.samples.spark.temperature.SparkKafkaStreamingTemperatureMain --master spark://10.8.0.17:7077 --conf spark.cores.max=1 /home/pgr/thingsboard-spark-backend/target/Spark-Temperature-KafkaStreaming.jar 
```
In another terminal of master do:

```sh
$ sudo /home/spark-2.3.1-bin-hadoop2.7/bin/spark-submit --class org.thingsboard.samples.spark.humidity.SparkKafkaStreamingHumidityMain --master spark://10.8.0.17:7077 --conf spark.cores.max=1 /home/pgr/thingsboard-spark-backend/target/Spark-Humidity-KafkaStreaming.jar
```
Its necesary to change the Ip of the previous commands and write the Ip of the cluster's master node 
