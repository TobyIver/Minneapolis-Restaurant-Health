package com.iverson.toby.rhealth;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivity extends Activity {

    //database Violation names from helper
    private static final String DB_NAME = MySQLiteHelper.DATABASE_NAME;
    private static final String TABLE_NAME = MySQLiteHelper.TABLE_Name;
    private static final String vName = MySQLiteHelper.COLUMN_Name;
    private static final String vAdd = MySQLiteHelper.COLUMN_Address;
    private static final String vDate = MySQLiteHelper.COLUMN_Date;
    private static final String vRisk = MySQLiteHelper.COLUMN_RiskLevel;
    private static final String vCode = MySQLiteHelper.COLUMN_CodeViolation;
    private static final String vCodeText = MySQLiteHelper.COLUMN_ViolationText;
    private static final String vCritical = MySQLiteHelper.COLUMN_Critical;
    private static final String v_id = MySQLiteHelper.COLUMN_ID;
    private static final String vRating = MySQLiteHelper.COLUMN_Rating;
    private SQLiteDatabase database;

    private ViolationDataSource datasource;

    private StringBuilder queryViolation = new StringBuilder();

    //GPS and Google Places variables
    private String latitude;
    private String longitude;
    private String provider;
    private final String APIKEY = "AIzaSyAMfjDmxGTWke6GgwZS0RbG3zg1Jjl1mtg";
    private final int radius = 2000;
    private String type = "food";
    private StringBuilder queryGoogle = new StringBuilder();
    private StringBuilder queryHealth = new StringBuilder();
    public ArrayList<Place> places = new ArrayList<Place>();
    public Place placeItem = new Place();
    private ArrayList<Integer> placeRatings = new ArrayList<Integer>();
    private ArrayList<Violation> violations = new ArrayList<Violation>();
    private ListView listView;
    MyLocation myLocation = new MyLocation();
    MyLocation.LocationResult locationResult;
    ProgressDialog progressDialog = null;
    Context context ;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        //Todo testing GPS cords
        latitude = String.valueOf(44.9757011);
        longitude = String.valueOf(-93.2728672);


        MyRunnable myRun = new MyRunnable();
        myRun.run();

        progressDialog = ProgressDialog.show(MainActivity.this, "Finding your location",
                "Please wait...", true);

        queryGoogle.append("https://maps.googleapis.com/maps/api/place/nearbysearch/xml?");
        queryGoogle.append("location=" +  latitude + "," + longitude + "&");
        queryGoogle.append("radius=" + radius + "&");
        queryGoogle.append("types=" + type + "&");
        queryGoogle.append("sensor=true&"); //Must be true if queried from a device with GPS
        queryGoogle.append("key=" + APIKEY);
        new QueryGooglePlaces().execute(queryGoogle.toString());



    }

    public static Document loadXMLFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        InputSource is = new InputSource(new StringReader(xml));

        return builder.parse(is);
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

/*
            queryGoogle.append("https://maps.googleapis.com/maps/api/place/nearbysearch/xml?");
            queryGoogle.append("location=" +  latitude + "," + longitude + "&");
            queryGoogle.append("radius=" + radius + "&");
            queryGoogle.append("types=" + type + "&");
            queryGoogle.append("sensor=true&"); //Must be true if queried from a device with GPS
            queryGoogle.append("key=" + APIKEY);
            new QueryGooglePlaces().execute(queryGoogle.toString());
            */
        }
    }

    /**
     * Based on: http://stackoverflow.com/questions/3505930
     */
    private class QueryGooglePlaces extends AsyncTask<String, String, String> {

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

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                Document xmlResult = loadXMLFromString(result);
                NodeList nodeList =  xmlResult.getElementsByTagName("result");
                for(int i = 0, length = nodeList.getLength(); i < length; i++) { //todo change back
                    Node node = nodeList.item(i);
                    if(node.getNodeType() == Node.ELEMENT_NODE) {
                        Element nodeElement = (Element) node;
                        Place place = new Place();
                        Node name = nodeElement.getElementsByTagName("name").item(0);
                        Node vicinity = nodeElement.getElementsByTagName("vicinity").item(0);
                        Node rating = nodeElement.getElementsByTagName("rating").item(0);
                        Node reference = nodeElement.getElementsByTagName("reference").item(0);
                        Node id = nodeElement.getElementsByTagName("id").item(0);
                        Node geometryElement = nodeElement.getElementsByTagName("geometry").item(0);
                        NodeList locationElement = geometryElement.getChildNodes();
                        Element latLngElem = (Element) locationElement.item(1);
                        Node lat = latLngElem.getElementsByTagName("lat").item(0);
                        Node lng = latLngElem.getElementsByTagName("lng").item(0);
                        float[] geometry =  {Float.valueOf(lat.getTextContent()),
                                Float.valueOf(lng.getTextContent())};
                        int typeCount = nodeElement.getElementsByTagName("type").getLength();
                        String[] types = new String[typeCount];
                        for(int j = 0; j < typeCount; j++) {
                            types[j] = nodeElement.getElementsByTagName("type").item(j).getTextContent();
                        }
                        place.setVicinity(vicinity.getTextContent());
                        place.setId(id.getTextContent());
                        place.setName(name.getTextContent());
                        if(null == rating) {
                            place.setRating(0.0f);
                        } else {
                            place.setRating(Float.valueOf(rating.getTextContent()));
                        }
                        place.setReference(reference.getTextContent());
                        place.setGeometry(geometry);
                        place.setTypes(types);
                        //places.add(place);



                        // reformating name for use in violations
                        String pname = place.getName();
                        pname = pname.toUpperCase();

                        // running name violation query
                        queryHealth = new StringBuilder();
                        queryHealth.append("http://communities.socrata.com/resource/nzdy-gqv2.xml");
                        queryHealth.append("?$where=starts_with(name_of_business,'");
                        queryHealth.append(pname + "')");

                        String qHealth = queryHealth.toString();
                        qHealth = qHealth.replaceAll(" ", "%20");








                        //verify violation results to prevent errors
                        String paddress = place.getVicinity();
                        paddress = paddress.toUpperCase();
                        paddress= paddress.substring(0, paddress.indexOf(" "));


                        for (int ii = 0; ii > violations.size(); ii++) {
                            Violation v = violations.get(ii);
                            String vaddress = v.getAddress();
                            vaddress = vaddress.substring(0, vaddress.indexOf(" "));
                            if ( v.getAddress() != vaddress){
                                violations.remove(ii);
                                ii = ii-1;
                            }

                        }



                        // Rates the violations for location
                        int xrating = 100;

                        for (int ii = 0; ii > violations.size(); ii++) {
                                Violation v = violations.get(ii);
                                int r = 0;
                                r = 2 * (4 - Integer.parseInt(v.getRiskLevel()));  //risk levels 1-3, 1 being worse
                                if (v.getCritical() == "Yes") {
                                    r = r * 5;
                                }
                                xrating = xrating - r;
                        }
                        place.setVRating(xrating);
                        //saves place
                        places.add(place);





                    }
                }



               PlaceAdapter placeAdapter = new PlaceAdapter(MainActivity.this, R.layout.activity_main, places);
                listView = (ListView)findViewById(R.id.httptestlist_listview);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int pos,
                                            long id) {
                        //int position = places.size() - pos -1;
                         placeItem = places.get(pos-1);

                        //TextView itemName = (TextView) findViewById(R.id.item_name);
                        //TextView itemVicinity = (TextView) findViewById(R.id.item_vicinity);
                        //TextView itemVRating = (TextView) findViewById(R.id.item_vrating);


                        //itemName.setText(placeItem.getName());
                        //itemVicinity.setText(placeItem.getVicinity());

                        setContentView(R.layout.item_selected);


                    }
                });


               listView.setAdapter(placeAdapter);

            } catch (Exception e) {
                Log.e("ERROR", e.getMessage());
            }


        }
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

    // Pulls health inspection data from API
    public class HealthCompare extends AsyncTask<String, String, String>  {

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
             violations = new ArrayList<>();


            try {
                Document xmlResult = MainActivity.loadXMLFromString(result);//Might have  to move load
                NodeList nodeList1 =  xmlResult.getElementsByTagName("row");
                Node node1 = nodeList1.item(0);
                Element nodeElement1 = (Element) node1;
                Node node2 = nodeElement1.getElementsByTagName("row").item(0);
                NodeList nodeList2 = node2.getChildNodes();
                //row nested in row......


                for(int i = 0, length = nodeList2.getLength(); i < length; i++) {
                    Node node = nodeList2.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element nodeElement = (Element) node;
                        Violation violation = new Violation();
                        Node name = nodeElement.getElementsByTagName("name_of_business").item(0);
                        Node address = nodeElement.getElementsByTagName("license_address").item(0);
                        Node date = nodeElement.getElementsByTagName("date_of_inspection").item(0);
                        Node riskLevel = nodeElement.getElementsByTagName("risk_level").item(0);
                        Node violationText = nodeElement.getElementsByTagName("standard_order_text").item(0);
                        Node codeViolation = nodeElement.getElementsByTagName("code_section").item(0);
                        Node critical = nodeElement.getElementsByTagName("critical").item(0);

                        violation.setName(name.getTextContent());
                        violation.setAddress(address.getTextContent());
                        violation.setDate(date.getTextContent());
                        violation.setRiskLevel(riskLevel.getTextContent());
                        violation.setViolationText(violationText.getTextContent());
                        violation.setCodeViolation(codeViolation.getTextContent());
                        violation.setCritial(critical.getTextContent());

                    }
                }

            } catch (Exception e) {
                Log.e("ERROR here", e.getMessage());
            }


        }
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