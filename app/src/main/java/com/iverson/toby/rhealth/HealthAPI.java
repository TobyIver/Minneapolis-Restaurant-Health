package com.iverson.toby.rhealth;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Toby on 4/25/2015.
 */
public class HealthAPI {

    public class run extends AsyncTask<String, String, String> {

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
            ArrayList<Violation> violations = new ArrayList<>();


            try {
                Document xmlResult = MainActivity.loadXMLFromString(result);//Might have  to move load
                NodeList nodeList =  xmlResult.getElementsByTagName("result");
                for(int i = 0, length = nodeList.getLength(); i < length; i++) {
                    Node node = nodeList.item(i);
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


                        violations.add(violation);

                        //todo where VSforP is handled


                    }
                }

            } catch (Exception e) {
                Log.e("ERROR", e.getMessage());
            }


        }
    }
}
