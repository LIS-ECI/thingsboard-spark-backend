/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.pgr.spark;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.SimpleDateFormat;
import org.apache.spark.mllib.tree.model.DecisionTreeModel;
import org.thingsboard.samples.spark.util.ExternalMethods;

/**
 *
 * @author christian
 */
public class GotaRule extends Rule implements Serializable{

    private static final int STREAM_WINDOW_MILLISECONDS = 5000; // 5 seconds
    private static final int TIME_ANALYSIS_MILISECONDS = 25000; //25 seconds 
    private static final int TIME_DAY_MILISECONDS = 60000; //60 seconds
    private static final int TIME_DAY_MILISECONDS_ERROR = 1000; //1 seconds
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS");
    private static final double PERCENTAGE = 80;
    private List<Action> actions;
    private List<String> types_Crops;


    public GotaRule() {
        actions = new ArrayList<>();
        actions.add(new ActionDroneFique());
        actions.add(new ActionSendAlert());
        types_Crops= new ArrayList<>();
        types_Crops.add("Papa");
    }
    
    @Override
    public List<String> getTypes_Crops() {
        return types_Crops;
    }

    @Override
    public void setTypes_Crops(List<String> types_Crops) {
        this.types_Crops = types_Crops;
    }
    
    @Override
    public void execute(HashMap<String, String> data,DecisionTreeModel model) {
        System.out.println("entró");
        String idLandlot = data.get("idLandlot");
        RuleAnalysis(idLandlot, data.get("humidityData"), data.get("temperatureData"),data.get("first_time"));
    }

    private void RuleAnalysis(String idLandlot, String humidityData, String temperatureData,String first_time) {
        
        Date now = new Date();
        long now_long = now.getTime();     
        String analysisString = ExternalMethods.getValueOfRedis("analysisString", idLandlot);
        String analysisValue = ExternalMethods.getValueOfRedis("analysisValue", idLandlot);
        String start_time = ExternalMethods.getValueOfRedis("start_time", idLandlot);
        System.out.println("STARTTIME :O"+ start_time);
        String condition="-"; 
        int value=0;
        String landlot_name = ExternalMethods.getLandlotNameCassandra(idLandlot);
        //Mirar si cumple la condición
        if (landlot_name.equals("Papa") && Double.parseDouble(humidityData) >= 90 && Double.parseDouble(temperatureData) >= 10) {
            condition="+";
            value=1;
        }
            //La primera vez
        if (analysisString == null || start_time == null) {
                analysisString = condition;
                analysisValue = String.valueOf(value);
                //Inicializar fecha
                ExternalMethods.saveToRedis("start_time", idLandlot, String.valueOf(now_long));
                start_time=String.valueOf(now_long);

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
                        String warning = ExternalMethods.getValueOfRedis("Warning", idLandlot);
                        //hay un warning?
                        //si --> no hacer nada
                        //no --> agregar
                        if (warning == null || warning.equals("-")) {
                            ExternalMethods.saveToRedis("Warning", idLandlot, "+");
                        }
                    }

                    if (now_long - Double.parseDouble(start_time) > TIME_DAY_MILISECONDS + STREAM_WINDOW_MILLISECONDS*2 ) {
                        String warning = ExternalMethods.getValueOfRedis("Warning", idLandlot);
                        //Por ser dia 2 apenas se cumplan las horas es porque hay alarma
                        //ALARMA
                        for (Action ac : actions) {
                            ac.setIdLandlot(idLandlot);
                            ac.execute();
                        }
                        ExternalMethods.saveToRedis("Warning", idLandlot, "-");
                        analysisString = "";
                        analysisValue = "0";
                        now = new Date();
                        now_long = now.getTime();
                        ExternalMethods.saveToRedis("start_time", idLandlot, String.valueOf(now_long));
                    }

                }
                analysisValue = String.valueOf(Integer.parseInt(analysisValue) + addValue);
            } //Mientras no haya pasado el tiempo
            else {
                analysisString += condition;
                analysisValue = String.valueOf(Integer.parseInt(analysisValue) + value);
            }

            //Si no cumple la condición (humedad)

        ExternalMethods.saveToRedis("analysisString", idLandlot, analysisString);
        ExternalMethods.saveToRedis("analysisValue", idLandlot, analysisValue);
        String warning = ExternalMethods.getValueOfRedis("Warning", idLandlot);
        System.out.println("start_time :o "+start_time);
        //SI es el primer dia
        boolean day1=(TIME_DAY_MILISECONDS - TIME_DAY_MILISECONDS_ERROR)< (now_long - Double.parseDouble(start_time)) && (now_long - Double.parseDouble(start_time)) < (TIME_DAY_MILISECONDS + TIME_DAY_MILISECONDS_ERROR);
        boolean day2=(TIME_DAY_MILISECONDS*2 - TIME_DAY_MILISECONDS_ERROR)< (now_long - Double.parseDouble(start_time)) && (now_long - Double.parseDouble(start_time)) < (TIME_DAY_MILISECONDS*2 + TIME_DAY_MILISECONDS_ERROR);
        if (day1){
            if (warning == null || warning.equals("-")) {
            //si no hubo alarma reiniciar
            ExternalMethods.saveToRedis("start_time", idLandlot, String.valueOf(now_long));
            }
            ExternalMethods.saveToRedis("analysisString", idLandlot, "");
            ExternalMethods.saveToRedis("analysisValue", idLandlot, "0");
        } 
        if (day2){
            ExternalMethods.saveToRedis("Warning", idLandlot, "-");
            ExternalMethods.saveToRedis("start_time", idLandlot, String.valueOf(now_long));
            ExternalMethods.saveToRedis("analysisString", idLandlot, "");
            ExternalMethods.saveToRedis("analysisValue", idLandlot, "0");
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


  

}
