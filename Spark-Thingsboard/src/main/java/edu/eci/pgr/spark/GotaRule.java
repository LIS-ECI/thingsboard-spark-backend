/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.pgr.spark;

import com.mycompany.connection.MongoDBSpatial;
import java.util.ArrayList;
import java.util.Date;
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
    private static final int TIME_ANALYSIS_MILISECONDS = 20000; //15 seconds 
    private static final int TIME_DAY_MILISECONDS = 60000; //30 seconds
    private static final int TIME_DAY_MILISECONDS_ERROR = 1000; //1 seconds

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

    @Override
    public void execute(HashMap<String, String> data) {
        System.out.println("Entró execute");
        String idParcel = data.get("idParcel");

        RuleAnalysis(idParcel, data.get("humidityData"), data.get("temperatureData"));

    }

    private void RuleAnalysis(String idParcel, String humidityData, String temperatureData) {
        Date now = new Date();
        long now_long = now.getTime();
        String analysisString = getValueOfRedis("analysisString", idParcel);
        String analysisValue = getValueOfRedis("analysisValue", idParcel);
        String start_time = getValueOfRedis("start_time", idParcel);

        //Mirar si cumple la condición
        if (Double.parseDouble(humidityData) >= 90 && Double.parseDouble(temperatureData) >= 10) {

            //La primera vez
            if (analysisString == null || start_time == null) {
                analysisString = "+";
                analysisValue = "1";
                //Inicializar fecha

                saveToRedis("start_time", idParcel, String.valueOf(now_long));

            } //Si se cumplieron las 11 horas
            else if (analysisString.length() == (TIME_ANALYSIS_MILISECONDS / STREAM_WINDOW_MILLISECONDS) - 1) {
                analysisString += "+";
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
                System.out.println("AVG: " + (analysisvalue * 100) / (Time_Seconds / Stream_Window));
                System.out.println("AVG 2:" + Time_Seconds / Stream_Window);

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

                    if (now_long - Double.parseDouble(start_time) > TIME_DAY_MILISECONDS - TIME_DAY_MILISECONDS_ERROR) {
                        String warning = getValueOfRedis("Warning", idParcel);
                        //Por ser dia 2 apenas se cumplan las horas es porque hay alarma
                        System.out.println("ALARMAA");
                        //ALARMA
                        for (Action ac : actions) {
                            ac.setIdParcel(idParcel);
                            ac.execute();
                        }
                        saveToRedis("Warning", idParcel, "-");
                        analysisString = "-";
                        analysisValue = "0";
                        now = new Date();
                        now_long = now.getTime();
                        saveToRedis("start_time", idParcel, String.valueOf(now_long));
                    }

                }
                analysisValue = String.valueOf(Integer.parseInt(analysisValue) + addValue);
            } //Mientras no haya pasado el tiempo
            else {
                analysisString += "+";
                analysisValue = String.valueOf(Integer.parseInt(analysisValue) + 1);
            }

            //Si no cumple la condición (humedad)
        } else {
            //si ya existe al menos un registro
            if (analysisString != null && start_time != null && !analysisString.equals("")) {
                //Si se cumplen las 11 horas
                if (analysisString.length() == (TIME_ANALYSIS_MILISECONDS / STREAM_WINDOW_MILLISECONDS) - 1) {
                    analysisString += "-";
                    int addValue = 0;
                    String temp = analysisString.substring(0, 1);
                    if (temp.equals("+")) {
                        addValue = -1;
                    }
                    analysisString = analysisString.substring(1);
                    double Time_Seconds = (double) TIME_ANALYSIS_MILISECONDS;
                    double Stream_Window = (double) STREAM_WINDOW_MILLISECONDS;
                    int analysis_value = Integer.parseInt(analysisValue);
                    //Si cumple la condición completa es decir con tiempos continuos
                    System.out.println("AVG: " + (analysis_value * (Time_Seconds / Stream_Window)) / 100);
                    if ((analysis_value * (Time_Seconds / Stream_Window)) / 100 >= PERCENTAGE) {
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

                        if (now_long - Double.parseDouble(start_time) > TIME_DAY_MILISECONDS - TIME_DAY_MILISECONDS_ERROR) {
                            String warning = getValueOfRedis("Warning", idParcel);
                            //Por ser dia 2 apenas se cumplan las horas es porque hay alarma antes

                            //ALARMA
                            System.out.println("ALARMAA");
                            for (Action ac : actions) {
                                ac.setIdParcel(idParcel);
                                ac.execute();
                            }
                            saveToRedis("Warning", idParcel, "-");
                            analysisString = "-";
                            analysisValue = "0";
                            now = new Date();
                            now_long = now.getTime();
                            saveToRedis("start_time", idParcel, String.valueOf(now_long));
                        }
                    }
                    analysisValue = String.valueOf(Integer.parseInt(analysisValue) + addValue);
                } else {
                    analysisString += "-";
                }
            }

        }
        if (analysisString != null && analysisValue != null) {
            saveToRedis("analysisString", idParcel, analysisString);
            saveToRedis("analysisValue", idParcel, analysisValue);
        }

        if (start_time != null) {
            System.out.println("TIME_DAY_MILISECONDS - TIME_DAY_MILISECONDS_ERROR"+String.valueOf(TIME_DAY_MILISECONDS - TIME_DAY_MILISECONDS_ERROR));
            System.out.println("now_long - Double.parseDouble(start_time)"+String.valueOf(now_long - Double.parseDouble(start_time)));
            System.out.println("OR");
            System.out.println("TIME_DAY_MILISECONDS + TIME_DAY_MILISECONDS_ERROR"+String.valueOf(TIME_DAY_MILISECONDS + TIME_DAY_MILISECONDS_ERROR));
            if ((TIME_DAY_MILISECONDS - TIME_DAY_MILISECONDS_ERROR) < (now_long - Double.parseDouble(start_time)) && (now_long - Double.parseDouble(start_time)) < (TIME_DAY_MILISECONDS + TIME_DAY_MILISECONDS_ERROR)) {
                //si no hubo alarma reiniciar
                System.out.println("RENOVANDO DIA");
                String warning = getValueOfRedis("Warning", idParcel);
                if (warning == null || warning.equals("-")) {
                    saveToRedis("analysisString", idParcel, "");
                    saveToRedis("analysisValue", idParcel, "0");
                }

            }
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
