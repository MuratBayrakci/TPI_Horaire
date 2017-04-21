package ch.cpln.bayrakcimu.tpi_horaire;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by BayrakciMu on 27.03.2017.
 */
public class AsyncAffichageHoraire extends AsyncTask<String, String, String> {
    HttpURLConnection conn;
    URL url = null;


    @Override
    protected String doInBackground(String... params){

        try {
            url = new URL (params[0]);

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "exception1";
        }

        try {
            conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.connect();

        } catch (IOException e) {
            e.printStackTrace();
            return "exception2";
        }

        try {
            int response_code = conn.getResponseCode();

            if (response_code == HttpURLConnection.HTTP_OK) {
                InputStream input = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                StringBuilder result = new StringBuilder();
                String line;


                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                return (result.toString());


            } else {
                return ("unsuccessful");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "exception3";
        }
        finally {
            conn.disconnect();
        }

    }


    @Override
    protected void onPostExecute(String result) {


            /*
            String lol="";
            ArrayList<String> alist = new ArrayList<String>();

            try {

                JSONArray jsonArrayGeneral = new JSONArray(result);
                for(int i =0;i<jsonArrayGeneral.length();i++) {

                    JSONObject jsonObjectGeneral = jsonArrayGeneral.getJSONObject(i);
                   // alist.add(i) = jsonObjectGeneral.getString("libelle");
                    alist.add(jsonObjectGeneral.getString("libelle"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


            TextView TvResultat = (TextView)findViewById(R.id.TvResultat);
            TvResultat.setText(alist.get(0));

*/
    }

}
