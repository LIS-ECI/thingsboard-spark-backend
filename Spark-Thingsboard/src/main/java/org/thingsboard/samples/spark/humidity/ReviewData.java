/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.samples.spark.humidity;

import com.baeldung.cassandra.java.client.repository.LandlotRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.connection.MongoDBException;
import com.mycompany.connection.MongoDBSpatial;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;
import org.thingsboard.samples.spark.temperature.TemperatureAndGeoZoneData;
import org.thingsboard.samples.spark.util.JedisUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import scala.Tuple2;

/**
 *
 * @author Cristian
 */
@Slf4j
public class ReviewData implements Serializable{

    private static final String THINGSBOARD_MQTT_ENDPOINT = "tcp://10.8.0.19:1883";
    private static final String TOPIC_TO_THINGSBOARD = "HumidityAvg";

    private MqttAsyncClient client;
    private MongoDBSpatial mdbs;

    ReviewData() throws MqttException {
        mdbs = new MongoDBSpatial();
    }

    public void analizeTelemetry(List<HumidityAndGeoZoneData> aggData, String Topic) throws Exception {
        if (!aggData.isEmpty()) {
            JavaRDD<HumidityAndGeoZoneData> telemetryData = SparkKafkaStreamingHumidityMain.sc.parallelize(aggData);

            //Convertir a un map(idlandlot, list<Integer>)
            JavaPairRDD<String, Double> hmap;
            hmap = telemetryData.mapToPair((HumidityAndGeoZoneData telemetryData1) -> {
                String idLandlot = mdbs.findLandlotsByDeviceId(telemetryData1.getDeviceId()).getId();
                return new Tuple2(idLandlot, telemetryData1.getHumidity());
            });

            List<Tuple2<String, Double>> hmap2 = hmap.collect();

            //count each values per key
            JavaPairRDD<String, Tuple2<Double, Double>> valueCount = hmap.mapValues(value -> new Tuple2<Double, Double>(value, 1.0));
            //add values by reduceByKey
            JavaPairRDD<String, Tuple2<Double, Double>> reducedCount = valueCount.reduceByKey((tuple1, tuple2) -> new Tuple2<Double, Double>(tuple1._1 + tuple2._1, tuple1._2 + tuple2._2));
            //calculate average
            JavaPairRDD<String, Double> averagePair = reducedCount.mapToPair(getAverageByKey);

            averagePair.foreach(data -> {
                //data.1: idlandlot
                //data.2: telemetry data
                saveToRedis(Topic, data._1, String.valueOf(data._2));

            });
        }
    }

    private void saveToRedis(String key, String idLandlot, String data) {
        Jedis jedis = JedisUtil.getPool().getResource();
        jedis.watch(key + idLandlot);
        Transaction t2 = jedis.multi();
        t2.set(key + idLandlot, data);
        t2.exec();
        jedis.close();
    }

    private static PairFunction<Tuple2<String, Tuple2<Double, Double>>, String, Double> getAverageByKey = (tuple) -> {
        Tuple2<Double, Double> val = tuple._2;
        double total = val._1;
        double count = val._2;
        Tuple2<String, Double> averagePair = new Tuple2<String, Double>(tuple._1, total / count);
        return averagePair;
    };
}
