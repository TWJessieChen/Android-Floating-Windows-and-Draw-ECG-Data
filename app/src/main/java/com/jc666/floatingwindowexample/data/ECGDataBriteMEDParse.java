package com.jc666.floatingwindowexample.data;

import android.content.Context;
import android.content.res.AssetManager;
import com.britemed.btecganaar.ecgreport.ECGReportJsonFormat;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JC666
 * @date 2023/01/10
 * @describe TODO
 */

public class ECGDataBriteMEDParse {

    private ECGReportJsonFormat values;

    private List<ECGReportDataFormat> valuesOneLeadTest = new ArrayList<>();

    public ECGDataBriteMEDParse(Context context){
        String json = parseJson(context,"ecgDataBriteMED.json");
        Gson gson = new Gson();
        values = gson.fromJson(json,  ECGReportJsonFormat.class);

        for(int index = 0; index < values.size(); index++) {
            ECGReportDataFormat data = new ECGReportDataFormat(
                values.get(index).getEcg(),
                values.get(index).isPacemaker(),
                values.get(index).getLeadOff()
            );
            valuesOneLeadTest.add(data);
        }

    }

    private static String parseJson(Context context, String fileName) {

        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assetManager = context.getAssets();
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public List<ECGReportDataFormat> getValuesOneLeadTest() {
        return valuesOneLeadTest;
    }

}
