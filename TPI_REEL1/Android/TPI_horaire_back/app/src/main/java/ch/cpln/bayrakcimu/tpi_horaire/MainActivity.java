package ch.cpln.bayrakcimu.tpi_horaire;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
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
import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.logging.ErrorManager;

public class MainActivity extends AppCompatActivity {


    String Output="";
    String Id="";
    String Output2="";
    String ContenuClasse ="";
    String ContenuDate="";


    int iCouleurTableau = 0;
    Boolean LargeurChampsHeure = true;
    Boolean DateDuJour = true;



    ArrayList<String> AlistHistorique = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        Button btnDatePicker = (Button)findViewById(R.id.btndatepicker);
        btnDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar DateActuelle=Calendar.getInstance();
                int AnneeActuelle = DateActuelle.get(Calendar.YEAR);
                int MoisActuel=DateActuelle.get(Calendar.MONTH);
                int JourActuel=DateActuelle.get(Calendar.DAY_OF_MONTH);

                final DatePickerDialog mDatePicker=new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int AnneeChoisie, int MoisChoisi, int JourChoisi) {

                        int  jour = JourChoisi;
                        int mois = MoisChoisi;
                        int annee = AnneeChoisie;

                       String dateComplet = String.valueOf(jour) + "-" + String.valueOf(mois + 1) + "-" + String.valueOf(annee);
                        EditText et2 = (EditText)findViewById(R.id.EtDate);
                        et2.setText(dateComplet);

                    }
                },AnneeActuelle, MoisActuel, JourActuel);
                mDatePicker.setTitle("Choisir la date");
                mDatePicker.show();  }

        });



            ContenuEditText();
            BoutonListeners();
            Requete();
            EcritureFichier();
            LectureFichier();
            SetAutoCompleteTextView();


        /*
        final Spinner spinner = (Spinner)findViewById(R.id.Spinner);
        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item,
                AlistHistorique);
        spinner.setAdapter(spinnerArrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String lol = spinner.getSelectedItem().toString();
                EditText EtTEst = (EditText)findViewById(R.id.ActvClasse);
                EtTEst.setText(lol);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

*/





        Button BtnRechercher = (Button)findViewById(R.id.BtnRechercher);
        BtnRechercher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                    ContenuEditText();
                    BoutonListeners();
                    Requete();
                    EcritureFichier();
                    LectureFichier();
                    SetAutoCompleteTextView();


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

    // Fonction pour mettre en place et mettre à jour l'autocompleteTextview

    public void SetAutoCompleteTextView(){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, AlistHistorique);
        AutoCompleteTextView textView = (AutoCompleteTextView)
                findViewById(R.id.ActvClasse);
        textView.setThreshold(0);
        textView.setAdapter(adapter);
    }


    // Ecriture dans le fichier.

    public void EcritureFichier(){
        AutoCompleteTextView Actv = (AutoCompleteTextView)findViewById(R.id.ActvClasse);
        String string = Actv.getText().toString();

        File chemin = getBaseContext().getFilesDir();
        File fichier = new File(chemin, "storage.txt");
        Writer writer;
        try {
            writer = new BufferedWriter(new FileWriter(fichier, true));
            writer.append(string + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Lecture du fichier storage et mise des lignes du fichier dans un arraylist.

    public void LectureFichier(){

        File chemin = getBaseContext().getFilesDir();
        File fichier = new File(chemin, "storage.txt");
        String ligne ="";

        try{
            BufferedReader input = new BufferedReader(new FileReader(fichier));
            while ((ligne = input.readLine()) != null) {
                int i= 0;
                AlistHistorique.add(i, ligne);
                i++;
            }
            input.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        SuppressionDoublons();

    }

    //Fonction pour supprimmer les doublons de lignes dans le fichier.
    public void SuppressionDoublons(){

        HashSet<String> hashSet = new HashSet<String>();
        hashSet.addAll(AlistHistorique);
        AlistHistorique.clear();
        AlistHistorique.addAll(hashSet);

    }


    // Ajout de jour / semaine à la date.

    public void TraitementDate(String string, int i){

        try {
            SimpleDateFormat Formater = new SimpleDateFormat("dd-MM-yyyy");
            Date dateObj = Formater.parse(string);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateObj);
            calendar.add(Calendar.DATE, i);
            ContenuDate = Formater.format(calendar.getTime());



            EditText EtDate = (EditText)findViewById(R.id.EtDate);
            EtDate.setText(ContenuDate);



            // Affichage du jour de la semaine

            String NomduJour = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
            switch(NomduJour){
                case "Monday":
                    NomduJour = "Lundi";
                    break;
                case "Tuesday":
                    NomduJour = "Mardi";
                    break;
                case "Wednesday":
                    NomduJour = "Mercredi";
                    break;
                case "Thursday":
                    NomduJour = "Jeudi";
                    break;
                case "Friday":
                    NomduJour = "Vendredi";
                    break;
                case "Saturday":
                    NomduJour = "Samedi";
                    break;
                case "Sunday":
                    NomduJour = "Dimanche";
                    break;
            }

            TextView TvJourDeLaSemaine = (TextView)findViewById(R.id.TvJourDeLaSemaine);
          //  TvJourDeLaSemaine.setText(NomduJour);

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
            case R.id.action_changer_vue:
                VueSemaine();
                return true;
            case R.id.action_effacer:
                EffacerRecherches();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Ouverture de l'activité semaine dès que l'on clique "VueSemaine" dans le menu.
    public void VueSemaine(){

        Intent intent = new Intent(this, Activite_VueSemaine.class);
        startActivity(intent);

    }

    // Création d'un nouveau fichier et vidage du tableau pour l'historique.

    public void EffacerRecherches(){

        File path2 = getBaseContext().getFilesDir();
        File file2 = new File(path2, "storage.txt");
        try {
            PrintWriter pw = new PrintWriter(file2);
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        AlistHistorique.clear();
        LectureFichier();
        SetAutoCompleteTextView();

    }

    public void ContenuEditText(){

       // EditText EtClasse = (EditText)findViewById(R.id.ActvClasse);
        AutoCompleteTextView Actv =(AutoCompleteTextView)findViewById(R.id.ActvClasse);
         ContenuClasse = Actv.getText().toString();


        SimpleDateFormat Formater = new SimpleDateFormat("dd-MM-yyyy");
        Calendar calendar = Calendar.getInstance();
        String JourActuel = Formater.format(calendar.getTime());


        EditText EtDate = (EditText)findViewById(R.id.EtDate);
         ContenuDate = EtDate.getText().toString();

        if(DateDuJour == true) {
            ContenuDate = JourActuel;
        }
        EtDate.setText(ContenuDate);


DateDuJour = false;








// 2 requête asynctask, d'abord l'id puis l'horaire avec cet id.

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


// Création des arrays et arrayslist pour stocker les données reçues.

        String[] ArrayHeureDebut = new String[100];
        String[] ArrayHeureFin = new String[100];
        String[] ArrayHeureDebutComplet = new String[100];
        String[] ArrayHeureFinComplet = new String[100];
        String[] ArrayLibelle = new String[100];
        String[] ArrayProfesseur = new String[100];
        String[] ArraySalle = new String[100];
        Integer[] ArrayCalculHeure = new Integer[100];
        String[] ArrayCalculHeureString = new String[100];


    //    ArrayList<Integer> aListDebut = new ArrayList<Integer>();
   //     ArrayList<Integer> aListFin = new ArrayList<Integer>();
        ArrayList<Integer> aListCalcul = new ArrayList<Integer>();

     //   ArrayList<String> AlistArrayHeureDebut = new ArrayList<String>();
    //    ArrayList<String> AlistArrayHeureFin = new ArrayList<String>();
        ArrayList<String> AlistHeureDebutComplet = new ArrayList<String>();
        ArrayList<String> AlistHeureFinComplet = new ArrayList<String>();
        ArrayList<String> AlistLibelle = new ArrayList<String>();
        ArrayList<String> AlistProfesseur = new ArrayList<String>();
        ArrayList<String> AlistArraySalle = new ArrayList<String>();
        ArrayList<Integer> AlistCalculHeure = new ArrayList<Integer>();




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


            // Ajout des données présents dans les array aux arraylists.

                AlistHeureDebutComplet.add(i, ArrayHeureDebut[i]);
                AlistHeureFinComplet.add(i,ArrayHeureDebut[i]);
                AlistProfesseur.add(i,ArrayProfesseur[i]);
                AlistArraySalle.add(i,ArraySalle[i]);
                AlistLibelle.add(i,ArrayLibelle[i]);

                try {
                    iHeureDebut = Integer.parseInt(ArrayHeureDebut[i]);
                    iHeureFin = Integer.parseInt(ArrayHeureFin[i]);


                    ArrayCalculHeure[i] = iHeureDebut + iHeureFin;
                    ArrayCalculHeureString[i] = String.valueOf(ArrayCalculHeure[i]);


                    AlistCalculHeure.add(i, ArrayCalculHeure[i]);
                    aListCalcul.add(i, iHeureDebut + iHeureFin);


                } catch (NumberFormatException nfe) {
                }




            }

          //  Collections.sort(aListCalcul);




            } catch (JSONException e) {
                e.printStackTrace();
            }



        // Tri et basculement en fonction du bon ordre des données.

        JSONArray jsonArrayGeneral = null;
        try {
            jsonArrayGeneral = new JSONArray(string);
            Collections.sort(aListCalcul);

            for(int i20=0;i20<jsonArrayGeneral.length();i20++) {

                for (int i10 = 0; i10 < jsonArrayGeneral.length(); i10++) {
                    if (AlistCalculHeure.get(i10).equals(aListCalcul.get(i20))) {
                        AlistLibelle.add(i20, ArrayLibelle[i10]);
                        AlistHeureDebutComplet.add(i20, ArrayHeureDebutComplet[i10]);
                        AlistHeureFinComplet.add(i20, ArrayHeureFinComplet[i10]);
                        AlistProfesseur.add(i20, ArrayProfesseur[i10]);
                        AlistArraySalle.add(i20, ArraySalle[i10]);
                    }
                }
            }



            //   }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        //Création des layouts necessaire à l'affichage.

        LinearLayout ViewPrincipale = (LinearLayout) findViewById (R.id.LlGeneral);
        LinearLayout llHoriz = new LinearLayout(getBaseContext());
        LinearLayout llVert1 = new LinearLayout(getBaseContext());
        LinearLayout llVert2 = new LinearLayout(getBaseContext());
        LinearLayout llVert3 = new LinearLayout(getBaseContext());
        ViewPrincipale.removeAllViews();
        iCouleurTableau = 0;


        for(int i50 = 0; i50<jsonArrayGeneral.length();i50++) {

            llHoriz = CreationHorizLayout();
            llVert1 = CreationVerticLayout();
            llVert2 = CreationVerticLayout();
            llVert3 = CreationVerticLayout();
            iCouleurTableau++;
            TextView tv = CreationTextView(AlistHeureDebutComplet.get(i50));
            TextView tv2 = CreationTextView(AlistHeureFinComplet.get(i50));
            LargeurChampsHeure = false;
            TextView tv3 = CreationTextView(AlistLibelle.get(i50));
            TextView tv4 = CreationTextView(AlistProfesseur.get(i50));
            TextView tv5 = CreationTextView(AlistArraySalle.get(i50));
            TextView tv6 = CreationTextView(" ");
            LargeurChampsHeure = true;


            ViewPrincipale.addView(llHoriz);
            llHoriz.addView(llVert1);
            llHoriz.addView(llVert2);
            llHoriz.addView(llVert3);

            llVert1.addView(tv);
            llVert1.addView(tv2);
            llVert2.addView(tv3);
            llVert2.addView(tv4);
            llVert3.addView(tv5);
            llVert3.addView(tv6);
        }




    }

    public  TextView CreationTextView (String text)
    {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int HauteurEcran = displayMetrics.heightPixels;
        int LargeurEcran = displayMetrics.widthPixels;

        TextView tv = new TextView (getBaseContext());
        tv.setText (text) ;
     /*   if(iCouleurHeure == 0){
            tv.setWidth(LargeurEcran/5);
        }else
        {
            tv.setWidth(LargeurEcran/2);          // Permet d'être à 20-50-30%
        } */
        if(LargeurChampsHeure){
            tv.setWidth(LargeurEcran/5);
        }
        else
        {
            tv.setWidth(LargeurEcran/2);
        }
        tv.setHeight(HauteurEcran/11);
        // tv.setHeight(200);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(Color.BLACK) ;
        return tv;
    }

    public  LinearLayout CreationHorizLayout()
    {
        LinearLayout ll = new LinearLayout (getBaseContext());
        ll.setOrientation(LinearLayout.HORIZONTAL);
      //  ll.setWeightSum(3);


       /* LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) ll.getLayoutParams();
        params.height = 500;
        params.width = 500;
        ll.setLayoutParams(params); */

        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));


        ShapeDrawable sd = new ShapeDrawable();
        sd.setShape(new RectShape());
        //sd.getPaint().setColor(Color.BLUE);
        sd.getPaint().setStrokeWidth(5);
        sd.getPaint().setStyle(Paint.Style.STROKE);
        ll.setBackground(sd);

        return ll;
    }

    public LinearLayout CreationVerticLayout(){




        LinearLayout ll = new LinearLayout(getBaseContext());
        ll.setOrientation(LinearLayout.VERTICAL);

        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        ShapeDrawable sd = new ShapeDrawable();
        // sd.setPadding(100,100,100,100);

        // sd.setShape(new RectShape());
        if ((iCouleurTableau % 2) == 1){
            sd.getPaint().setColor(Color.argb(235,235,235,235));

        }
        else{
            sd.getPaint().setColor(Color.argb(201,201,201,201));

        }


        // sd.getPaint().setStrokeWidth(5);
        // sd.getPaint().setStyle(Paint.Style.STROKE);
        ll.setBackground(sd);

        return ll;
    }



    public String ParseId(String string){
        string = string.split(":")[0];
        string = string.substring(0, string.length() -1);
        string = string.substring(2);
        return string;
    }



}
