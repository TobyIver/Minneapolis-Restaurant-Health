package com.iverson.toby.rhealth;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Toby on 4/25/2015.
 */



    // Pulls health inspection data from API
    public class HealthAPI extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... args) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse hresponse;
            String responseString = null;
            try {
                hresponse = httpclient.execute(new HttpGet(args[0]));
                StatusLine statusLine = hresponse.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    hresponse.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                } else {
                    //Closes the connection.
                    hresponse.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
                Log.e("ERROR", e.getMessage());
            } catch (IOException e) {
                Log.e("ERROR", e.getMessage());
            }
            return responseString;


        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            JSONObject jsonObject = null;
            try {
                //pulling information out of Health JSON
                JSONArray jarray = new JSONArray(result);
                placeItem.setVRating(101);
                for (int i = 0; i < jarray.length(); i++) {
                    JSONObject j = jarray.getJSONObject(i);

                    String date = j.getString("date_of_inspection");
                    String critical = j.getString("critical");
                    String vname = j.getString("name_of_business");
                    String codeViolation = j.getString("code_section");
                    String riskLevel = j.getString("risk_level");
                    String address = j.getString("license_address");
                    String violationText = j.getString("standard_order_text");

                    //verify violation results to prevent errors
                    String paddress = placeItem.getVicinity();
                    paddress = paddress.toUpperCase();
                    paddress = paddress.substring(0, paddress.indexOf(" "));
                    String vaddress = address.substring(0, address.indexOf(" "));

                    if (vaddress.equals(paddress)) {
                        Violation violation = new Violation();
                        violation.setName(vname);
                        violation.setAddress(address);
                        violation.setDate(date);
                        violation.setRiskLevel(riskLevel);
                        violation.setViolationText(violationText);
                        violation.setCodeViolation(codeViolation);
                        violation.setCritial(critical);


                        // adding risk level to rating
                        int r = 0;
                        r = 2 * (4 - Integer.parseInt(riskLevel));  //risk levels 1-3, 1 being worse
                        if (critical.equals("Yes")) {
                            r = r * 3;
                        }
                        violation.setRating(r);
                        placeItem.setVRating(placeItem.getVRating() - r);

                        CurrentViolations.add(violation);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            TextView itemName = (TextView) findViewById(R.id.item_name);
            TextView itemVicinity = (TextView) findViewById(R.id.item_vicinity);
            TextView itemVRating = (TextView) findViewById(R.id.item_vrating);
            TextView itemRating = (TextView) findViewById(R.id.item_rating);
            TextView itemNegative = (TextView) findViewById(R.id.item_negative);

            itemRating.setText(Float.toString(placeItem.getRating()) + "/5 Google Rating");
            itemName.setText(placeItem.getName());
            itemVicinity.setText(placeItem.getVicinity());
            itemVRating.setText(Integer.toString(placeItem.getVRating()) + "/100 Health Score");
            if(placeItem.getVRating() < 0){
                itemNegative.setText("Yes negative numbers are bad");
            }
            Button clickButton = (Button) findViewById(R.id.violations_button);
            clickButton.setOnClickListener( new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    setContentView(R.layout.violation_layout);



                    ViolationAdapter violationAdapter = new ViolationAdapter(MainActivity.this, R.layout.activity_main, violations);
                    violationView = (ListView)findViewById(R.id.violation_listview);
                    violationView.setAdapter(violationAdapter);

                    Button returnButton = (Button) findViewById(R.id.back_button);
                    returnButton.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            setContentView(R.layout.item_selected);

                        }
                    });


                };
            });
            Button mapButton = (Button) findViewById(R.id.map_button);
            mapButton.setOnClickListener( new View.OnClickListener() {

                @Override
                public void onClick(View v) {


                    float[] geometry =  placeItem.getGeometry();

                    String uri = String.format(Locale.ENGLISH, "geo:"+ geometry[0] + "," + geometry[1]);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    startActivity(intent);

                    Toast.makeText(getApplicationContext(), "loading map " + uri,
                            Toast.LENGTH_LONG).show();

                }
            });
        }
    }