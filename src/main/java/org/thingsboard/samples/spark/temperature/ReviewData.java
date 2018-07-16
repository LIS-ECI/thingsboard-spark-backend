/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.samples.spark.temperature;

import edu.eci.pgr.cassandra.java.client.connector.CassandraConnector;
import edu.eci.pgr.cassandra.java.client.repository.KeyspaceRepository;
import edu.eci.pgr.cassandra.java.client.repository.LandlotRepository;
import com.datastax.driver.core.Session;
import com.mycompany.connection.MongoDBException;
import com.mycompany.connection.MongoDBSpatial;
import edu.eci.pgr.spark.rules.Rule;
import edu.eci.pgr.spark.RulesEngine;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.mllib.tree.model.DecisionTreeModel;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.thingsboard.samples.spark.util.ExternalMethods;
import org.thingsboard.samples.spark.util.JedisUtil;
import org.thingsboard.server.common.data.landlot.Landlot;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;
import scala.Tuple2;

/**
 *
 * @author Cristian
 */
@Slf4j
public class ReviewData implements Serializable{

    private static RulesEngine rulesEngine = new RulesEngine();

    ReviewData( ) {}

    public void analizeTelemetry(JavaRDD<TemperatureAndGeoZoneData> telemetryData, String Topic,DecisionTreeModel model) {
        

            //Convertir a un map(idlandlot, list<Integer>)
            JavaPairRDD<String, Double> hmap;
            hmap = telemetryData.mapToPair((TemperatureAndGeoZoneData telemetryData1) -> {
                String idLandlot = ExternalMethods.getMdbs().findLandlotsByDeviceId(telemetryData1.getDeviceId()).getId();
                return new Tuple2(idLandlot, telemetryData1.getTemperature());
            });

            List<Tuple2<String, Double>> hmap2 = hmap.collect();
            
            //count each values per key
            JavaPairRDD<String, Tuple2<Double, Double>> valueCount = hmap.mapValues(value -> new Tuple2<Double, Double>(value, 1.0));
            //add values by reduceByKey
            JavaPairRDD<String, Tuple2<Double, Double>> reducedCount = valueCount.reduceByKey((tuple1, tuple2) -> new Tuple2<Double, Double>(tuple1._1 + tuple2._1, tuple1._2 + tuple2._2));
            //calculate average
            JavaPairRDD<String, Double> averagePair = reducedCount.mapToPair(getAverageByKey);
            //for each landlot review sick probability
            averagePair.foreach(data -> {
                //data.1: idlandlot
                //data.2: telemetry data
                System.out.println("Key=" + data._1() + " Average=" + data._2());
                String idLandlot = data._1();
                double temperatureData = data._2();
                String token = ExternalMethods.getTokenSpark(idLandlot, Topic);

                //Get data of the other applications
                String temphumi = ExternalMethods.getValueOfRedis("humidity", idLandlot);
                Double humidityData = 0.0;
                if (temphumi != null) {
                    humidityData = Double.parseDouble(temphumi);
                }
                
                
                String templight = ExternalMethods.getValueOfRedis("light", idLandlot);
                Double lightyData = 0.0;
                if (templight != null) {
                    lightyData = Double.parseDouble(templight);
                }

                HashMap<String, String> dataApplications = new HashMap<>();
                dataApplications.put("humidityData", String.valueOf(humidityData));
                dataApplications.put("temperatureData", String.valueOf(temperatureData));
                dataApplications.put("lightData", String.valueOf(lightyData));
                dataApplications.put("idLandlot", idLandlot);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS");
                Date now = new Date();
                dataApplications.put("first_time", sdf.format(now));

                String landlot_name = ExternalMethods.getCropNameCassandra(idLandlot);
                if (landlot_name!=null) {
                    System.out.println("landlot_name: " + landlot_name);
                    dataApplications.put("landlot_name", landlot_name);
                    rulesEngine.execute(dataApplications,model);
                }

            });

        
    }

    private static PairFunction<Tuple2<String, Tuple2<Double, Double>>, String, Double> getAverageByKey = (tuple) -> {
        Tuple2<Double, Double> val = tuple._2;
        double total = val._1;
        double count = val._2;
        Tuple2<String, Double> averagePair = new Tuple2<String, Double>(tuple._1, total / count);
        return averagePair;
    };

}
