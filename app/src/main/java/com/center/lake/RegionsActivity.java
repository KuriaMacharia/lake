package com.center.lake;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.center.lake.Helper.HttpJsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegionsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private static final String KEY_SUCCESS = "success";
    private static final String KEY_DATA = "data";
    private static final String KEY_COUNTY = "county";
    private static final String KEY_COUNTY_CODE = "co_number";
    private static final String KEY_PROVINCE_NUMBER = "pro_number";

    private static final String KEY_COUNTY_NUMBER = "co_number";
    private static final String KEY_REGION = "region";
    private static final String KEY_REGION_NUMBER = "re_number";
    private static final String KEY_DIRECTION_CODE = "di_code";
    private static final String KEY_REGION_CODE = "re_code";

    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_FULL_NUMBER = "full_number";

    private static final String BASE_URL = "http://www.anwani.net/seya/";

    String[] direction = {"---Select---", "North", "South","East", "West", "Center"};

    String mainCounty, countyNumber, provinceNumber, selectedCounty, selectedDirection, directionCode,
            proCode, coCode, finalRegionNumber, finalRegionName, regionNumberOnly;

    Spinner countySpin, directionSpin;
    EditText regionEdt, codeEdt;
    TextView regionNameTxt, regionCodeTxt;
    Button generateBtn, saveBtn;
    private ArrayList<HashMap<String, String>> listCounties;
    ArrayList<String> listOnlyCounty;
    int success;
    ProgressDialog UstartDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regions);

        listCounties= new ArrayList<>();
        listOnlyCounty= new ArrayList<String>();
        UstartDialog = new ProgressDialog(RegionsActivity.this, R.style.mydialog);

        countySpin=(Spinner) findViewById(R.id.spin_county_region);
        directionSpin=(Spinner) findViewById(R.id.spin_direction_region);
        regionEdt=(EditText) findViewById(R.id.edt_name_region);
        codeEdt=(EditText) findViewById(R.id.edt_code_region);
        regionNameTxt=(TextView) findViewById(R.id.txt_region_name_region);
        regionCodeTxt=(TextView) findViewById(R.id.txt_region_number_region);
        generateBtn=(Button) findViewById(R.id.btn_generate_region);
        saveBtn=(Button) findViewById(R.id.btn_save_region);

        selectedCounty="None";
        selectedDirection="None";

        new LoadCounties().execute();
        countySpin.setOnItemSelectedListener(this);
        directionSpin.setOnItemSelectedListener(this);

        ArrayAdapter dir = new ArrayAdapter(RegionsActivity.this, android.R.layout.simple_spinner_item, direction);
        dir.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        directionSpin.setAdapter(dir);

        generateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!selectedCounty.contentEquals("None") && !selectedDirection.contentEquals("None")
                && !regionEdt.getText().toString().isEmpty() && !codeEdt.getText().toString().isEmpty()){

                    finalRegionName= regionEdt.getText().toString() + ", " + selectedCounty;
                    finalRegionNumber = proCode + coCode + "." + directionCode + codeEdt.getText().toString();

                    regionNumberOnly= proCode + coCode +  directionCode + codeEdt.getText().toString();
                    regionNameTxt.setText(finalRegionName);
                    regionCodeTxt.setText(finalRegionNumber);
                }
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AddRegion().execute();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        selectedCounty = String.valueOf(countySpin.getSelectedItem());
        selectedDirection = String.valueOf(directionSpin.getSelectedItem());

        if (selectedDirection.contentEquals("North")){
            directionCode="2";

        }else if (selectedDirection.contentEquals("South")){
            directionCode="6";

        }else if (selectedDirection.contentEquals("East")){
            directionCode="4";

        }else if (selectedDirection.contentEquals("West")){
            directionCode="8";

        }else if (selectedDirection.contentEquals("Center")){
            directionCode="1";

        }else{
            selectedDirection="None";
        }

        if (selectedCounty.contentEquals("---Select---")){
            selectedCounty="None";

        }else{
            for (int j = 0; j < listCounties.size(); j++) {
                if(selectedCounty.contentEquals(listCounties.get(j).get(KEY_COUNTY))){
                    coCode= listCounties.get(j).get(KEY_COUNTY_NUMBER);
                    proCode= listCounties.get(j).get(KEY_PROVINCE_NUMBER);
                }
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private class LoadCounties extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "fetch_counties.php", "GET", null);
            try {
                success = jsonObject.getInt(KEY_SUCCESS);
                if (success == 1) {
                    JSONArray incidences = jsonObject.getJSONArray(KEY_DATA);

                    for (int i = 0; i < incidences.length(); i++) {
                        JSONObject incidence = incidences.getJSONObject(i);

                        mainCounty = incidence.getString(KEY_COUNTY);
                        countyNumber = incidence.getString(KEY_COUNTY_NUMBER);
                        provinceNumber = incidence.getString(KEY_PROVINCE_NUMBER);

                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(KEY_COUNTY, mainCounty);
                        map.put(KEY_COUNTY_NUMBER, countyNumber);
                        map.put(KEY_PROVINCE_NUMBER, provinceNumber);
                        listCounties.add(map);
                        listOnlyCounty.add(mainCounty);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String result) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (success == 1) {
                        ArrayAdapter scsc = new ArrayAdapter(RegionsActivity.this, android.R.layout.simple_spinner_item, listOnlyCounty);
                        scsc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        countySpin.setAdapter(scsc);


                    } else {
                        Toast.makeText(RegionsActivity.this, "Error loading addresses", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private class AddRegion extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            UstartDialog.setMessage("Adding. Please wait...");
            UstartDialog.setIndeterminate(false);
            UstartDialog.setCancelable(false);
            UstartDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();

            httpParams.put(KEY_REGION, regionEdt.getText().toString());
            httpParams.put(KEY_REGION_NUMBER, finalRegionNumber);
            httpParams.put(KEY_REGION_CODE, codeEdt.getText().toString());
            httpParams.put(KEY_DIRECTION_CODE, directionCode);
            httpParams.put(KEY_COUNTY, selectedCounty);
            httpParams.put(KEY_COUNTY_NUMBER, proCode + coCode);
            httpParams.put(KEY_FULL_NAME, finalRegionName);
            httpParams.put(KEY_FULL_NUMBER, regionNumberOnly);

            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "add_region.php", "POST", httpParams);
            if(success==1)
                try {
                    success = jsonObject.getInt(KEY_SUCCESS);

                }catch (JSONException e) {
                    e.printStackTrace();
                }
            return null;
        }

        protected void onPostExecute(String result) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (success == 1) {
                        Toast.makeText(RegionsActivity.this,"Road Added",Toast.LENGTH_LONG).show();
                        startActivity(new Intent(RegionsActivity.this, RegionsActivity.class));
                        UstartDialog.dismiss();
                    } else {
                        Toast.makeText(RegionsActivity.this,"Address Adding Failed",Toast.LENGTH_LONG).show();
                        UstartDialog.dismiss();
                    }
                }
            });
        }
    }
}
