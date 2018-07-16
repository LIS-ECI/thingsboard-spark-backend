package spark.testcode;


import edu.eci.pgr.cassandra.java.client.connector.CassandraConnector;
import edu.eci.pgr.cassandra.java.client.repository.KeyspaceRepository;
import edu.eci.pgr.cassandra.java.client.repository.LandlotRepository;
import com.datastax.driver.core.Session;
import com.mycompany.connection.MongoDBException;
import com.mycompany.connection.MongoDBSpatial;
import edu.eci.pgr.spark.RulesEngine;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.thingsboard.samples.spark.temperature.TemperatureAndGeoZoneData;
import org.thingsboard.samples.spark.util.JedisUtil;
import org.thingsboard.server.common.data.landlot.Landlot;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;
import scala.Tuple2;





public class WordCount {
    
    
  private static MongoDBSpatial mdbs;
  private static String Topic ="temperature";
  private static RulesEngine rulesEngine;

  
  private static PairFunction<Tuple2<String, Tuple2<Double, Double>>,String,Double> getAverageByKey = (tuple) -> {
     Tuple2<Double, Double> val = tuple._2;
     double total = val._1;
     double count = val._2;
     Tuple2<String, Double> averagePair = new Tuple2<String, Double>(tuple._1, total / count);
     return averagePair;
  };
  
  private static String getLandlotNameCassandra(String idLandlot) {
        CassandraConnector connector = new CassandraConnector();
        connector.connect("10.8.0.19", null);
        Session session = connector.getSession();
        KeyspaceRepository sr = new KeyspaceRepository(session);
        sr.useKeyspace("thingsboard");
        LandlotRepository pr = new LandlotRepository(session);
        System.out.println("LANDLOT "+idLandlot);
        Landlot p = pr.selectById(idLandlot);
        
        return p.getCrop().getName();
    }
  
  private static String getValueOfRedis(String key, String idLandlot) {
        boolean funciono = true;
        String content = "";
        while (funciono) {
            Jedis jedis = JedisUtil.getPool().getResource();
            jedis.watch(key + idLandlot);
            Transaction t = jedis.multi();
            Response<String> valor = t.get(key + idLandlot);

            List<Object> res = t.exec();
            if (res.size() > 0) {
                funciono = false;
                content = valor.get();
                jedis.close();
            }
        }
        return content;
    }
  
  private static String getTokenSpark(String idLandlot, String Topic) {
        String token = null;
        try {
            token = mdbs.getTokenByIdLandlotTopic(idLandlot, Topic);
        } catch (MongoDBException ex) {
            Logger.getLogger(WordCount.class.getName()).log(Level.SEVERE, null, ex);
        }
        return token;
    }
  
  public static void main(String[] args) throws Exception {
    // Create a Java Spark Context.
    ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");
        
    rulesEngine = ac.getBean(RulesEngine.class);
    mdbs = new MongoDBSpatial();
    SparkConf conf = new SparkConf().setAppName("wordCount").setMaster("local");
     JavaSparkContext sc = new JavaSparkContext(conf);
   

    // Load our input data.
    ArrayList<TemperatureAndGeoZoneData> aggData= new ArrayList<>();
    TemperatureAndGeoZoneData temp1= new TemperatureAndGeoZoneData();
    temp1.setCount(2);
    temp1.setDeviceId("5d8796e0-74e5-11e8-9cab-55a210069b83");
    temp1.setTemperature(19.5);
    aggData.add(temp1);
    TemperatureAndGeoZoneData temp2= new TemperatureAndGeoZoneData();
    temp2.setCount(1);
    temp2.setDeviceId("12246f80-7a20-11e8-8b35-d36cf5cff956");
    temp2.setTemperature(19.0);
    aggData.add(temp2);
    TemperatureAndGeoZoneData temp3= new TemperatureAndGeoZoneData();
    temp3.setCount(1);
    temp3.setDeviceId("be4b89f0-7a3f-11e8-88c8-d36cf5cff956");
    temp3.setTemperature(20.0);
    aggData.add(temp3);
    
    for (TemperatureAndGeoZoneData tmp: aggData){
        System.out.println("Values aggdata: "+tmp.getDeviceId()+" "+tmp.getTemperature());
    }
    
    JavaRDD<TemperatureAndGeoZoneData> telemetryData = sc.parallelize(aggData);
    
    //Convertir a un map(idlandlot, list<Integer>)
    
    JavaPairRDD<String,Double> hmap;
    hmap=telemetryData.mapToPair(new PairFunction<TemperatureAndGeoZoneData,String,Double>(){
        @Override
        public Tuple2<String, Double> call(TemperatureAndGeoZoneData telemetryData) throws MongoDBException { 
            Tuple2<String, Double> res;        
            String idLandlot = mdbs.findLandlotsByDeviceId(telemetryData.getDeviceId()).getId();
            res=new Tuple2(idLandlot,telemetryData.getTemperature());
            return res;
        }
    });
    
    List<Tuple2<String,Double>> hmap2= hmap.collect();
    for (Tuple2<String,Double> tmp: hmap2){
        System.out.println("Value: "+tmp._1+" "+tmp._2);
    }
    
    
    
    //count each values per key
    JavaPairRDD<String, Tuple2<Double, Double>> valueCount = hmap.mapValues(value -> new Tuple2<Double, Double>(value,1.0));
    //add values by reduceByKey
    JavaPairRDD<String, Tuple2<Double, Double>> reducedCount = valueCount.reduceByKey((tuple1,tuple2) ->  new Tuple2<Double, Double>(tuple1._1 + tuple2._1, tuple1._2 + tuple2._2));
    //calculate average
    JavaPairRDD<String, Double> averagePair = reducedCount.mapToPair(getAverageByKey);
    //for each landlot review sick probability
    averagePair.foreach(data -> {
        //data.1: idlandlot
        //data.2: telemetry data
        System.out.println("Key="+data._1() + " Average=" + data._2());
        String idLandlot= data._1();
        double temperatureData= data._2();
        String token = getTokenSpark(idLandlot, Topic);
        
        //review enfermedades

        String temphumi=getValueOfRedis("humidity", idLandlot);
        Double humidityData=0.0;
        if (temphumi!=null){
           humidityData = Double.parseDouble(temphumi);
        }
        
        HashMap<String,String> data2 = new HashMap<>();
        data2.put("humidityData",String.valueOf(humidityData));
        data2.put("temperatureData", String.valueOf(temperatureData));
        data2.put("idLandlot", idLandlot);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS");
        Date now = new Date();
        data2.put("first_time",  sdf.format(now));
        
        String landlot_name = getLandlotNameCassandra(idLandlot);
        System.out.println("landlot_name: "+landlot_name);
        if (landlot_name.equals("Papa")){
            //rulesEngine.execute(data2,model);
        }
        
        
    }); 
    
    
    
    
    
    //stop sc
    sc.stop();
    sc.close();
}


}
    
    
    
    
    
    
    
  
  
