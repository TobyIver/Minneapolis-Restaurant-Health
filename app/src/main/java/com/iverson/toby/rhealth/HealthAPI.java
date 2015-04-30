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
            HttpResponse hresponse;
            String responseString = null;
            try {
                hresponse = httpclient.execute(new HttpGet(args[0]));
                StatusLine statusLine = hresponse.getStatusLine();
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
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
            ArrayList<Violation> violations = new ArrayList<>();


            try {
                Document healthResult = MainActivity.loadXMLFromString(result);//Might have  to move load
                NodeList nodeList1 =  healthResult.getElementsByTagName("row");
                Node node1 = nodeList1.item(0);
                Element nodeElement1 = (Element) node1;
                Node node2 = nodeElement1.getElementsByTagName("row").item(0);
                NodeList nodeList2 = node2.getChildNodes();
                //row nested in row......


                for(int ii = 0, lengthi = nodeList2.getLength(); ii < lengthi; ii++) {
                    Node hnode = nodeList2.item(ii);
                    if (hnode.getNodeType() == Node.ELEMENT_NODE) {
                        Element vnodeElement = (Element) hnode;
                        Violation violation = new Violation();
                        Node vname = vnodeElement.getElementsByTagName("name_of_business").item(0);
                        Node address = vnodeElement.getElementsByTagName("license_address").item(0);
                        Node date = vnodeElement.getElementsByTagName("date_of_inspection").item(0);
                        Node riskLevel = vnodeElement.getElementsByTagName("risk_level").item(0);
                        Node violationText = vnodeElement.getElementsByTagName("standard_order_text").item(0);
                        Node codeViolation = vnodeElement.getElementsByTagName("code_section").item(0);
                        Node critical = vnodeElement.getElementsByTagName("critical").item(0);

                        violation.setName(vname.getTextContent());
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
}
