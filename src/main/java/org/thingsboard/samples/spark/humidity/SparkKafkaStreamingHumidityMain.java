/**
 * Copyright © 2016 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.samples.spark.humidity;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.eci.pgr.spark.RulesEngine;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import scala.Tuple2;

import java.util.*;
import org.apache.spark.api.java.JavaSparkContext;


public class SparkKafkaStreamingHumidityMain {

    // Kafka brokers URL for Spark Streaming to connect and fetched messages from.
    private static final String KAFKA_BROKER_LIST = "10.8.0.18:9092";
    // Time interval in milliseconds of Spark Streaming Job, 10 seconds by default.
    private static final int STREAM_WINDOW_MILLISECONDS = 5000; // 5 seconds
    // Kafka telemetry topic to subscribe to. This should match to the topic in the rule action.
    private static final String Topic="humidity";
    private static final Collection<String> TOPICS = Arrays.asList(Topic);
    // The application name
    public static final String APP_NAME = "Spark Humidity";
    private static RulesEngine rulesEngine; 

    // Misc Kafka client properties
    private static Map<String, Object> getKafkaParams() {
        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", KAFKA_BROKER_LIST);
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        kafkaParams.put("group.id", "DEFAULT_GROUP_ID");
        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("enable.auto.commit", false);
        return kafkaParams;
    }

    public static void main(String[] args) throws Exception {
        new StreamRunner().start();
    }

    @Slf4j
    private static class StreamRunner {

        private ReviewData reviewData;

        StreamRunner() throws Exception {
            //restClient = new RestClient();
            rulesEngine= new RulesEngine();
            reviewData = new ReviewData();
            
        }

        void start() throws Exception {
            SparkConf conf = new SparkConf().setAppName(APP_NAME); //.setMaster("local");
            
                try (JavaStreamingContext ssc = new JavaStreamingContext(conf, new Duration(STREAM_WINDOW_MILLISECONDS))) {

                JavaInputDStream<ConsumerRecord<String, String>> stream =
                        KafkaUtils.createDirectStream(
                                ssc,
                                LocationStrategies.PreferConsistent(),
                                ConsumerStrategies.<String, String>Subscribe(TOPICS, getKafkaParams())
                        );
                stream.foreachRDD(rdd ->
                {
                    
                    // Map incoming JSON to WindSpeedAndGeoZoneData objects
                    JavaRDD<HumidityAndGeoZoneData> windRdd = rdd.map(new HumidityStationDataMapper());
                    // Map WindSpeedAndGeoZoneData objects by GeoZone

                    JavaPairRDD<String, AvgHumidityData> temperatureByZoneRdd = windRdd.mapToPair(d -> new Tuple2<>(d.getDeviceId(), new AvgHumidityData(d.getHumidity())));                    
// Reduce all data volume by GeoZone key
                    temperatureByZoneRdd = temperatureByZoneRdd.reduceByKey((a, b) -> AvgHumidityData.sum(a, b));
                    // Map <GeoZone, AvgWindSpeedData> back to WindSpeedAndGeoZoneData
                    List<HumidityAndGeoZoneData> aggData = temperatureByZoneRdd.map(t -> new HumidityAndGeoZoneData(t._1, t._2.getAvgValue(),t._2.getCount())).collect();                    
// Push aggregated data to ThingsBoard Asset
                    //restClient.sendTelemetryToAsset(aggData);
                   if (!aggData.isEmpty()) {
                        JavaRDD<HumidityAndGeoZoneData> telemetryData = ssc.sparkContext().parallelize(aggData);
                        reviewData.analizeTelemetry(telemetryData,Topic,rulesEngine);
                   }
                    
                });
                ssc.start();
                ssc.awaitTermination();
            }
        }

        private IMqttActionListener getCallback() {
            return new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    log.info("Telemetry data updated!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    log.error("Telemetry data update failed!", exception);
                }
            };
        }
        
        private static class HumidityStationDataMapper implements Function<ConsumerRecord<String, String>, HumidityAndGeoZoneData> {
            private static final ObjectMapper mapper = new ObjectMapper();

            @Override
            public HumidityAndGeoZoneData call(ConsumerRecord<String, String> record) throws Exception {
                return mapper.readValue(record.value(), HumidityAndGeoZoneData.class);
            }
        }
    }
}