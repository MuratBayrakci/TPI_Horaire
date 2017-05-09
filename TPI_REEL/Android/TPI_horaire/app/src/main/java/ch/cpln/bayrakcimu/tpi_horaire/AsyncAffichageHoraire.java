package ch.cpln.bayrakcimu.tpi_horaire;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;

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

    public AsyncReponse delegate = null;
    ProgressDialog dialog;


    Activity activite;
    public int iNum;
    public AsyncAffichageHoraire(Activity a, int numrecu)
    {
        this.activite = a;
        dialog = new ProgressDialog(activite);
        iNum = numrecu;
    }

    public void onPreExecute() {
        if(iNum==0){
            this.dialog.setMessage("Réception des données...");
            this.dialog.setCancelable(false);
            this.dialog.show();
        }
    }




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
            int code_reponse = conn.getResponseCode();

            if (code_reponse == HttpURLConnection.HTTP_OK) {
                InputStream input = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                StringBuilder result = new StringBuilder();
                String strLigne;


                while ((strLigne = reader.readLine()) != null) {
                    result.append(strLigne);
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

            dialog.dismiss();
            delegate.RetourOutput(result);

    }

}
