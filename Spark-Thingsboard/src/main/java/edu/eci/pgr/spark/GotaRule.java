/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.pgr.spark;

import com.baeldung.cassandra.java.client.CassandraConnector;
import com.baeldung.cassandra.java.client.repository.KeyspaceRepository;
import com.baeldung.cassandra.java.client.repository.ParcelRepository;
import com.datastax.driver.core.Session;
import com.mycompany.connection.MongoDBSpatial;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.thingsboard.samples.spark.util.JedisUtil;
import org.thingsboard.server.common.data.parcel.Parcel;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

/**
 *
 * @author cristian
 */
@Service
public class GotaRule implements Rule {

    private static final int STREAM_WINDOW_MILLISECONDS = 5000; // 5 seconds
    private static final int TIME_ANALYSIS_MILISECONDS = 25000; //25 seconds 
    private static final int TIME_DAY_MILISECONDS = 60000; //60 seconds
    private static final int TIME_DAY_MILISECONDS_ERROR = 1000; //1 seconds
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS");
    private static final double PERCENTAGE = 80;
    private List<Action> actions;

    public GotaRule() {
        actions = new ArrayList<>();
        actions.add(new ActionDroneFique());
        actions.add(new ActionSendAlert());

    }

    private String getValueOfRedis(String key, String idParcel) {
        boolean funciono = true;
        String content = "";
        while (funciono) {
            Jedis jedis = JedisUtil.getPool().getResource();
            jedis.watch(key + idParcel);
            Transaction t = jedis.multi();
            Response<String> valor = t.get(key + idParcel);

            List<Object> res = t.exec();
            if (res.size() > 0) {
                funciono = false;
                content = valor.get();
                jedis.close();
            }
        }
        return content;
    }

    
    private String getParcelNameCassandra(String idParcel) {
        CassandraConnector connector = new CassandraConnector();
        connector.connect("10.8.0.18", null);
        Session session = connector.getSession();
        KeyspaceRepository sr = new KeyspaceRepository(session);
        sr.useKeyspace("thingsboard");
        ParcelRepository pr = new ParcelRepository(session);
        Parcel p = pr.selectById(idParcel);
        return p.getCrop().getName();
    }
    
    @Override
    public void execute(HashMap<String, String> data) {
        String idParcel = data.get("idParcel");
        RuleAnalysis(idParcel, data.get("humidityData"), data.get("temperatureData"),data.get("first_time"));
    }

    private void RuleAnalysis(String idParcel, String humidityData, String temperatureData,String first_time) {
        
        Date now = new Date();
        long now_long = now.getTime();     
        String analysisString = getValueOfRedis("analysisString", idParcel);
        String analysisValue = getValueOfRedis("analysisValue", idParcel);
        String start_time = getValueOfRedis("start_time", idParcel);
        String condition="-"; 
        int value=0;
        String parcel_name = getParcelNameCassandra(idParcel);
        //Mirar si cumple la condición
        if (parcel_name.equals("Papa") && Double.parseDouble(humidityData) >= 90 && Double.parseDouble(temperatureData) >= 10) {
            condition="+";
            value=1;
        }
            //La primera vez
        if (analysisString == null || start_time == null) {
                analysisString = condition;
                analysisValue = String.valueOf(value);
                //Inicializar fecha
                saveToRedis("start_time", idParcel, String.valueOf(now_long));

            } //Si se cumplieron las 11 horas
            else if (analysisString.length() == (TIME_ANALYSIS_MILISECONDS / STREAM_WINDOW_MILLISECONDS) - 1) {
                analysisString += condition;
                int addValue = 1;
                String temp = analysisString.substring(0, 1);
                if (temp.equals("+")) {
                    addValue = 0;
                }
                analysisString = analysisString.substring(1);

                long Time_Seconds = (long) TIME_ANALYSIS_MILISECONDS;
                long Stream_Window = (long) STREAM_WINDOW_MILLISECONDS;
                int analysisvalue = Integer.parseInt(analysisValue) + 1;
                //Si cumple la condición completa es decir con tiempos
                //Si se genero el valor durante las 11 horas seguidas
                if ((analysisvalue * 100) / (Time_Seconds / Stream_Window) >= PERCENTAGE) {

                    //Si estoy en el dia 1 
                    if (now_long - Double.parseDouble(start_time) < TIME_DAY_MILISECONDS + TIME_DAY_MILISECONDS_ERROR) {
                        String warning = getValueOfRedis("Warning", idParcel);
                        //hay un warning?
                        //si --> no hacer nada
                        //no --> agregar
                        if (warning == null || warning.equals("-")) {
                            saveToRedis("Warning", idParcel, "+");
                        }
                    }

                    if (now_long - Double.parseDouble(start_time) > TIME_DAY_MILISECONDS + STREAM_WINDOW_MILLISECONDS*2 ) {
                        String warning = getValueOfRedis("Warning", idParcel);
                        //Por ser dia 2 apenas se cumplan las horas es porque hay alarma
                        //ALARMA
                        for (Action ac : actions) {
                            ac.setIdParcel(idParcel);
                            ac.execute();
                        }
                        saveToRedis("Warning", idParcel, "-");
                        analysisString = "";
                        analysisValue = "0";
                        now = new Date();
                        now_long = now.getTime();
                        saveToRedis("start_time", idParcel, String.valueOf(now_long));
                    }

                }
                analysisValue = String.valueOf(Integer.parseInt(analysisValue) + addValue);
            } //Mientras no haya pasado el tiempo
            else {
                analysisString += condition;
                analysisValue = String.valueOf(Integer.parseInt(analysisValue) + value);
            }

            //Si no cumple la condición (humedad)

        saveToRedis("analysisString", idParcel, analysisString);
        saveToRedis("analysisValue", idParcel, analysisValue);
        String warning = getValueOfRedis("Warning", idParcel);

        //SI es el primer dia
        boolean day1=(TIME_DAY_MILISECONDS - TIME_DAY_MILISECONDS_ERROR)< (now_long - Double.parseDouble(start_time)) && (now_long - Double.parseDouble(start_time)) < (TIME_DAY_MILISECONDS + TIME_DAY_MILISECONDS_ERROR);
        boolean day2=(TIME_DAY_MILISECONDS*2 - TIME_DAY_MILISECONDS_ERROR)< (now_long - Double.parseDouble(start_time)) && (now_long - Double.parseDouble(start_time)) < (TIME_DAY_MILISECONDS*2 + TIME_DAY_MILISECONDS_ERROR);
        if (day1){
            if (warning == null || warning.equals("-")) {
            //si no hubo alarma reiniciar
            saveToRedis("start_time", idParcel, String.valueOf(now_long));
            }
            saveToRedis("analysisString", idParcel, "");
            saveToRedis("analysisValue", idParcel, "0");
        } 
        if (day2){
            saveToRedis("Warning", idParcel, "-");
            saveToRedis("start_time", idParcel, String.valueOf(now_long));
            saveToRedis("analysisString", idParcel, "");
            saveToRedis("analysisValue", idParcel, "0");
        }
                 
        File file = new File("DatosPGR1.csv");
      
        FileWriter writer; 
        try {
            writer = new FileWriter(file,true);
            Date now2 = new Date();
            writer.write(first_time+","+sdf.format(now2)+'\n');
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(GotaRule.class.getName()).log(Level.SEVERE, null, ex);
        }
            
   }


    private void saveToRedis(String key, String idParcel, String data) {
        Jedis jedis = JedisUtil.getPool().getResource();
        jedis.watch(key + idParcel);
        Transaction t2 = jedis.multi();
        t2.set(key + idParcel, data);
        t2.exec();
        jedis.close();
    }

}
