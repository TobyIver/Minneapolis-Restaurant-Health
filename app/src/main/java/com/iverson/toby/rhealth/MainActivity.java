package com.iverson.toby.rhealth;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivity extends Activity {

    //database Violation names
    private static final String DB_NAME = "Health.jpg";
    private static final String TABLE_NAME = "inspections";
    private static final String vName = "NameofBusiness";
    private static final String vAdd = "LicenseAddress";
    private static final String vDate = "DateofInspection";
    private static final String vRisk = "RiskLevel";
    private static final String vCode = "CodeSection";
    private static final String vCodeText = "StandardOrderText";
    private static final String vCritical = "Critical";
    private static final String v_id = "_id";
    private SQLiteDatabase database;

    private ViolationDataSource datasource;



    //GPS and Google Places variables
    private String latitude;
    private String longitude;
    private String provider;
    private final String APIKEY = "AIzaSyAMfjDmxGTWke6GgwZS0RbG3zg1Jjl1mtg";
    private final int radius = 2000;
    private String type = "food";
    private StringBuilder query = new StringBuilder();
    private ArrayList<Place> places = new ArrayList<Place>();
    private ListView listView;
    MyLocation myLocation = new MyLocation();
    MyLocation.LocationResult locationResult;
    ProgressDialog progressDialog = null;
    Context context ;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.httptestlist);


        datasource = new ViolationDataSource(this);
        datasource.open();

/*
        String fr = null;

        Cursor friendCursor = database.query(TABLE_NAME, new String[] {v_id, vName}//, vDate, vRisk,vCritical}
                , vName + " = CAPITAL GRILLE"  , null, null, null, null);
        friendCursor.moveToFirst();
        if(!friendCursor.isAfterLast()) {
            do {
                String name = friendCursor.getString(1);
                fr = fr + name + " ";
            } while (friendCursor.moveToNext());
        }
        friendCursor.close();

        Toast.makeText(getApplicationContext(), fr,
                Toast.LENGTH_LONG).show();
*/
//location getter Todo turn on after testing done
/*
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


        MyRunnable myRun = new MyRunnable();
        myRun.run();

        progressDialog = ProgressDialog.show(MainActivity.this, "Finding your location",
                "Please wait...", true);
*/
        // testing GPS cords
        latitude = String.valueOf(44.9757011);
        longitude = String.valueOf(-93.2728672);
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




            query.append("https://maps.googleapis.com/maps/api/place/nearbysearch/xml?");
            query.append("location=" +  latitude + "," + longitude + "&");
            query.append("radius=" + radius + "&");
            query.append("types=" + type + "&");
            query.append("sensor=true&"); //Must be true if queried from a device with GPS
            query.append("key=" + APIKEY);
            new QueryGooglePlaces().execute(query.toString());
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
                for(int i = 0, length = nodeList.getLength(); i < length; i++) {
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
                        places.add(place);

                        //todo where VSforP is handled


                        String pname = place.getName();
                        pname = pname.toUpperCase();




                        String paddress = place.getVicinity();
                        paddress = paddress.toUpperCase();
                        paddress= paddress.substring(0, paddress.indexOf(" "));
/*

                        String fr = null;

                        Cursor friendCursor = database.query(TABLE_NAME, new String[] {v_id}//, vDate, vRisk,vCritical}
                                , vName + " = " + pname + " AND " + vAdd + " = " + paddress , null, null, null, null);
                        friendCursor.moveToFirst();
                        if(!friendCursor.isAfterLast()) {
                            do {
                                String name = friendCursor.getString(1);
                                fr = fr + name + " ";
                            } while (friendCursor.moveToNext());
                        }
                        friendCursor.close();

                        Toast.makeText(getApplicationContext(), fr,
                                Toast.LENGTH_LONG).show();

*/

                    }
                }
                /**** Puts places into listview doesn't work****/
                PlaceAdapter placeAdapter = new PlaceAdapter(MainActivity.this, R.layout.activity_main, places);
                listView = (ListView)findViewById(R.id.httptestlist_listview);
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