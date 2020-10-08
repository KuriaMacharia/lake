package com.center.lake;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.center.lake.Helper.HttpJsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RoadsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public static final String county1 = "countyKey";
    public static final String coCode1 = "coCodeKey";
    public static final String proCode1 = "proCodeKey";
    public static final String MyPREFERENCES = "MyPrefs" ;

    private static final String KEY_SUCCESS = "success";
    private static final String KEY_DATA = "data";
    private static final String KEY_ROAD = "road";
    private static final String KEY_ROAD_NUMBER = "road_number";
    private static final String KEY_CODE = "rcode";
    private static final String KEY_COUNTY = "county";
    private static final String KEY_COUNTY_NUMBER = "co_number";
    private static final String KEY_DIRECTION_CODE = "di_code";
    private static final String KEY_PROVINCE_NUMBER = "pro_number";
    private static final String KEY_ROAD_UNIQUE = "road_unique";

    private static final String BASE_URL = "http://www.anwani.net/seya/";

    String[] category = {"---Select---", "Class A", "Class B","Class C", "Class D", "Class E","Class SRP", "Class U",
                        "County Roads Town", "County Roads Outskirts"};
    String[] direction = {"---Select---", "North", "South","East", "West", "Center"};
    String rCode, selectedClass, theRoad, theCode, selectedCounty, selectedDirection, mainCounty, countyNumber,
        provinceNumber, directionCode, coCode, proCode, theNumber, finalRoadNumber, roadUnique, pos;
    int success;
    ProgressDialog UstartDialog;

    EditText nameEdt, numberEdt;
    TextView previewTxt, roadCount;
    Spinner classSpin, countySpin, directionSpin;
    Button saveBtn, generateBtn;
    ListView roadList;
    private ArrayList<HashMap<String, String>> listRoads;
    private ArrayList<HashMap<String, String>> listCounties;
    ArrayList<String> listOnlyCounty;
    ArrayList<String> list,listR;
    List<String> directionList;
    int mCounterL, mCounterR, addressNumber, previousCounterR;
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roads);

        sharedpreferences = getSharedPreferences(RoadsActivity.MyPREFERENCES, Context.MODE_PRIVATE);
        selectedCounty = sharedpreferences.getString(county1,"");
        coCode = sharedpreferences.getString(coCode1,"");
        proCode = sharedpreferences.getString(proCode1,"");

        listRoads= new ArrayList<>();
        listCounties= new ArrayList<>();
        listOnlyCounty= new ArrayList<String>();
        list= new ArrayList<>();
        listR= new ArrayList<>();
        directionList= new ArrayList<>();
        directionList= Arrays.asList(category);

        nameEdt=(EditText) findViewById(R.id.ed_road_name);
        previewTxt=(TextView) findViewById(R.id.txt_road_preview);
        classSpin=(Spinner) findViewById(R.id.spin_road_category);
        saveBtn=(Button) findViewById(R.id.btn_save_road);
        generateBtn=(Button) findViewById(R.id.btn_generate_road);
        roadCount=(TextView) findViewById(R.id.txt_road_count);
        roadList=(ListView) findViewById(R.id.list_roads);
        countySpin=(Spinner) findViewById(R.id.spin_counties_roads);
        directionSpin=(Spinner) findViewById(R.id.spin_direction_roads);
        numberEdt=(EditText) findViewById(R.id.edt_road_number);

        UstartDialog = new ProgressDialog(RoadsActivity.this, R.style.mydialog);

        selectedCounty="None";
        selectedDirection="None";

        new LoadCounties().execute();
        countySpin.setOnItemSelectedListener(this);
        directionSpin.setOnItemSelectedListener(this);

        ArrayAdapter dir = new ArrayAdapter(RoadsActivity.this, android.R.layout.simple_spinner_item, direction);
        dir.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        directionSpin.setAdapter(dir);

        classSpin.setOnItemSelectedListener(this);
        ArrayAdapter cty = new ArrayAdapter(RoadsActivity.this, android.R.layout.simple_spinner_item, category);
        cty.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classSpin.setAdapter(cty);
        rCode="None";

        new LoadRoads().execute();

        generateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCounterR = 1;
                /*if (previousCounterR == 0){
                    addressNumber = mCounterR;
                }else{
                    addressNumber = previousCounterR + 1;
                }*/

                addressNumber=Integer.parseInt(numberEdt.getText().toString());

                finalRoadNumber=String.format("%03d", addressNumber);
                roadUnique=proCode+coCode+directionCode+ rCode+ finalRoadNumber;
                previewTxt.setText(roadUnique);

                saveBtn.setVisibility(View.VISIBLE);
                generateBtn.setVisibility(View.GONE);
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nameEdt.getText().toString().isEmpty() || rCode.contentEquals("None")){
                    Toast.makeText(RoadsActivity.this,"Missing Field",Toast.LENGTH_LONG).show();
                }else{
                    new AddRoad().execute();
                }
            }
        });

        previewTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LoadPrevious().execute();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        selectedClass = String.valueOf(classSpin.getSelectedItem());

        if (selectedClass.contentEquals("Class A")){
            rCode="1";

        }else if (selectedClass.contentEquals("Class B")){
            rCode="2";

        }else if (selectedClass.contentEquals("Class C")){
            rCode="3";

        }else if (selectedClass.contentEquals("Class D")){
            rCode="4";

        }else if (selectedClass.contentEquals("Class E")){
            rCode="5";

        }else if (selectedClass.contentEquals("Class SRP")){
            rCode="6";

        }else if (selectedClass.contentEquals("Class U")){
            rCode="7";

        }else if (selectedClass.contentEquals("County Roads Town")){
            rCode="8";

        }else if (selectedClass.contentEquals("County Roads Outskirts")){
            rCode="9";

        }else{
            rCode="None";
        }

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
                        ArrayAdapter scsc = new ArrayAdapter(RoadsActivity.this, android.R.layout.simple_spinner_item, listOnlyCounty);
                        scsc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        countySpin.setAdapter(scsc);


                    } else {
                        Toast.makeText(RoadsActivity.this, "Error loading addresses", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private class AddRoad extends AsyncTask<String, String, String> {
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

            httpParams.put(KEY_ROAD, nameEdt.getText().toString());
            httpParams.put(KEY_CODE, rCode);

            httpParams.put(KEY_ROAD_NUMBER, finalRoadNumber);
            httpParams.put(KEY_DIRECTION_CODE, directionCode);

            httpParams.put(KEY_COUNTY, selectedCounty);
            httpParams.put(KEY_COUNTY_NUMBER, coCode);

            httpParams.put(KEY_PROVINCE_NUMBER, proCode);
            httpParams.put(KEY_ROAD_UNIQUE, roadUnique);

            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "add_main_road.php", "POST", httpParams);
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
                        Toast.makeText(RoadsActivity.this,"Road Added",Toast.LENGTH_LONG).show();
                        startActivity(new Intent(RoadsActivity.this, RoadsActivity.class));
                        UstartDialog.dismiss();
                    } else {
                        Toast.makeText(RoadsActivity.this,"Address Adding Failed",Toast.LENGTH_LONG).show();
                        UstartDialog.dismiss();
                    }
                }
            });
        }
    }

    private class LoadRoads extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "fetch_roads.php", "GET", null);
            try {
                success = jsonObject.getInt(KEY_SUCCESS);
                if (success == 1) {
                    JSONArray incidences = jsonObject.getJSONArray(KEY_DATA);

                    for (int i = 0; i < incidences.length(); i++) {
                        JSONObject incidence = incidences.getJSONObject(i);

                        theRoad = incidence.getString(KEY_ROAD);
                        theCode = incidence.getString(KEY_CODE);

                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(KEY_ROAD, theRoad);
                        map.put(KEY_CODE, theCode);
                        listRoads.add(map);
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
                        PopulateList();
                    } else {
                        Toast.makeText(RoadsActivity.this, "Error loading addresses", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void PopulateList(){
        roadCount.setText(String.valueOf(listRoads.size()));

        ListAdapter adapter = new SimpleAdapter(
                RoadsActivity.this, listRoads, R.layout.single_road,
                new String[]{KEY_ROAD, KEY_CODE},
                new int[]{R.id.txt_road_name, R.id.txt_road_code});

        roadList.setAdapter(adapter);

        roadList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                nameEdt.setText(((TextView) view.findViewById(R.id.txt_road_name)).getText().toString());
                String code=((TextView) view.findViewById(R.id.txt_road_code)).getText().toString();

                if (code.contentEquals("1")){
                    pos="Class A";

                }else if (code.contentEquals("2")){
                    pos="Class B";

                }else if (code.contentEquals("3")){
                    pos="Class C";

                }else if (code.contentEquals("4")){
                    pos="Class D";

                }else if (code.contentEquals("5")){
                    pos="Class E";

                }else if (code.contentEquals("6")){
                    pos="Class SRP";

                }else if (code.contentEquals("7")){
                    pos="Class U";

                }else if (code.contentEquals("8")){
                    pos="County Roads Town";

                }else if (code.contentEquals("9")){
                    pos="County Roads Outskirts";

                }else{
                    code="None";
                }

                classSpin.setSelection(directionList.indexOf(pos));
            }
        });
    }

    private class LoadPrevious extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            UstartDialog.setMessage("Generating Number. Please wait...");
            UstartDialog.setIndeterminate(false);
            UstartDialog.setCancelable(false);
            UstartDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put(KEY_CODE, rCode);
            httpParams.put(KEY_DIRECTION_CODE, directionCode);
            httpParams.put(KEY_COUNTY, selectedCounty);
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "fetch_previous_roads.php", "GET", httpParams);
            try {
                success = jsonObject.getInt(KEY_SUCCESS);
                if (success == 1) {
                    JSONArray incidences = jsonObject.getJSONArray(KEY_DATA);

                    for (int i = 0; i < incidences.length(); i++) {
                        JSONObject incidence = incidences.getJSONObject(i);
                        theNumber = incidence.getString(KEY_ROAD_NUMBER);
                        list.add(theNumber);
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
                        if(list.size()>0) {
                            Collections.sort(list);
                            if(list.size()!=0) {
                                previousCounterR = Integer.parseInt(list.get(list.size()-1));
                            }
                        }else{
                            previousCounterR = 0;
                        }
                        generateBtn.setVisibility(View.VISIBLE);
                        previewTxt.setText("Preview Road");
                        previewTxt.setEnabled(false);

                    } else {
                        generateBtn.setEnabled(false);

                        Toast.makeText(RoadsActivity.this,"Initializing Failed",Toast.LENGTH_LONG).show();

                    }

                    UstartDialog.dismiss();
                }
            });
        }
    }
}
