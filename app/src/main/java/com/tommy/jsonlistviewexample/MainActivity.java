package com.tommy.jsonlistviewexample;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends Activity {
    ListView list;
    ImageButton runBtn;
    ArrayList<String> myList = new ArrayList<>();

    //URL to get JSON Array
    private static String url = "http://mysafeinfo.com/api/data?list=countries&format=json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        runBtn = (ImageButton) findViewById(R.id.runButton);
        runBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new JSONParse().execute();
            }
        });
    }

    private static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    //AsyncTask to retrieve countries and add them to listview
    private class JSONParse extends AsyncTask<String, String, JSONObject> {
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Getting Countries ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            HttpGet httpget = new HttpGet(url);
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;

            try {
                response = httpclient.execute(httpget);
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    InputStream instream = entity.getContent();
                    String result = convertStreamToString(instream);
                    JSONArray arr = new JSONArray(result);
                    for (int i = 0; i < arr.length(); i++) {

                        JSONObject jObj = arr.getJSONObject(i);
                        String data = jObj.getString("nm");
                        myList.add(data);
                        //Log.i("OutPut", data);
                    }
                    instream.close();
                }

            } catch (Exception e) {
                Log.e("Error", e.toString());
            }
            return new JSONObject();
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            pDialog.dismiss();

            // Display countries in listview
            list = (ListView) findViewById(R.id.list);
            list.setAdapter(null);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.list_v,
                    R.id.name, myList);
            list.setAdapter(arrayAdapter);

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                    String selected = myList.get(position);
                    Toast.makeText(getApplicationContext(), "You selected : " + selected, Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
