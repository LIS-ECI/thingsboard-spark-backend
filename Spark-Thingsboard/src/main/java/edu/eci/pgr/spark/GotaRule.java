/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.pgr.spark;

import com.mycompany.connection.MongoDBSpatial;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.thingsboard.samples.spark.util.JedisUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

/**
 *
 * @author cristian
 */
@Service
public class GotaRule implements Rule{
    

    private static final int STREAM_WINDOW_MILLISECONDS = 5000; // 5 seconds
    private static final int TIME_SECONDS = 60000;
    private static final double PERCENTAGE = 80;
    private List<Action> actions;
    
    
     public GotaRule(){
        actions= new ArrayList<>();
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
    
    @Override
    public void execute(HashMap<String,String> data) {
        
        String idParcel=data.get("idParcel");
        String dataString = getValueOfRedis("data", idParcel);
        String Parcelvalue = getValueOfRedis("value", idParcel); 

        if (data.get("ParcelName").equals("Papa") && Double.parseDouble(data.get("humidityData")) > 75 && Double.parseDouble(data.get("temperatureData"))> 10) {

         
            //La primera vez que entra             
            if (dataString == null) {
                dataString = "+";
                Parcelvalue = "1";
            } //Cuando se cumple el tiempo estimado
            else if (dataString.length() == (TIME_SECONDS / STREAM_WINDOW_MILLISECONDS) - 1) {
                dataString += "+";
                int addValue = 1;
                String temp = dataString.substring(0, 1);
                if (temp.equals("+")) {
                    addValue = 0;
                }
                dataString = dataString.substring(1);

                double Time_Seconds = (double) TIME_SECONDS;
                double Stream_Window = (double) STREAM_WINDOW_MILLISECONDS;
                int parcel_value = Integer.parseInt(Parcelvalue)+1;
                //Si cumple la condición completa es decir con tiempos
                System.out.println("AVG: "+(parcel_value * 100)/(Time_Seconds / Stream_Window) );
                System.out.println("AVG 2:"+Time_Seconds / Stream_Window);
                if ((parcel_value * 100)/(Time_Seconds / Stream_Window) >= PERCENTAGE) {
                    for (Action ac: actions){
                        ac.setIdParcel(idParcel);
                        ac.execute();
                    }
                }
                Parcelvalue = String.valueOf(Integer.parseInt(Parcelvalue) + addValue);


            } //Cuando aún no se cumple el tiempo
            else {
                dataString += "+";
                Parcelvalue = String.valueOf(Integer.parseInt(Parcelvalue) + 1);
            }

        } else {
            //Si no cumple la condición
            if (dataString != null) {
                if (dataString.length() == (TIME_SECONDS / STREAM_WINDOW_MILLISECONDS) - 1) {
                    dataString += "-";
                    int addValue = 0;
                    String temp = dataString.substring(0, 1);
                    if (temp.equals("+")) {
                        addValue = -1;
                    }
                    dataString = dataString.substring(1);
                    double Time_Seconds = (double) TIME_SECONDS;
                    double Stream_Window = (double) STREAM_WINDOW_MILLISECONDS;
                    int parcel_value = Integer.parseInt(Parcelvalue);
                    //Si cumple la condición completa es decir con tiempos
                    System.out.println("AVG: "+(parcel_value * (Time_Seconds / Stream_Window)) / 100 );
                    if ((parcel_value * (Time_Seconds / Stream_Window)) / 100 >= PERCENTAGE) {
                        System.out.println("dataString"+dataString);
                        for (Action ac: actions){
                            ac.setIdParcel(idParcel);
                            ac.execute();
                        }
                    }
                    Parcelvalue = String.valueOf(Integer.parseInt(Parcelvalue) + addValue);

                } else {
                    dataString += "-";
                }
            }

        }
        if (dataString != null) {
            saveToRedis("data", idParcel, dataString);
            //Guardar valores en redis
            saveToRedis("value", idParcel, Parcelvalue);
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
