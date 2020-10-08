package com.center.lake;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.center.lake.Helper.HttpJsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    public static final String county1 = "countyKey";
    public static final String coCode1 = "coCodeKey";
    public static final String proCode1 = "proCodeKey";
    public static final String MyPREFERENCES = "MyPrefs" ;

    private static final String KEY_SUCCESS = "success";
    private static final String KEY_DATA = "data";
    private static final String KEY_COUNTY = "county";
    private static final String KEY_COUNTY_CODE = "co_number";
    private static final String KEY_PROVINCE_NUMBER = "pro_number";

    private static final String BASE_URL = "http://www.anwani.net/seya/";

    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;
    String mainCounty, countyNumber, provinceNumber, selectedCounty, coCode, proCode;

    private ArrayList<HashMap<String, String>> listCounties;
    ArrayList<String> listOnlyCounty;
    ConstraintLayout consRoads, consRegions, consRoutes;
    int success;
    Spinner countySpin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        listCounties= new ArrayList<>();
        listOnlyCounty= new ArrayList<String>();

        new LoadCounties().execute();
        consRoads=(ConstraintLayout) findViewById(R.id.cons_roads_home);
        consRegions=(ConstraintLayout) findViewById(R.id.cons_regions_home);
        consRoutes=(ConstraintLayout) findViewById(R.id.cons_routes_home);
        countySpin=(Spinner) findViewById(R.id.spin_county_main);

        consRoads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RoadsActivity.class));
            }
        });

        consRegions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RegionsActivity.class));
            }
        });

        consRoutes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RoutesActivity.class));
            }
        });
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
                        countyNumber = incidence.getString(KEY_COUNTY_CODE);
                        provinceNumber = incidence.getString(KEY_PROVINCE_NUMBER);

                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(KEY_COUNTY, mainCounty);
                        map.put(KEY_COUNTY_CODE, countyNumber);
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
                        ArrayAdapter scsc = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_item, listOnlyCounty);
                        scsc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        countySpin.setAdapter(scsc);

                        countySpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                selectedCounty = String.valueOf(countySpin.getSelectedItem());
                                if (!selectedCounty.contentEquals("---Select---")) {
                                    for (int j = 0; j < listCounties.size(); j++) {
                                        if (selectedCounty.contentEquals(listCounties.get(j).get(KEY_COUNTY))) {
                                            coCode = listCounties.get(j).get(KEY_COUNTY_CODE);
                                            proCode = listCounties.get(j).get(KEY_PROVINCE_NUMBER);
                                        }
                                    }

                                    editor = sharedpreferences.edit();
                                    editor.putString(county1, selectedCounty);
                                    editor.putString(coCode1, coCode);
                                    editor.putString(proCode1, proCode);
                                    editor.commit();
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });


                    } else {
                        Toast.makeText(MainActivity.this, "Error loading addresses", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
