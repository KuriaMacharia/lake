package com.center.lake;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.center.lake.Helper.CheckNetWorkStatus;
import com.center.lake.Helper.HttpJsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RoutesActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String KEY_SUCCESS = "success";
    private static final String KEY_DATA = "data";

    private static final String KEY_COUNTY = "county";
    private static final String KEY_PROVINCE_NUMBER = "pro_number";
    private static final String KEY_COUNTY_NUMBER = "co_number";

    private static final String KEY_REGION = "region";
    private static final String KEY_REGION_NUMBER = "full_number";

    private static final String KEY_ROAD = "road";
    private static final String KEY_ROAD_CODE = "rcode";
    private static final String KEY_ROAD_NUMBER = "road_number";

    private static final String KEY_ROUTE_NUMBER = "route_number";
    private static final String KEY_UNIQUE_NUMBER = "unique_number";
    private static final String KEY_DIRECTION_CODE = "di_code";

    private static final String KEY_START = "start";

    private static final String BASE_URL = "http://www.anwani.net/seya/";

    String[] direction = {"---Select---", "North", "South","East", "West", "Center"};

    String mainCounty, countyNumber, provinceNumber, selectedCounty, selectedDirection, directionCode, coCode, proCode,
            theRegion, theRegionNumber, theRoad, theCode, selectedRegion, selectedRoad, regionCode, roadCode, roadNumber,
            routeNumber, routeUnique, theRoadNumber, searchChar;
    int success;

    Spinner countySpin, directionSpin, regionSpin, roadSpin;
    Button generateBtn, saveBtn;
    TextView previewTxt;
    private ArrayList<HashMap<String, String>> listCounties;
    private ArrayList<HashMap<String, String>> listRoads, listRegions, listAllRoads;
    ArrayList<String> listOnlyCounty, listOnlyRegions, listOnlyRoads, listNameRoads;
    ProgressDialog UstartDialog;
    ImageView homeImg, refreshImg;
    AutoCompleteTextView searchEdt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);

        listCounties= new ArrayList<>();
        listOnlyCounty= new ArrayList<String>();
        listRoads= new ArrayList<>();
        listRegions= new ArrayList<>();
        listOnlyRoads= new ArrayList<String>();
        listOnlyRegions= new ArrayList<String>();

        listNameRoads= new ArrayList<>();
        listAllRoads= new ArrayList<>();
        UstartDialog = new ProgressDialog(RoutesActivity.this, R.style.mydialog);

        homeImg=(ImageView) findViewById(R.id.img_home);
        refreshImg=(ImageView) findViewById(R.id.img_refresh);

        countySpin=(Spinner) findViewById(R.id.spin_county_routes);
        directionSpin=(Spinner) findViewById(R.id.spin_direction_routes);
        regionSpin=(Spinner) findViewById(R.id.spin_regions_routes);
        roadSpin=(Spinner) findViewById(R.id.spin_roads_routes);
        generateBtn=(Button) findViewById(R.id.btn_generate_routes);
        saveBtn=(Button) findViewById(R.id.btn_save_routes);
        previewTxt=(TextView) findViewById(R.id.txt_preview_routes);
        searchEdt = (AutoCompleteTextView) findViewById(R.id.edt_search_road);

        selectedCounty="None";
        selectedDirection="None";

        new LoadCounties().execute();
        countySpin.setOnItemSelectedListener(this);
        directionSpin.setOnItemSelectedListener(this);
        regionSpin.setOnItemSelectedListener(this);
        roadSpin.setOnItemSelectedListener(this);

        ArrayAdapter dir = new ArrayAdapter(RoutesActivity.this, android.R.layout.simple_spinner_item, direction);
        dir.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        directionSpin.setAdapter(dir);

        homeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RoutesActivity.this, MainActivity.class));
            }
        });

        refreshImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RoutesActivity.this, RoutesActivity.class));
            }
        });

        searchEdt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (CheckNetWorkStatus.isNetworkAvailable(RoutesActivity.this)) {
                    generateBtn.setEnabled(true);
                }else{
                    Toast.makeText(RoutesActivity.this, "Unable to connect to internet", Toast.LENGTH_LONG).show();
                }
            }
        });

        searchEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                generateBtn.setEnabled(false);
                if(charSequence.toString().length()==2){
                    searchChar=searchEdt.getText().toString();
                    new SearchRoads().execute();
                }else{
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        generateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String myRoute = regionCode + "." + roadCode +roadNumber;
                previewTxt.setText(myRoute);
                routeNumber=regionCode + "." + selectedRoad+roadNumber;
                routeUnique=regionCode + roadCode + roadNumber;

                previewTxt.setVisibility(View.VISIBLE);
                saveBtn.setVisibility(View.VISIBLE);
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AddRoute().execute();
            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        selectedDirection = String.valueOf(directionSpin.getSelectedItem());

        if (!selectedCounty.contentEquals("---Select---")) {
            if (selectedDirection.contentEquals("North")) {
                new LoadRegions().execute();
                new LoadRoads().execute();
                directionCode = "2";

            } else if (selectedDirection.contentEquals("South")) {
                new LoadRegions().execute();
                new LoadRoads().execute();
                directionCode = "6";

            } else if (selectedDirection.contentEquals("East")) {
                new LoadRegions().execute();
                new LoadRoads().execute();
                directionCode = "4";

            } else if (selectedDirection.contentEquals("West")) {
                new LoadRegions().execute();
                new LoadRoads().execute();
                directionCode = "8";

            } else if (selectedDirection.contentEquals("Center")) {
                new LoadRegions().execute();
                new LoadRoads().execute();
                directionCode = "1";

            } else {
                selectedDirection = "None";
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
                    listOnlyCounty.add("---Select---");

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
                        ArrayAdapter scsc = new ArrayAdapter(RoutesActivity.this, android.R.layout.simple_spinner_item, listOnlyCounty);
                        scsc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        countySpin.setAdapter(scsc);

                        countySpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                selectedCounty = String.valueOf(countySpin.getSelectedItem());
                                if (!selectedCounty.contentEquals("---Select---")) {
                                    generateBtn.setVisibility(View.GONE);
                                    for (int j = 0; j < listCounties.size(); j++) {
                                        if (selectedCounty.contentEquals(listCounties.get(j).get(KEY_COUNTY))) {
                                            coCode = listCounties.get(j).get(KEY_COUNTY_NUMBER);
                                            proCode = listCounties.get(j).get(KEY_PROVINCE_NUMBER);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });


                    } else {
                        Toast.makeText(RoutesActivity.this, "Error loading addresses", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private class LoadRegions extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            UstartDialog.setMessage("Fetching regions. Please wait...");
            UstartDialog.setIndeterminate(false);
            UstartDialog.setCancelable(false);
            UstartDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put(KEY_DIRECTION_CODE, directionCode);
            httpParams.put(KEY_COUNTY, selectedCounty);
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "fetch_regions.php", "GET", httpParams);
            try {
                success = jsonObject.getInt(KEY_SUCCESS);
                if (success == 1) {
                    JSONArray incidences = jsonObject.getJSONArray(KEY_DATA);
                    listOnlyRegions.add("---Select---");

                    for (int i = 0; i < incidences.length(); i++) {
                        JSONObject incidence = incidences.getJSONObject(i);

                        theRegion = incidence.getString(KEY_REGION);
                        theRegionNumber = incidence.getString(KEY_REGION_NUMBER);

                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(KEY_REGION, theRegion);
                        map.put(KEY_REGION_NUMBER, theRegionNumber);
                        listRegions.add(map);
                        listOnlyRegions.add(theRegion);
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

                        ArrayAdapter scsc = new ArrayAdapter(RoutesActivity.this, android.R.layout.simple_spinner_item, listOnlyRegions);
                        scsc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        regionSpin.setAdapter(scsc);
                        regionSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                selectedRegion= String.valueOf(regionSpin.getSelectedItem());
                                if (selectedRegion.contentEquals("---Select---")){
                                    generateBtn.setVisibility(View.GONE);

                                }else{
                                    for (int j = 0; j < listRegions.size(); j++) {
                                        if(selectedRegion.contentEquals(listRegions.get(j).get(KEY_REGION))){
                                            regionCode= listRegions.get(j).get(KEY_REGION_NUMBER);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });
                    } else {

                        Toast.makeText(RoutesActivity.this,"Initializing Failed",Toast.LENGTH_LONG).show();

                    }

                    UstartDialog.dismiss();
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
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put(KEY_DIRECTION_CODE, directionCode);
            httpParams.put(KEY_COUNTY, selectedCounty);
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "fetch_roads_routes.php", "GET", httpParams);
            try {
                success = jsonObject.getInt(KEY_SUCCESS);
                if (success == 1) {
                    JSONArray incidences = jsonObject.getJSONArray(KEY_DATA);
                    listOnlyRoads.add("---Select---");

                    for (int i = 0; i < incidences.length(); i++) {
                        JSONObject incidence = incidences.getJSONObject(i);

                        theRoad = incidence.getString(KEY_ROAD);
                        theCode = incidence.getString(KEY_ROAD_CODE);
                        theRoadNumber = incidence.getString(KEY_ROAD_NUMBER);

                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(KEY_ROAD, theRoad);
                        map.put(KEY_ROAD_CODE, theCode);
                        map.put(KEY_ROAD_NUMBER, theRoadNumber);
                        listRoads.add(map);
                        listOnlyRoads.add(theRoad);
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
                        ArrayAdapter scsc = new ArrayAdapter(RoutesActivity.this, android.R.layout.simple_spinner_item, listOnlyRoads);
                        scsc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        roadSpin.setAdapter(scsc);

                        roadSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                selectedRoad= String.valueOf(roadSpin.getSelectedItem());

                                if (selectedRoad.contentEquals("---Select---")){
                                    generateBtn.setVisibility(View.GONE);

                                }else{
                                    if(listOnlyRoads.size()>0) {
                                        for (int j = 0; j < listRoads.size(); j++) {
                                            if (selectedRoad.contentEquals(listRoads.get(j).get(KEY_ROAD))) {
                                                roadCode = listRoads.get(j).get(KEY_ROAD_CODE);
                                                roadNumber = listRoads.get(j).get(KEY_ROAD_NUMBER);
                                            }
                                        }

                                        generateBtn.setVisibility(View.VISIBLE);
                                    }
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });

                    } else {
                        Toast.makeText(RoutesActivity.this, "Error loading addresses", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private class SearchRoads extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put(KEY_START, searchChar);
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "road_search_routes.php", "GET", httpParams);
            try {
                success = jsonObject.getInt(KEY_SUCCESS);
                if (success == 1) {
                    JSONArray incidences = jsonObject.getJSONArray(KEY_DATA);

                    for (int i = 0; i < incidences.length(); i++) {
                        JSONObject incidence = incidences.getJSONObject(i);

                        theRoad = incidence.getString(KEY_ROAD);
                        theCode = incidence.getString(KEY_ROAD_CODE);
                        theRoadNumber = incidence.getString(KEY_ROAD_NUMBER);

                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(KEY_ROAD, theRoad);
                        map.put(KEY_ROAD_CODE, theCode);
                        map.put(KEY_ROAD_NUMBER, theRoadNumber);
                        listAllRoads.add(map);
                        listNameRoads.add(theRoad);
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
                        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(RoutesActivity.this, android.R.layout.simple_dropdown_item_1line, listNameRoads);
                        searchEdt.setAdapter(dataAdapter);
                    } else {
                        Toast.makeText(RoutesActivity.this, "Error loading addresses", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private class AddRoute extends AsyncTask<String, String, String> {
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

            httpParams.put(KEY_ROAD, selectedRoad);
            httpParams.put(KEY_ROAD_CODE, roadCode);

            httpParams.put(KEY_REGION, selectedRegion);
            httpParams.put(KEY_REGION_NUMBER, regionCode);

            httpParams.put(KEY_COUNTY, selectedCounty);
            httpParams.put(KEY_COUNTY_NUMBER, coCode);
            httpParams.put(KEY_PROVINCE_NUMBER, proCode);
            httpParams.put(KEY_ROUTE_NUMBER, routeNumber);
            httpParams.put(KEY_UNIQUE_NUMBER, routeUnique);

            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "add_route.php", "POST", httpParams);
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
                        Toast.makeText(RoutesActivity.this,"Route Added",Toast.LENGTH_LONG).show();
                        startActivity(new Intent(RoutesActivity.this, RoutesActivity.class));
                        UstartDialog.dismiss();
                    } else {
                        Toast.makeText(RoutesActivity.this,"Route Adding Failed",Toast.LENGTH_LONG).show();
                        UstartDialog.dismiss();
                    }
                }
            });
        }
    }
}
