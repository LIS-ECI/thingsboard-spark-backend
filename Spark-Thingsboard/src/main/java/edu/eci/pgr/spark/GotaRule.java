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
public class GotaRule implements Rule {

    private static final int STREAM_WINDOW_MILLISECONDS = 5000; // 5 seconds
    private static final int TIME_HUMIDITY_SECONDS = 20000;
    private static final int TIME_TEMPERATURE_SECONDS = 40000;

    private static final double PERCENTAGE = 100;
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

    public void humidityAnalysis(String idParcel, String humidityData ){
        String humidityString = getValueOfRedis("Hdata", idParcel);
        String humidityValue = getValueOfRedis("Hvalue", idParcel);
        //Mirar datos de la humedad
        if (Double.parseDouble(humidityData) >= 90) {

            //La primera vez
            if (humidityString == null) {
                humidityString = "+";
                humidityValue = "1";
            } //Si pasó el tiempo para la humedad
            else if (humidityString.length() == (TIME_HUMIDITY_SECONDS / STREAM_WINDOW_MILLISECONDS) - 1) {
                humidityString += "+";
                int addValue = 1;
                String temp = humidityString.substring(0, 1);
                if (temp.equals("+")) {
                    addValue = 0;
                }
                humidityString = humidityString.substring(1);

                double Time_Seconds = (double) TIME_HUMIDITY_SECONDS;
                double Stream_Window = (double) STREAM_WINDOW_MILLISECONDS;
                int humidityvalue = Integer.parseInt(humidityValue) + 1;
                //Si cumple la condición completa es decir con tiempos
                System.out.println("AVG: " + (humidityvalue * 100) / (Time_Seconds / Stream_Window));
                System.out.println("AVG 2:" + Time_Seconds / Stream_Window);
                if ((humidityvalue * 100) / (Time_Seconds / Stream_Window) >= PERCENTAGE) {
                    saveToRedis("humidityWarning", idParcel, "+");
                }
                humidityValue = String.valueOf(Integer.parseInt(humidityValue) + addValue);
            } //Mientras no haya pasado el tiempo
            else {
                humidityString += "+";
                humidityValue = String.valueOf(Integer.parseInt(humidityValue) + 1);
            }
        } else {
            //Si no cumple la condición (humedad)
            if (humidityString != null) {
                if (humidityString.length() == (TIME_HUMIDITY_SECONDS / STREAM_WINDOW_MILLISECONDS) - 1) {
                    humidityString += "-";
                    int addValue = 0;
                    String temp = humidityString.substring(0, 1);
                    if (temp.equals("+")) {
                        addValue = -1;
                    }
                    humidityString = humidityString.substring(1);
                    double Time_Seconds = (double) TIME_HUMIDITY_SECONDS;
                    double Stream_Window = (double) STREAM_WINDOW_MILLISECONDS;
                    int humidity_value = Integer.parseInt(humidityValue);
                    //Si cumple la condición completa es decir con tiempos
                    System.out.println("AVG: " + (humidity_value * (Time_Seconds / Stream_Window)) / 100);
                    if ((humidity_value * (Time_Seconds / Stream_Window)) / 100 >= PERCENTAGE) {
                        saveToRedis("humidityWarning", idParcel, "+");
                    }
                    humidityValue = String.valueOf(Integer.parseInt(humidityValue) + addValue);
                }
                else {
                    humidityString += "-";
                }
            }
            
        }
        if (humidityString != null) {
                saveToRedis("Hdata", idParcel, humidityString);
                //Guardar valores en redis
                saveToRedis("Hvalue", idParcel, humidityValue);
            }

    
    }
    
    public void temperatureAnalysis(String idParcel,String temperatureData){
    String temperatureTime = getValueOfRedis("temperatureTime", idParcel);
        if (Double.parseDouble(temperatureData) >= 10) {
            System.out.println("temperatureTime");
            if (temperatureTime == null) {
                //inicializar el temperatureTime  
                temperatureTime = "1";
                
            } 
            //Si se cumplió el tiempo
            else if (Double.parseDouble(temperatureTime) == (TIME_TEMPERATURE_SECONDS / STREAM_WINDOW_MILLISECONDS) - 1) {
                //Si existe al menos 1 vez que durante 7 horas la humedad fue +
                String humidityWarning = getValueOfRedis("humidityWarning", idParcel);

                if (humidityWarning != null && humidityWarning.equals("+")) {
                    saveToRedis("humidityWarning", idParcel, "-");
                    for (Action ac : actions) {
                        ac.setIdParcel(idParcel);
                        ac.execute();
                    }
                    temperatureTime = "0";
                } //Se cumplio el tiempo pero no hay humedad alta se reinicia 
                else {
                    temperatureTime = String.valueOf(TIME_HUMIDITY_SECONDS / STREAM_WINDOW_MILLISECONDS);
                }

            } //Si aun no se cumple el tiempo
            else {
                temperatureTime = String.valueOf(Double.parseDouble(temperatureTime) + 1);
            }

        } 
        else {
            temperatureTime = "0";
            saveToRedis("Hdata", idParcel, "-");
            saveToRedis("Hvalue", idParcel, "0");
        }
        saveToRedis("temperatureTime", idParcel, temperatureTime);
    }
    
    
    
    @Override
    public void execute(HashMap<String, String> data) {
        System.out.println("Entró execute");
        String idParcel = data.get("idParcel");
        humidityAnalysis(idParcel,data.get("humidityData"));
        temperatureAnalysis(idParcel,data.get("temperatureData"));
        
  

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
