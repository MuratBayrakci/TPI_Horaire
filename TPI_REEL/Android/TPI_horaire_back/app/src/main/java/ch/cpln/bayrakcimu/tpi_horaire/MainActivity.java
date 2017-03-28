package ch.cpln.bayrakcimu.tpi_horaire;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {


    String Output="";
    String Id="";
    String Output2="";
    String ContenuClasse ="";
    String ContenuDate="";

    Boolean DateActuel = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);





/*
        EditText EtClasse = (EditText)findViewById(R.id.EtClasse);
        String ContenuClasse = EtClasse.getText().toString();
        EditText EtDate = (EditText)findViewById(R.id.EtDate);
        String ContenuDate = EtDate.getText().toString(); */

       // CreationLayout();


        CreationEditText();
        BoutonListeners();
        Requete();
        CreationLayout();


/*
        try {
            Output= new AsyncAffichageHoraire().execute("http://devinter.cpln.ch/pdf/hypercool/controler.php?action=ressource&nom=" + ContenuClasse).get();
            Id =  ParseId(Output);
            Output2 =  new AsyncAffichageHoraire().execute("http://devinter.cpln.ch/pdf/hypercool/controler.php?action=horaire&ident=" + Id + "&sub=date&date=" + ContenuDate).get();
            ParseOutput(Output2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } */


        Button BtnRechercher = (Button)findViewById(R.id.BtnRechercher);
        BtnRechercher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
/*
                EditText EtClasse = (EditText)findViewById(R.id.EtClasse);
                String ContenuClasse = EtClasse.getText().toString();
                EditText EtDate = (EditText)findViewById(R.id.EtDate);
                String ContenuDate = EtDate.getText().toString();
*/
                CreationEditText();
                BoutonListeners();
                Requete();
                /*
                try {
                    Output= new AsyncAffichageHoraire().execute("http://devinter.cpln.ch/pdf/hypercool/controler.php?action=ressource&nom=" + ContenuClasse).get();
                    Id =  ParseId(Output);
                   Output2 = new AsyncAffichageHoraire().execute("http://devinter.cpln.ch/pdf/hypercool/controler.php?action=horaire&ident=" + Id + "&sub=date&date=" + ContenuDate).get();
                    ParseOutput(Output2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } */

            }
        });



    }



    public void BoutonListeners(){

        Button BtnPlusJour = (Button)findViewById(R.id.BtnPlusJour);
        BtnPlusJour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TraitementDate(ContenuDate, 1);
                Requete();
            }
        });

        Button BtnPlusSemaine = (Button)findViewById(R.id.BtnPlusSemaine);
        BtnPlusSemaine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TraitementDate(ContenuDate, 7);
                Requete();
            }
        });

        Button BtnMoinsJour = (Button)findViewById(R.id.BtnMoinsJour);
        BtnMoinsJour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TraitementDate(ContenuDate, -1);
                Requete();
            }
        });

        Button BtnMoinsSemaine = (Button)findViewById(R.id.BtnMoinsSemaine);
        BtnMoinsSemaine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TraitementDate(ContenuDate, -7);
                Requete();
            }
        });

    }

    public void TraitementDate(String string, int i){

        try {
            SimpleDateFormat Formater = new SimpleDateFormat("dd-MM-yyyy");
            Date dateObj = Formater.parse(string);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateObj);
            calendar.add(Calendar.DATE, i);
            ContenuDate = Formater.format(calendar.getTime());

            String dayLongName = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());

            EditText EtDate = (EditText)findViewById(R.id.EtDate);
            EtDate.setText(ContenuDate);
            TextView TvJourDeLaSemaine = (TextView)findViewById(R.id.TvJourDeLaSemaine);
            TvJourDeLaSemaine.setText(dayLongName);

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }




    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_vue_semaine:
                VueSemaine();
                return true;
            case R.id.action_effacer:
                EffacerRecherches();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void VueSemaine(){

    }
    public void EffacerRecherches(){

    }

    public void CreationEditText(){

        EditText EtClasse = (EditText)findViewById(R.id.EtClasse);
         ContenuClasse = EtClasse.getText().toString();


        SimpleDateFormat Formater = new SimpleDateFormat("dd-MM-yyyy");
        Calendar c = Calendar.getInstance();
        String JourActuel = Formater.format(c.getTime());


        EditText EtDate = (EditText)findViewById(R.id.EtDate);
         ContenuDate = EtDate.getText().toString();

        if(DateActuel == true) {
            ContenuDate = JourActuel;
        }
        EtDate.setText(ContenuDate);

DateActuel = false;







    }
    public void Requete(){

        try {
            Output= new AsyncAffichageHoraire().execute("http://devinter.cpln.ch/pdf/hypercool/controler.php?action=ressource&nom=" + ContenuClasse).get();
            Id =  ParseId(Output);
            Output2 =  new AsyncAffichageHoraire().execute("http://devinter.cpln.ch/pdf/hypercool/controler.php?action=horaire&ident=" + Id + "&sub=date&date=" + ContenuDate).get();
            ParseOutput(Output2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    public void ParseOutput(String string){




        String[] ArrayHeureDebut = new String[100];
        String[] ArrayHeureFin = new String[100];
        String[] ArrayHeureDebutComplet = new String[100];
        String[] ArrayHeureFinComplet = new String[100];
        String[] ArrayLibelle = new String[100];
        String[] ArrayProfesseur = new String[100];
        String[] ArraySalle = new String[100];
        Integer[] ArrayCalculHeure = new Integer[100];
        String[] ArrayCalculHeureString = new String[100];


        ArrayList<Integer> aListDebut = new ArrayList<Integer>();
        ArrayList<Integer> aListFin = new ArrayList<Integer>();
        ArrayList<Integer> aListCalcul = new ArrayList<Integer>();

        ArrayList<String> AlistArrayHeureDebut = new ArrayList<String>();
        ArrayList<String> AlistArrayHeureFin = new ArrayList<String>();
        ArrayList<String> AlistArrayHeureDebutComplet = new ArrayList<String>();
        ArrayList<String> AlistArrayHeureFinComplet = new ArrayList<String>();
        ArrayList<String> AlistArrayLibelle = new ArrayList<String>();
        ArrayList<String> AlistArrayProfesseur = new ArrayList<String>();
        ArrayList<String> AlistArraySalle = new ArrayList<String>();
        ArrayList<Integer> AlistArrayCalculHeure = new ArrayList<Integer>();




        int i = 0;
        int iHeureDebut = 0;
        int iHeureFin = 0;


        try {
            JSONArray jsonArrayGeneral = new JSONArray(string);
            for (i = 0; i < jsonArrayGeneral.length(); i++) {

                JSONObject jsonObjectGeneral = jsonArrayGeneral.getJSONObject(i);
                ArrayHeureDebut[i] = jsonObjectGeneral.getString("heureDebut");
                ArrayHeureFin[i] = jsonObjectGeneral.getString("heureFin");
                ArrayLibelle[i] = jsonObjectGeneral.getString("libelle");




                try {
                    JSONArray jsonArrayProf = jsonObjectGeneral.getJSONArray("professeur");
                    ArrayProfesseur[i] = jsonArrayProf.getString(0);


                }catch (JSONException e){
                    ArrayProfesseur[i] = "/";

                }


                try {
                    JSONArray jsonArraySalle = jsonObjectGeneral.getJSONArray("salle");
                    // ArraySalle[i] = jsonArraySalle.getString(jsonArraySalle.length()-1);
                    ArraySalle[i] = jsonArraySalle.getString(0);


                }catch(JSONException e) {
                    ArraySalle[i] = "/-";


                }

                try {
                    ArrayProfesseur[i] = ArrayProfesseur[i].substring(0, ArrayProfesseur[i].indexOf(" "));


                }catch(Exception e){}

                try {
                    ArraySalle[i] = ArraySalle[i].substring(0, ArraySalle[i].indexOf("-"));


                }
                catch(Exception e){

                }




                ArrayHeureDebutComplet[i] = ArrayHeureDebut[i];
                ArrayHeureFinComplet[i] = ArrayHeureFin[i];
                ArrayHeureDebut[i] = ArrayHeureDebut[i].substring(0, ArrayHeureDebut[i].length() - 3);
                ArrayHeureFin[i] = ArrayHeureFin[i].substring(0, ArrayHeureFin[i].length() - 3);




                AlistArrayHeureDebutComplet.add(i, ArrayHeureDebut[i]);
                AlistArrayHeureFinComplet.add(i,ArrayHeureDebut[i]);
                AlistArrayProfesseur.add(i,ArrayProfesseur[i]);
                AlistArraySalle.add(i,ArraySalle[i]);
                AlistArrayLibelle.add(i,ArrayLibelle[i]);

                try {
                    iHeureDebut = Integer.parseInt(ArrayHeureDebut[i]);
                    iHeureFin = Integer.parseInt(ArrayHeureFin[i]);


                    ArrayCalculHeure[i] = iHeureDebut + iHeureFin;
                    ArrayCalculHeureString[i] = String.valueOf(ArrayCalculHeure[i]);


                    AlistArrayCalculHeure.add(i, ArrayCalculHeure[i]);
                    aListCalcul.add(i, iHeureDebut + iHeureFin);


                } catch (NumberFormatException nfe) {
                }




            }

            Collections.sort(aListCalcul);




            } catch (JSONException e) {
                e.printStackTrace();
            }


        JSONArray jsonArrayGeneral = null;
        try {
            jsonArrayGeneral = new JSONArray(string);




            Collections.sort(aListCalcul);



            for(int i20=0;i20<jsonArrayGeneral.length();i20++) {

                for (int i10 = 0; i10 < jsonArrayGeneral.length(); i10++) {
                    if (AlistArrayCalculHeure.get(i10).equals(aListCalcul.get(i20))) {
                        AlistArrayLibelle.add(i20, ArrayLibelle[i10]);
                        AlistArrayHeureDebutComplet.add(i20, ArrayHeureDebutComplet[i10]);
                        AlistArrayHeureFinComplet.add(i20, ArrayHeureFinComplet[i10]);
                        AlistArrayProfesseur.add(i20, ArrayProfesseur[i10]);
                        AlistArraySalle.add(i20, ArraySalle[i10]);
                    }
                }
            }



            //   }
        } catch (JSONException e) {
            e.printStackTrace();
        }







        TextView TvResultat = (TextView) findViewById(R.id.TvResultat);
        //    TvResultat.setText(ArrayHeureDebut[0] + " " + ArrayHeureDebut[1] + " " + ArrayHeureDebut[2] + " " + ArrayHeureDebut[3] );
            // TvResultat.setText(ArrayHeureFin[0] + " " + ArrayHeureFin[1] + " " + ArrayHeureFin[2] + " " + ArrayHeureFin[3]);

        TvResultat.setText(AlistArrayLibelle.get(1));


            TextView TvTest = (TextView) findViewById(R.id.TvTest);
            //  TvTest.setText(ArrayCalculHeureString[0] + " " + ArrayCalculHeureString[1] + " " + ArrayCalculHeureString[2] + " " + ArrayCalculHeureString[3] + " " + ArrayProfesseur[0] + " "+ ArraySalle[0]);


            TextView TvSalle = (TextView) findViewById(R.id.TvSalle);
            TextView TvHeureDebut = (TextView) findViewById(R.id.TvHeureDebut);
            TextView TvHeureFin = (TextView) findViewById(R.id.TvHeureFin);
            TextView TvLibelle = (TextView) findViewById(R.id.TvLibelle);
            TextView TvProf = (TextView) findViewById(R.id.TvProf);

            TvSalle.setText(ArraySalle[0]);
            TvHeureDebut.setText(ArrayHeureDebutComplet[0]);
            TvHeureFin.setText(ArrayHeureFinComplet[0]);
            TvLibelle.setText(ArrayLibelle[0]);
            TvProf.setText(ArrayProfesseur[0]);



        }



    public void CreationLayout(){


        /*
        LinearLayout parent = new LinearLayout(this);
        parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        parent.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout layout = new LinearLayout(this);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout layout2 = new LinearLayout(this);
        layout2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        layout2.setOrientation(LinearLayout.VERTICAL);

        LinearLayout layout3 = new LinearLayout(this);
        layout3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        layout3.setOrientation(LinearLayout.VERTICAL);

        parent.addView(layout);
        parent.addView(layout2);
        parent.addView(layout3);


        TextView tv1 = new TextView(this);
        TextView tv2 = new TextView(this);
        TextView tv3 = new TextView(this);
        TextView tv4 = new TextView(this);
        TextView tv5 = new TextView(this);

        layout.addView(tv1);
        layout.addView(tv2);
        layout2.addView(tv3);
        layout2.addView(tv4);
        layout3.addView(tv5);

        tv1.setText("SIFHASIOFHDIOFSDFHIOSD");
*/
/*
        LinearLayout parent = new LinearLayout(this);
         parent = (LinearLayout)findViewById(R.id.LlGeneral);
        parent.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        parent.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout layout = (LinearLayout)findViewById(R.id.Ll1);
        layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout layout2 = (LinearLayout)findViewById(R.id.Ll2);
        layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout layout3 = (LinearLayout)findViewById(R.id.Ll3);
        layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.setOrientation(LinearLayout.VERTICAL);


        LinearLayout layoutsupreme = (LinearLayout)findViewById(R.id.LlSupreme);
        layoutsupreme.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layoutsupreme.setOrientation(LinearLayout.VERTICAL);


        TextView TvHeureDebut = (TextView)findViewById(R.id.TvHeureDebut);
        TextView TvHeureFin = (TextView)findViewById(R.id.TvHeureFin);
        TextView TvProf = (TextView)findViewById(R.id.TvProf);
        TextView TvLibelle = (TextView)findViewById(R.id.TvLibelle);
        TextView TvSalle = (TextView)findViewById(R.id.TvSalle);



        layoutsupreme.addView(parent);
        parent.addView(layout);
        parent.addView(layout2);
        parent.addView(layout3);



        layout.addView(TvHeureDebut);
        layout.addView(TvHeureFin);
        layout2.addView(TvLibelle);
        layout2.addView(TvProf);
        layout3.addView(TvSalle);

        TvHeureDebut.setText("CAR MARCH");


*/

/*
        View LlSupreme = findViewById(R.id.LlSupreme);
        //View L1General = findViewById(R.id.LlGeneral);
        LinearLayout a = new LinearLayout(this);

        TextView valueTv = new TextView(this);
        valueTv.setText("lol");
        valueTv.setId(0);
        valueTv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));



        ((LinearLayout) LlSupreme).addView(valueTv);

      //  ((LinearLayout)L1General).removeView(LlSupreme);
      //  ((LinearLayout)L1General).addView(L1General);
      //  ((LinearLayout)LlSupreme).addView(a); */


        LinearLayout LlSupreme = (LinearLayout) findViewById(R.id.LlSupreme);
        LinearLayout LlGeneral = (LinearLayout) findViewById(R.id.LlGeneral);


      //  ((LinearLayout) LlSupreme).addView(LlGeneral);

    }


    public String ParseId(String string){
        string = string.split(":")[0];
        string = string.substring(0, string.length() -1);
        string = string.substring(2);
        return string;
    }


    private class AsyncAffichageHoraire extends AsyncTask<String, String, String> {

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
}
