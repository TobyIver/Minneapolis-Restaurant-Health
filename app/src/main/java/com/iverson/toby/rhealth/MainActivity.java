package com.iverson.toby.rhealth;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import org.xml.sax.InputSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivity extends Activity {


    private String latitude;
    private String longitude;
    private final String APIKEY = "AIzaSyAMfjDmxGTWke6GgwZS0RbG3zg1Jjl1mtg";
    private final int radius = 2000;
    private String type = "food";
    private StringBuilder queryGoogle = new StringBuilder();
    public ArrayList<Place> places = new ArrayList<Place>();
    private ListView listView;
    private ListView violationView;
    MyLocation myLocation = new MyLocation();
    MyLocation.LocationResult locationResult;
    ProgressDialog progressDialog = null;
    Context context ;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        CurrentViolations.start();
        super.onCreate(savedInstanceState);

        openMenu();

    }
//opening page
    public void  openMenu() {

        setContentView(R.layout.open);

        final EditText restSearch = (EditText) findViewById(R.id.editsearch);
        Button sbtn = (Button) findViewById(R.id.search_button);
        Button lbtn = (Button) findViewById(R.id.location_button);


        // search button
        sbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String psearch = restSearch.getText().toString();


                setContentView(R.layout.httptestlist);

                //getting rid of special characters
                psearch = psearch.toUpperCase();
                psearch = psearch.replaceAll(" ", "%20");
                psearch = psearch.replaceAll("&", "%26");
                psearch = psearch.replaceAll("!", "%21");
                if (psearch.contains("'")) {
                    psearch = psearch.substring(0, psearch.indexOf("'"));
                }

                //creating query
                queryGoogle = new StringBuilder();
                queryGoogle.append("https://maps.googleapis.com/maps/api/place/textsearch/xml?");
                queryGoogle.append("location=44.9756997,-93.2664641&");
                queryGoogle.append("radius=10000&");
                queryGoogle.append("types=" + type + "&");
                queryGoogle.append("query=" + psearch + "&");
                queryGoogle.append("key=" + APIKEY);

                new SearchGooglePlaces().execute(queryGoogle.toString());

            }
        });
        //location button
        lbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                setContentView(R.layout.httptestlist);

                LocationManager locationManager = (LocationManager) getSystemService(
                        Context.LOCATION_SERVICE);
                LocationListener myLocationListener = new MyLocationListener();

                locationResult = new MyLocation.LocationResult() {
                    @Override
                    public void gotLocation(Location location) {
                        latitude = String.valueOf(location.getLatitude());
                        longitude = String.valueOf(location.getLongitude());
                        progressDialog.dismiss();
                        new GetCurrentLocation().execute(latitude, longitude);
                    }
                };

                /*****Enter lat lon for testing*****/
                //latitude = String.valueOf(44.9157615);
                //longitude = String.valueOf(-93.2629201);


                MyRunnable myRun = new MyRunnable();
                myRun.run();

                progressDialog = ProgressDialog.show(MainActivity.this, "Finding your location",
                        "Please wait...", true);
                queryGoogle = new StringBuilder();
                queryGoogle.append("https://maps.googleapis.com/maps/api/place/nearbysearch/xml?");
                queryGoogle.append("location=" + latitude + "," + longitude + "&");
                queryGoogle.append("radius=" + radius + "&");
                queryGoogle.append("types=" + type + "&");
                queryGoogle.append("sensor=true&"); //Must be true if queried from a device with GPS
                queryGoogle.append("key=" + APIKEY);
                new QueryGooglePlaces().execute(queryGoogle.toString());

            }
        });


    }


    private class GetCurrentLocation extends AsyncTask<Object, String, Boolean> {

        @Override
        protected Boolean doInBackground(Object... myLocationObjs) {
            if(null != latitude && null != longitude) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            assert result;
        }
    }

    /**
     * Based on: http://stackoverflow.com/questions/3505930
     */

    //gets Google places api from gps
    private class QueryGooglePlaces extends AsyncTask<String, String, String> {
        //getting the google places API XML
        @Override
        protected String doInBackground(String... args) {

            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;
            try {
                response = httpclient.execute(new HttpGet(args[0]));
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                } else {
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
                Log.e("ERROR", e.getMessage());
            } catch (IOException e) {
                Log.e("ERROR", e.getMessage());
            }
            return responseString;
        }
        // parses the xml into Places
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                places.clear();
                Document xmlResult = loadXMLFromString(result);
                NodeList nodeList =  xmlResult.getElementsByTagName("result");
                for(int i = 0, length = nodeList.getLength(); i < length; i++) {
                    Node node = nodeList.item(i);
                    if(node.getNodeType() == Node.ELEMENT_NODE) {
                        Element nodeElement = (Element) node;
                        Place place = new Place();
                        Node name = nodeElement.getElementsByTagName("name").item(0);
                        Node vicinity = nodeElement.getElementsByTagName("vicinity").item(0);
                        Node rating = nodeElement.getElementsByTagName("rating").item(0);
                        Node id = nodeElement.getElementsByTagName("id").item(0);
                        Node geometryElement = nodeElement.getElementsByTagName("geometry").item(0);
                        NodeList locationElement = geometryElement.getChildNodes();
                        Element latLngElem = (Element) locationElement.item(1);
                        Node lat = latLngElem.getElementsByTagName("lat").item(0);
                        Node lng = latLngElem.getElementsByTagName("lng").item(0);
                        float[] geometry =  {Float.valueOf(lat.getTextContent()),
                                Float.valueOf(lng.getTextContent())};

                        // put elements into array
                        place.setVicinity(vicinity.getTextContent());
                        place.setId(id.getTextContent());
                        place.setName(name.getTextContent());
                        if(null == rating) {
                            place.setRating(0.0f);
                        } else {
                            place.setRating(Float.valueOf(rating.getTextContent()));
                        }
                        place.setGeometry(geometry);
                        place.setIdForV(i);
                        places.add(place);
                    }
                }
            FilLStoreList();
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage());
            }
        }
    }


//gets Google places api from search string
    // different types of search returned different labels, needed to make 2 with the slight change
    private class SearchGooglePlaces extends AsyncTask<String, String, String> {
        //getting the google places API XML
        @Override
        protected String doInBackground(String... args) {

            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;
            try {
                response = httpclient.execute(new HttpGet(args[0]));
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                } else {
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
                Log.e("ERROR", e.getMessage());
            } catch (IOException e) {
                Log.e("ERROR", e.getMessage());
            }
            return responseString;
        }
        // parses the xml into Places
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                places.clear();
                Document xmlResult = loadXMLFromString(result);
                NodeList nodeList =  xmlResult.getElementsByTagName("result");
                for(int i = 0, length = nodeList.getLength(); i < length; i++) {
                    Node node = nodeList.item(i);
                    if(node.getNodeType() == Node.ELEMENT_NODE) {
                        Element nodeElement = (Element) node;
                        Place place = new Place();
                        Node name = nodeElement.getElementsByTagName("name").item(0);
                        Node vicinity = nodeElement.getElementsByTagName("formatted_address").item(0);
                        Node rating = nodeElement.getElementsByTagName("rating").item(0);
                        Node id = nodeElement.getElementsByTagName("id").item(0);
                        Node geometryElement = nodeElement.getElementsByTagName("geometry").item(0);
                        NodeList locationElement = geometryElement.getChildNodes();
                        Element latLngElem = (Element) locationElement.item(1);
                        Node lat = latLngElem.getElementsByTagName("lat").item(0);
                        Node lng = latLngElem.getElementsByTagName("lng").item(0);
                        float[] geometry =  {Float.valueOf(lat.getTextContent()),
                                Float.valueOf(lng.getTextContent())};
                        int typeCount = nodeElement.getElementsByTagName("type").getLength();

                        place.setVicinity(vicinity.getTextContent());
                        place.setId(id.getTextContent());
                        place.setName(name.getTextContent());
                        if(null == rating) {
                            place.setRating(0.0f);
                        } else {
                            place.setRating(Float.valueOf(rating.getTextContent()));
                        }
                        place.setGeometry(geometry);
                        place.setIdForV(i);

                        places.add(place);
                    }
                }

                FilLStoreList();

            } catch (Exception e) {
                Log.e("ERROR", e.getMessage());
            }


        }
    }
// shows individual restaurants
    public void FillPlace(){
        setContentView(R.layout.item_selected);

        TextView itemName = (TextView) findViewById(R.id.item_name);
        TextView itemVicinity = (TextView) findViewById(R.id.item_vicinity);
        TextView itemVRating = (TextView) findViewById(R.id.item_vrating);
        TextView itemRating = (TextView) findViewById(R.id.item_rating);
        TextView itemNegative = (TextView) findViewById(R.id.item_negative);

        itemRating.setText(Float.toString(CurrentPlace.place.getRating()) + "/5 Google Rating");
        itemName.setText(CurrentPlace.place.getName());
        itemVicinity.setText(CurrentPlace.place.getVicinity());
        itemVRating.setText(Integer.toString(CurrentPlace.place.getVRating()) + "/100 Health Score");
        if(CurrentPlace.place.getVRating() < 0){
            itemNegative.setText("Yes negative numbers are bad");
        }

        Button clickButton = (Button) findViewById(R.id.violations_button);
        clickButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                setContentView(R.layout.violation_layout);
                ViolationAdapter violationAdapter = new ViolationAdapter(MainActivity.this, R.layout.activity_main, CurrentViolations.all());
                violationView = (ListView)findViewById(R.id.violation_listview);
                violationView.setAdapter(violationAdapter);
                Button returnButton = (Button) findViewById(R.id.back_button);
                returnButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        FillPlace();
                    }
                });


            };
        });
        //opens map
        Button mapButton = (Button) findViewById(R.id.map_button);
        mapButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                float[] geometry =  CurrentPlace.place.getGeometry();

                String uri = String.format(Locale.ENGLISH, "geo:"+ Float.toString(geometry[0]) + "," + Float.toString(geometry[1]));
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);

                Toast.makeText(getApplicationContext(), "loading map " + uri,
                        Toast.LENGTH_LONG).show();

            }
        });


        Button returnButton = (Button) findViewById(R.id.list_button);
        returnButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                setContentView(R.layout.httptestlist);
                FilLStoreList();
            }
        });

    }




    public void FilLStoreList() {


    PlaceAdapter placeAdapter = new PlaceAdapter(MainActivity.this, R.layout.activity_main, places);
        Button nbtn = (Button) findViewById(R.id.new_button);
        nbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openMenu();
            }
        });

    listView = (ListView)findViewById(R.id.httptestlist_listview);

    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int pos,
                                long id) {
            CurrentPlace.place = places.get(pos);

            // reformating name for use in CurrentViolations.v
            String pname = CurrentPlace.place.getName();
            //getting rid of special characters
            pname = pname.toUpperCase();
            pname = pname.replaceAll("&", "%26");
            pname = pname.replaceAll("!", "%21");
            if (pname.contains("'")) {
                pname = pname.substring(0, pname.indexOf("'"));
            }
            new HealthAPI().execute(pname);
       }
    });
    listView.setAdapter(placeAdapter);
}



    private class PlaceAdapter extends ArrayAdapter<Place> {
        public Context context;
        public int layoutResourceId;
        public ArrayList<Place> places;

        public PlaceAdapter(Context context, int layoutResourceId, ArrayList<Place> places) {
            super(context, layoutResourceId, places);
            this.layoutResourceId = layoutResourceId;
            this.places = places;
        }

        @Override
        public View getView(int rowIndex, View convertView, ViewGroup parent) {
            View row = convertView;
            if(null == row) {
                LayoutInflater layout = (LayoutInflater)getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE
                );
                row = layout.inflate(R.layout.activity_main, null);
            }
            Place place = places.get(rowIndex);
            if(null != place) {
                TextView name = (TextView) row.findViewById(R.id.htttptestrow_name);
                TextView vicinity = (TextView) row.findViewById(
                        R.id.httptestrow_vicinity);
                if(null != name) {
                    name.setText(place.getName());
                }
                if(null != vicinity) {
                    vicinity.setText(place.getVicinity());
                }
            }
            return row;
        }
    }







    private class ViolationAdapter extends ArrayAdapter<Violation> {
        public Context context;
        public int layoutResourceId;
        public ArrayList<Violation> violations;

        public ViolationAdapter(Context context, int layoutResourceId, ArrayList<Violation> violations) {
            super(context, layoutResourceId, violations);
            this.layoutResourceId = layoutResourceId;
            this.violations = violations;
        }


        @Override
        public View getView(int rowIndex, View convertView, ViewGroup parent) {
            View row = convertView;
            if(null == row) {
                LayoutInflater layout = (LayoutInflater)getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE
                );
                row = layout.inflate(R.layout.violation_list, null);
            }
            Violation violation = violations.get(rowIndex);

            if(null != violation) {
                TextView date = (TextView) row.findViewById(R.id.v_date);
                TextView code = (TextView) row.findViewById(R.id.v_code);
                TextView text = (TextView) row.findViewById(R.id.v_text);
                TextView risk = (TextView) row.findViewById(R.id.v_risk);
                TextView critical = (TextView) row.findViewById(R.id.v_critical);
                TextView rating = (TextView) row.findViewById(R.id.v_rating);

                String d =  violation.getDate();
                d = d.substring(0, d.indexOf("T"));

                date.setText("Inspection date: " + d);
                code.setText("Violation: " + violation.getCodeViolation());
                text.setText(violation.getViolationText());
                risk.setText("Risk level: " + violation.getRiskLevel());
                critical.setText("Is violation critical?: " + violation.getCritical());
                rating.setText("Point penalty to score: " + Integer.toString(violation.getRating()));


            }
            return row;
        }

    }

    public class HealthAPI extends AsyncTask<String, String, String> {
        private StringBuilder queryHealth = new StringBuilder();

        @Override
        protected String doInBackground(String... args) {
    // running name violation query
            queryHealth = new StringBuilder();
            queryHealth.append("http://communities.socrata.com/resource/nzdy-gqv2.json");
            queryHealth.append("?$where=starts_with(name_of_business,'");
            queryHealth.append(args[0] + "')");

            String qHealth = queryHealth.toString();
            qHealth = qHealth.replaceAll(" ", "%20");

            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse hresponse;
            String responseString = null;
            try {
                hresponse = httpclient.execute(new HttpGet(qHealth));
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
            CurrentViolations.start();
            JSONObject jsonObject = null;
            try {
                //pulling information out of Health JSON
                JSONArray jarray = new JSONArray(result);
                CurrentPlace.place.setVRating(100);

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
                    String paddress = CurrentPlace.place.getVicinity();
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
                        r =  (4 - Integer.parseInt(riskLevel));  //risk levels 1-3, 1 being worse
                        if (critical.equals("Yes")) {
                            r = r * 4;
                        }
                        violation.setRating(r);
                        CurrentPlace.place.setVRating(CurrentPlace.place.getVRating() - r);

                        CurrentViolations.add(violation);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            FillPlace();
        }
    }

    public static Document loadXMLFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        InputSource is = new InputSource(new StringReader(xml));

        return builder.parse(is);
    }
    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    }

    public class MyRunnable implements Runnable {
        public MyRunnable() {
        }

        public void run() {
            myLocation.getLocation(getApplicationContext(), locationResult);
        }
    }


}