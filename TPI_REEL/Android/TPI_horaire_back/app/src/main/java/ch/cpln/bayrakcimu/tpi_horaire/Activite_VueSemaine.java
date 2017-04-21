package ch.cpln.bayrakcimu.tpi_horaire;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

public class Activite_VueSemaine extends AppCompatActivity {

    // Déclaration des variables globales


    // Toutes les outputs de l'asynctask
    String Output="";
    String Id="";
    String Output2="";
    String Output3="";
    String Output4="";
    String Output5="";
    String Output6="";
    String Output7="";
    String Output8="";


    // Valeur des edittext de la date et de la classe
    String ContenuClasse ="";
    String ContenuDate="";



    int iCouleurTableau = 0;
    Boolean LargeurChampsHeure = true;
    Boolean DateDuJour = true;

    ArrayList<String> AlistHistorique = new ArrayList<String>();
    ArrayList<String> AlistDateSemaine = new ArrayList<String>();


    String[] libelles = new String[100];
    int val=0;
    int value=0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activite__vue_semaine);




        Button btnDatePicker = (Button)findViewById(R.id.btndatepicker2);
        btnDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar DateActuelle=Calendar.getInstance();
                int AnneeActuelle = DateActuelle.get(Calendar.YEAR);
                int MoisActuel=DateActuelle.get(Calendar.MONTH);
                int JourActuel=DateActuelle.get(Calendar.DAY_OF_MONTH);

                final DatePickerDialog mDatePicker=new DatePickerDialog(Activite_VueSemaine.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int AnneeChoisie, int MoisChoisi, int JourChoisi) {

                        int  jour = JourChoisi;
                        int mois = MoisChoisi;
                        int annee = AnneeChoisie;

                        String dateComplet = String.valueOf(jour) + "-" + String.valueOf(mois + 1) + "-" + String.valueOf(annee);
                        EditText et2 = (EditText)findViewById(R.id.EtDate2);
                        et2.setText(dateComplet);

                    }
                },AnneeActuelle, MoisActuel, JourActuel);
                mDatePicker.setTitle("Choisir la date");
                mDatePicker.show();  }

        });



        ContenuEditText();
        DateEnSemaine(ContenuDate);
        Requete();
        EcritureFichier();
        LectureFichier();


/*
        final Spinner spinner = (Spinner)findViewById(R.id.sp1);
        final ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item,
                AlistHistorique);
        spinner.setAdapter(spinnerArrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String lol = spinner.getSelectedItem().toString();
                EditText EtTEst = (EditText)findViewById(R.id.ActvClasse2);
                EtTEst.setText(lol);



            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

*/




        Button BtnPlusSemaine = (Button)findViewById(R.id.BtnPlusSemaine2);
        BtnPlusSemaine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TraitementDate(ContenuDate, 7);
                DateEnSemaine(ContenuDate);
                Requete();
            }
        });


        Button BtnMoinsSemaine = (Button)findViewById(R.id.BtnMoinsSemaine2);
        BtnMoinsSemaine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                   TraitementDate(ContenuDate, -7);
                DateEnSemaine(ContenuDate);
                Requete();
            }
        });


        Button BtnRechercher = (Button)findViewById(R.id.BtnRechercher2);
        BtnRechercher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ContenuEditText();
                DateEnSemaine(ContenuDate);
                Requete();
                EcritureFichier();
                LectureFichier();



            }
        });



    }

    public void TraitementDate(String date, int nombre){


        // Fonction permettant d'ajouter des semaines à la date actuelle

        Date ObjetDate = null;
        try {
            SimpleDateFormat Formater = new SimpleDateFormat("dd-MM-yyyy");
            ObjetDate = Formater.parse(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(ObjetDate);
            calendar.add(Calendar.DATE, nombre);
            ContenuDate = Formater.format(calendar.getTime());

            EditText etDate = (EditText)findViewById(R.id.EtDate2);
            etDate.setText(ContenuDate);

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }



    public void ContenuEditText(){

        // Obtention des contenus des edittests pour les mettre dans des variables.

        // EditText EtClasse = (EditText)findViewById(R.id.ActvClasse);
        AutoCompleteTextView Actv =(AutoCompleteTextView)findViewById(R.id.ActvClasse2);
        ContenuClasse = Actv.getText().toString();


        SimpleDateFormat Formater = new SimpleDateFormat("dd-MM-yyyy");
        Calendar calendar = Calendar.getInstance();
        String JourActuel = Formater.format(calendar.getTime());


        EditText EtDate = (EditText)findViewById(R.id.EtDate2);
        ContenuDate = EtDate.getText().toString();

        //Si c'est la première fois qu'on fait la requete ( ouverture de l'application) alors on mets le jour actuel dans la variable contenuDate.

        if(DateDuJour == true) {
            ContenuDate = JourActuel;
        }
        EtDate.setText(ContenuDate);


        DateDuJour = false;

    }



    public void Requete(){


        //Requete des éléments nécaissaire. Tout d'abord l'id de la classe, puis requête des jours de la semaine en utilisant cet id.

        try {
            Output= new AsyncAffichageHoraire().execute("http://devinter.cpln.ch/pdf/hypercool/controler.php?action=ressource&nom=" + ContenuClasse).get();
            Id =  ParseId(Output);
            Output2 = new AsyncAffichageHoraire().execute("http://devinter.cpln.ch/pdf/hypercool/controler.php?action=horaire&ident=" + Id + "&sub=date&date=" + AlistDateSemaine.get(0)).get();
            Output3 = new AsyncAffichageHoraire().execute("http://devinter.cpln.ch/pdf/hypercool/controler.php?action=horaire&ident=" + Id + "&sub=date&date=" +  AlistDateSemaine.get(1)).get();
            Output4 = new AsyncAffichageHoraire().execute("http://devinter.cpln.ch/pdf/hypercool/controler.php?action=horaire&ident=" + Id + "&sub=date&date=" +  AlistDateSemaine.get(2)).get();
            Output5 = new AsyncAffichageHoraire().execute("http://devinter.cpln.ch/pdf/hypercool/controler.php?action=horaire&ident=" + Id + "&sub=date&date=" +  AlistDateSemaine.get(3)).get();
            Output6 = new AsyncAffichageHoraire().execute("http://devinter.cpln.ch/pdf/hypercool/controler.php?action=horaire&ident=" + Id + "&sub=date&date=" +  AlistDateSemaine.get(4)).get();
            Output7 = new AsyncAffichageHoraire().execute("http://devinter.cpln.ch/pdf/hypercool/controler.php?action=horaire&ident=" + Id + "&sub=date&date=" +  AlistDateSemaine.get(5)).get();
            Output8 = new AsyncAffichageHoraire().execute("http://devinter.cpln.ch/pdf/hypercool/controler.php?action=horaire&ident=" + Id + "&sub=date&date=" +  AlistDateSemaine.get(6)).get();

            //Fonction pour traiter les données et l'affichage.

            ParseOutput(Output2, Output3, Output4, Output5, Output6, Output7, Output8);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void ParseOutput(String output2, String output3, String output4, String output5, String output6, String output7, String output8) {



        //Création des arraylists qui vont contenir toutes les données par thème (prof, salle...)

        ArrayList<String>[] alistGeneralProf = new ArrayList[10];
        for(int i = 0;i<7;i++){
            alistGeneralProf[i] = new ArrayList<>();
        }
        /*
        alistGeneralProf[0] = new ArrayList<>();
        alistGeneralProf[1] = new ArrayList<>();
        alistGeneralProf[2] = new ArrayList<>();
        alistGeneralProf[3] = new ArrayList<>();
        alistGeneralProf[4] = new ArrayList<>();
        alistGeneralProf[5] = new ArrayList<>();
        alistGeneralProf[6] = new ArrayList<>(); */

        ArrayList<String>[] alistGeneralSalle = new ArrayList[10];
        for(int i= 0;i<7;i++){
            alistGeneralSalle[i] = new ArrayList<>();
        }
/*
        alistGeneralSalle[0] = new ArrayList<>();
        alistGeneralSalle[1] = new ArrayList<>();
        alistGeneralSalle[2] = new ArrayList<>();
        alistGeneralSalle[3] = new ArrayList<>();
        alistGeneralSalle[4] = new ArrayList<>();
        alistGeneralSalle[5] = new ArrayList<>();
        alistGeneralSalle[6] = new ArrayList<>();
*/
        ArrayList<String>[] alistGeneralCodeMatiere = new ArrayList[10];
        for(int i=0;i<7;i++){
            alistGeneralCodeMatiere[i] = new ArrayList<>();
        }

        ArrayList<String>[] alistGeneralLibelle = new ArrayList[10];
        for(int i=0;i<7;i++){
            alistGeneralLibelle[i] = new ArrayList<>();
        }
        /*
        alistGeneralLibelle[0] = new ArrayList<>();
        alistGeneralLibelle[1] = new ArrayList<>();
        alistGeneralLibelle[2] = new ArrayList<>();
        alistGeneralLibelle[3] = new ArrayList<>();
        alistGeneralLibelle[4] = new ArrayList<>();
        alistGeneralLibelle[5] = new ArrayList<>();
        alistGeneralLibelle[6] = new ArrayList<>();
*/

        ArrayList<String>[] alistGeneralHeureDebutComplet = new ArrayList[10];
        for(int i=0;i<7;i++){
            alistGeneralHeureDebutComplet[i] = new ArrayList<>();
        }
        /*
        alistGeneralHeureDebutComplet[0] = new ArrayList<>();
        alistGeneralHeureDebutComplet[1] = new ArrayList<>();
        alistGeneralHeureDebutComplet[2] = new ArrayList<>();
        alistGeneralHeureDebutComplet[3] = new ArrayList<>();
        alistGeneralHeureDebutComplet[4] = new ArrayList<>();
        alistGeneralHeureDebutComplet[5] = new ArrayList<>();
        alistGeneralHeureDebutComplet[6] = new ArrayList<>();
*/


        ArrayList<String>[] alistGeneralHeureFinComplet = new ArrayList[10];
        for(int i=0;i<7;i++){
            alistGeneralHeureFinComplet[i] = new ArrayList<>();
        }
        /*
        alistGeneralHeureFinComplet[0] = new ArrayList<>();
        alistGeneralHeureFinComplet[1] = new ArrayList<>();
        alistGeneralHeureFinComplet[2] = new ArrayList<>();
        alistGeneralHeureFinComplet[3] = new ArrayList<>();
        alistGeneralHeureFinComplet[4] = new ArrayList<>();
        alistGeneralHeureFinComplet[5] = new ArrayList<>();
        alistGeneralHeureFinComplet[6] = new ArrayList<>();

*/

        ArrayList<Integer>[] alistCalcul = new ArrayList[10];
        for(int i=0;i<7;i++){
            alistCalcul[i] = new ArrayList<>();
        }
        /*
        alistCalcul[0] = new ArrayList<>();
        alistCalcul[1] = new ArrayList<>();
        alistCalcul[2] = new ArrayList<>();
        alistCalcul[3] = new ArrayList<>();
        alistCalcul[4] = new ArrayList<>();
        alistCalcul[5] = new ArrayList<>();
        alistCalcul[6] = new ArrayList<>();
*/

        ArrayList<Integer>[] alistCalculHeure = new ArrayList[10];
        for(int i=0; i<7;i++){
            alistCalculHeure[i] = new ArrayList<>();
        }
        /*
        alistCalculHeure[0] = new ArrayList<>();
        alistCalculHeure[1] = new ArrayList<>();
        alistCalculHeure[2] = new ArrayList<>();
        alistCalculHeure[3] = new ArrayList<>();
        alistCalculHeure[4] = new ArrayList<>();
        alistCalculHeure[5] = new ArrayList<>();
        alistCalculHeure[6] = new ArrayList<>();
*/


        // Création du tableau de couleur pour les branches.

        int[] arrayCouleurs = new int[100];
        arrayCouleurs[0] = Color.rgb(255,255,255);
        arrayCouleurs[1] = Color.rgb(153,255,255);
        arrayCouleurs[2] = Color.rgb(204,255,204);
        arrayCouleurs[3] = Color.rgb(255,204,255);
        arrayCouleurs[4] = Color.rgb(255,102,178);
        arrayCouleurs[5] = Color.rgb(200,59,245);
        arrayCouleurs[6] = Color.rgb(255,51,123);
        arrayCouleurs[7] = Color.rgb(255,100,200);
        arrayCouleurs[8] = Color.rgb(222,76,51);
        arrayCouleurs[9] = Color.rgb(210,234,51);
        arrayCouleurs[10] = Color.rgb(10,145,51);
        arrayCouleurs[11] = Color.rgb(255,51,54);
        arrayCouleurs[12] = Color.rgb(255,51,151);
        arrayCouleurs[13] = Color.rgb(100,51,251);
        arrayCouleurs[14] = Color.rgb(255,21,71);
        arrayCouleurs[15] = Color.rgb(143,51,51);
        arrayCouleurs[16] = Color.rgb(56,51,51);
        arrayCouleurs[17] = Color.rgb(125,51,51);
        arrayCouleurs[18] = Color.rgb(255,65,510);
        arrayCouleurs[19] = Color.rgb(200,123,45);




        String[] output = {output2,output3,output4,output5,output6,output7,output8};

        // Création des arrays car plus simple d'utilisation.

        String[][] arrayLibelle = new String[100][100];
        String[][] arrayProf = new String[100][100];
        String[][] arrayHeureDebut = new String[100][100];
        String[][] arrayHeureFin = new String[100][100];
        String[][] arraySalle = new String[100][100];
        String[][] arrayHeureDebutComplet = new String[100][100];
        String[][] arrayHeureFinComplet = new String[100][100];
        Integer[][] arrayCalcul = new Integer[100][100];
        String[][] arrayCodeMatiere = new String[100][100];

        // ArrayList<String>[][] alistLibelle = new ArrayList[100][100];
        // alistLibelle[0][0]= new ArrayList<String>();




        int iHeureDebut = 0;
        int iHeureFin = 0;


        // Le nombre de branches reçues pour une journée

        Integer[] NombreInfoRecu = new Integer[100];



        try {

            for(int iJour= 0; iJour<6;iJour++) {



                JSONArray jsonArraygeneral2 = new JSONArray(output[iJour]);
                for (int iNbBranche = 0; iNbBranche < jsonArraygeneral2.length(); iNbBranche++) {

                    JSONArray jsonArrayGeneral = new JSONArray(output[iJour]);


                    JSONObject jsonObjectGeneral = jsonArrayGeneral.getJSONObject(iNbBranche);
                    arrayLibelle[iJour][iNbBranche]=jsonObjectGeneral.getString("libelle");
                    arrayCodeMatiere[iJour][iNbBranche]=jsonObjectGeneral.getString("codeMatiere");
                    arrayHeureDebut[iJour][iNbBranche]=jsonObjectGeneral.getString("heureDebut");
                    arrayHeureFin[iJour][iNbBranche]=jsonObjectGeneral.getString("heureFin");



                    // Si pas de prof, alors on met un / sinon ca fait crash.
                    try{
                        JSONArray jsonArrayProf = jsonObjectGeneral.getJSONArray("professeur");
                        arrayProf[iJour][iNbBranche]=jsonArrayProf.getString(0);
                    }catch (JSONException e){
                        arrayProf[iJour][iNbBranche]="/";
                    }

                    try {
                        JSONArray jsonArraySalle = jsonObjectGeneral.getJSONArray("salle");
                        arraySalle[iJour][iNbBranche]=jsonArraySalle.getString(0);
                    }catch(JSONException e){
                        arraySalle[iJour][iNbBranche]="/-";
                    }


                    // On parse les profs pour avoir uniquement le nom.
                    try{
                        arrayProf[iJour][iNbBranche] = arrayProf[iJour][iNbBranche].substring(0, arrayProf[iJour][iNbBranche].indexOf(" "));
                    }catch(Exception e)
                    {

                    }

                    // On parse la salle car le code contient des nombres inutiles (B106B-sdasda)
                    try{
                        arraySalle[iJour][iNbBranche] = arraySalle[iJour][iNbBranche].substring(0, arraySalle[iJour][iNbBranche].indexOf("-"));
                    }catch(Exception e)
                    {

                    }


                    arrayHeureDebutComplet[iJour][iNbBranche] = arrayHeureDebut[iJour][iNbBranche];
                    arrayHeureFinComplet[iJour][iNbBranche] = arrayHeureFin[iJour][iNbBranche];
                    arrayHeureDebut[iJour][iNbBranche] = arrayHeureDebut[iJour][iNbBranche].substring(0,arrayHeureDebut[iJour][iNbBranche].length()-3);
                    arrayHeureFin[iJour][iNbBranche] = arrayHeureFin[iJour][iNbBranche].substring(0, arrayHeureFin[iJour][iNbBranche].length()-3);

                    try {
                        iHeureDebut = Integer.parseInt(arrayHeureDebut[iJour][iNbBranche]);
                        iHeureFin = Integer.parseInt(arrayHeureFin[iJour][iNbBranche]);
                        arrayCalcul[iJour][iNbBranche] = iHeureDebut + iHeureFin;
                    }catch(NumberFormatException e){

                    }


                    // Mise des données depuis les arrays aux arraylists.

                    alistGeneralProf[iJour].add(iNbBranche, arrayProf[iJour][iNbBranche]);
                    alistGeneralSalle[iJour].add(iNbBranche, arraySalle[iJour][iNbBranche]);
                    alistGeneralLibelle[iJour].add(iNbBranche, arrayLibelle[iJour][iNbBranche]);
                    alistGeneralHeureDebutComplet[iJour].add(iNbBranche, arrayHeureDebut[iJour][iNbBranche]);
                    alistGeneralHeureFinComplet[iJour].add(iNbBranche, arrayHeureFinComplet[iJour][iNbBranche]);
                    alistCalcul[iJour].add(iNbBranche, arrayCalcul[iJour][iNbBranche]);
                    alistCalculHeure[iJour].add(iNbBranche, arrayCalcul[iJour][iNbBranche]);
                    alistGeneralCodeMatiere[iJour].add(iNbBranche, arrayCodeMatiere[iJour][iNbBranche]);


                    NombreInfoRecu[iJour] = iNbBranche + 1;


                }
            }




            for(int i=0;i<7;i++){
                if(NombreInfoRecu[i]==null|| NombreInfoRecu[i].equals("null")){NombreInfoRecu[i]=0;}
            }
/*
            if(NombreInfoRecu[0] == null || NombreInfoRecu[0].equals("null")){ NombreInfoRecu[0] = 0;}
            if(NombreInfoRecu[1] == null || NombreInfoRecu[1].equals("null")){ NombreInfoRecu[1] = 0;}
            if(NombreInfoRecu[2] == null || NombreInfoRecu[2].equals("null")){ NombreInfoRecu[2] = 0;}
            if(NombreInfoRecu[3] == null || NombreInfoRecu[3].equals("null")){ NombreInfoRecu[3] = 0;}
            if(NombreInfoRecu[4] == null || NombreInfoRecu[4].equals("null")){ NombreInfoRecu[4] = 0;}
            if(NombreInfoRecu[5] == null || NombreInfoRecu[5].equals("null")){ NombreInfoRecu[5] = 0;}
            if(NombreInfoRecu[6] == null || NombreInfoRecu[6].equals("null")){ NombreInfoRecu[6] = 0;}
*/


            // Tri des arraylists en fonction du calcul.

            for(int i=0;i<7;i++){
                Collections.sort(alistCalcul[i]);
            }
/*
            Collections.sort(alistCalcul[0]);
            Collections.sort(alistCalcul[1]);
            Collections.sort(alistCalcul[2]);
            Collections.sort(alistCalcul[3]);
            Collections.sort(alistCalcul[4]);
            Collections.sort(alistCalcul[5]);
            Collections.sort(alistCalcul[6]);
*/


            // Basculement des places en fonction du calcul,

            for (int iJour = 0;iJour<6; iJour++) {

                for (int i20 = 0; i20 <NombreInfoRecu[iJour];i20++){

                    for(int i10 = 0; i10<NombreInfoRecu[iJour];i10++){

                        if(alistCalculHeure[iJour].get(i10).equals(alistCalcul[iJour].get(i20))){
                            alistGeneralLibelle[iJour].add(i20, arrayLibelle[iJour][i10]);
                            alistGeneralHeureDebutComplet[iJour].add(i20, arrayHeureDebutComplet[iJour][i10]);
                            alistGeneralHeureFinComplet[iJour].add(i20, arrayHeureFinComplet[iJour][i10]);
                            alistGeneralProf[iJour].add(i20, arrayProf[iJour][i10]);
                            alistGeneralSalle[iJour].add(i20, arraySalle[iJour][i10]);
                            alistGeneralCodeMatiere[iJour].add(i20,arrayCodeMatiere[iJour][i10]);
                        }

                    }


                }



            }



            TextView tv10 = (TextView)findViewById(R.id.Text10);
     //   tv10.setText(alistGeneralProf[1].get(0));





            // Tableau de toutes les périodes pour pouvoir les utiliser pour savoir le nombre de période.

            String[] arrayPeriodes = {"07h25","08h10","08h55","09h40","10h45","11h30","12h15","13h10","13h55","14h55","15h40","16h25","17h30","18h15","19h00","20h00","20h45","21h30","22h15"};
            String[] arrayJourSemaine = {"Lundi","Mardi","Mercredi","Jeudi","Vendredi"};


            // On va mettre les layouts du xml dans le ll[]

            LinearLayout[][] llGeneral = new LinearLayout[100][100];

            for (int i2 =0; i2<5;i2++) {
                for (int i4 = 0; i4 < 19; i4++) {
                    String Idtv = "ll_" + arrayJourSemaine[i2] + "_" + arrayPeriodes[i4];
                    int resID = getResources().getIdentifier(Idtv, "id", getPackageName());
                    llGeneral[i2][i4] = (LinearLayout) findViewById(resID);
                }
            }

            TextView[][] tvGeneralLibelle = new TextView[200][200];


            for (int i2 =0; i2<5;i2++) {
                for (int i4 = 0; i4 < 19; i4++) {
                    String Idtv = "tv_" + arrayJourSemaine[i2] + "_" + arrayPeriodes[i4] + "_libelle";
                    int resID = getResources().getIdentifier(Idtv, "id", getPackageName());
                    tvGeneralLibelle[i2][i4] = (TextView) findViewById(resID);
                }
            }



            TextView[][] tvGeneralSalle = new TextView[200][200];


            for (int i2 =0; i2<5;i2++) {
                for (int i4 = 0; i4 < 19; i4++) {
                    String Idtv = "tv_" + arrayJourSemaine[i2] + "_" + arrayPeriodes[i4] + "_salle";
                    int resID = getResources().getIdentifier(Idtv, "id", getPackageName());
                    tvGeneralSalle[i2][i4] = (TextView) findViewById(resID);
                }
            }


            // Vidage de toutes les cases.

            tvGeneralLibelle[4][7].setText("");

            for(int i2 = 0; i2<5;i2++) {
                for (int i= 0;i<19;i++) {
                    llGeneral[i2][i].setBackgroundColor(Color.TRANSPARENT);
                    llGeneral[i2][i].setBackgroundResource(R.drawable.border);
                    tvGeneralLibelle[i2][i].setBackgroundColor(Color.TRANSPARENT);
                    tvGeneralSalle[i2][i].setBackgroundColor(Color.TRANSPARENT);
                    tvGeneralLibelle[i2][i].setText("");
                    tvGeneralSalle[i2][i].setText("");
                }
            }






            // Selon l'heure début on va envoyer le numero du layout à la fonction AffichSemaine.

            for(int iJour = 0; iJour<5;iJour++) {

                for (int iBranche = 0; iBranche < NombreInfoRecu[iJour]; iBranche++) {

                    switch (alistGeneralHeureDebutComplet[iJour].get(iBranche)) {

                        case "07h25":
                            int numeroll0 = 0;
                            String heureDebut0 = "07h25";
                            AfficheSemaine(iJour,iBranche,numeroll0,heureDebut0,llGeneral,arrayPeriodes,alistGeneralHeureFinComplet,tvGeneralLibelle,tvGeneralSalle,alistGeneralLibelle,alistGeneralSalle,alistGeneralProf,alistGeneralCodeMatiere,arrayCouleurs);
                            break;
                        //     case "08h30":
                        case "08h10":
                            int numeroll1 = 1;
                            String heureDebut1 = "08h10";
                            AfficheSemaine(iJour,iBranche,numeroll1,heureDebut1,llGeneral,arrayPeriodes,alistGeneralHeureFinComplet,tvGeneralLibelle,tvGeneralSalle,alistGeneralLibelle,alistGeneralSalle,alistGeneralProf,alistGeneralCodeMatiere,arrayCouleurs);
                            break;
                        //       case "09h15":
                        case "08h55":
                            int numeroll2 = 2;
                            String heureDebut2 = "08h55";
                            AfficheSemaine(iJour,iBranche,numeroll2,heureDebut2,llGeneral,arrayPeriodes,alistGeneralHeureFinComplet,tvGeneralLibelle,tvGeneralSalle,alistGeneralLibelle,alistGeneralSalle,alistGeneralProf,alistGeneralCodeMatiere,arrayCouleurs);
                            break;
                        case "09h40":
                            int numeroll3 = 3;
                            String heureDebut3 = "09h40";
                            AfficheSemaine(iJour,iBranche,numeroll3,heureDebut3,llGeneral,arrayPeriodes,alistGeneralHeureFinComplet,tvGeneralLibelle,tvGeneralSalle,alistGeneralLibelle,alistGeneralSalle,alistGeneralProf,alistGeneralCodeMatiere,arrayCouleurs);
                            break;
                        case "10h45":
                            int numeroll4 = 4;
                            String heureDebut4 = "10h45";
                            AfficheSemaine(iJour,iBranche,numeroll4,heureDebut4,llGeneral,arrayPeriodes,alistGeneralHeureFinComplet,tvGeneralLibelle,tvGeneralSalle,alistGeneralLibelle,alistGeneralSalle,alistGeneralProf,alistGeneralCodeMatiere,arrayCouleurs);
                            break;
                        case "11h30":
                            int numeroll5 = 5;
                            String heureDebut5 = "11h30";
                            AfficheSemaine(iJour,iBranche,numeroll5,heureDebut5,llGeneral,arrayPeriodes,alistGeneralHeureFinComplet,tvGeneralLibelle,tvGeneralSalle,alistGeneralLibelle,alistGeneralSalle,alistGeneralProf,alistGeneralCodeMatiere,arrayCouleurs);
                            break;
                        case "12h15":
                            int numeroll6 = 6;
                            String heureDebut6 = "12h15";
                            AfficheSemaine(iJour,iBranche,numeroll6,heureDebut6,llGeneral,arrayPeriodes,alistGeneralHeureFinComplet,tvGeneralLibelle,tvGeneralSalle,alistGeneralLibelle,alistGeneralSalle,alistGeneralProf,alistGeneralCodeMatiere,arrayCouleurs);
                            break;
                        case "13h10":
                            int numeroll7 = 7;
                            String heureDebut7 = "13h10";
                            AfficheSemaine(iJour,iBranche,numeroll7,heureDebut7,llGeneral,arrayPeriodes,alistGeneralHeureFinComplet,tvGeneralLibelle,tvGeneralSalle,alistGeneralLibelle,alistGeneralSalle,alistGeneralProf,alistGeneralCodeMatiere,arrayCouleurs);
                            break;
                        case "13h55":
                            int numeroll8 = 8;
                            String heureDebut8 = "13h55";
                            AfficheSemaine(iJour,iBranche,numeroll8,heureDebut8,llGeneral,arrayPeriodes,alistGeneralHeureFinComplet,tvGeneralLibelle,tvGeneralSalle,alistGeneralLibelle,alistGeneralSalle,alistGeneralProf,alistGeneralCodeMatiere,arrayCouleurs);
                            break;
                        case "14h55":
                            int numeroll9 = 9;
                            String heureDebut9 = "14h55";
                            AfficheSemaine(iJour,iBranche,numeroll9,heureDebut9,llGeneral,arrayPeriodes,alistGeneralHeureFinComplet,tvGeneralLibelle,tvGeneralSalle,alistGeneralLibelle,alistGeneralSalle,alistGeneralProf,alistGeneralCodeMatiere,arrayCouleurs);
                            break;
                        case "15h40":
                            int numeroll10 = 10;
                            String heureDebut10 = "15h40";
                            AfficheSemaine(iJour,iBranche,numeroll10,heureDebut10,llGeneral,arrayPeriodes,alistGeneralHeureFinComplet,tvGeneralLibelle,tvGeneralSalle,alistGeneralLibelle,alistGeneralSalle,alistGeneralProf,alistGeneralCodeMatiere,arrayCouleurs);
                            break;
                        case "16h25":
                            int numeroll11 = 11;
                            String heureDebut11 = "16h25";
                            AfficheSemaine(iJour,iBranche,numeroll11,heureDebut11,llGeneral,arrayPeriodes,alistGeneralHeureFinComplet,tvGeneralLibelle,tvGeneralSalle,alistGeneralLibelle,alistGeneralSalle,alistGeneralProf,alistGeneralCodeMatiere,arrayCouleurs);
                            break;
                        case "17h30":
                            int numeroll12 = 12;
                            String heureDebut12 = "17h30";
                            AfficheSemaine(iJour,iBranche,numeroll12,heureDebut12,llGeneral,arrayPeriodes,alistGeneralHeureFinComplet,tvGeneralLibelle,tvGeneralSalle,alistGeneralLibelle,alistGeneralSalle,alistGeneralProf,alistGeneralCodeMatiere,arrayCouleurs);
                            break;
                        case "18h15":
                            int numeroll13 = 13;
                            String heureDebut13 = "18h15";
                            AfficheSemaine(iJour,iBranche,numeroll13,heureDebut13,llGeneral,arrayPeriodes,alistGeneralHeureFinComplet,tvGeneralLibelle,tvGeneralSalle,alistGeneralLibelle,alistGeneralSalle,alistGeneralProf,alistGeneralCodeMatiere,arrayCouleurs);
                            break;
                        case "19h00":
                            int numeroll14 = 14;
                            String heureDebut14 = "19h00";
                            AfficheSemaine(iJour,iBranche,numeroll14,heureDebut14,llGeneral,arrayPeriodes,alistGeneralHeureFinComplet,tvGeneralLibelle,tvGeneralSalle,alistGeneralLibelle,alistGeneralSalle,alistGeneralProf,alistGeneralCodeMatiere,arrayCouleurs);
                            break;
                        case "20h00":
                            int numeroll15 = 15;
                            String heureDebut15 = "20h00";
                            AfficheSemaine(iJour,iBranche,numeroll15,heureDebut15,llGeneral,arrayPeriodes,alistGeneralHeureFinComplet,tvGeneralLibelle,tvGeneralSalle,alistGeneralLibelle,alistGeneralSalle,alistGeneralProf,alistGeneralCodeMatiere,arrayCouleurs);
                            break;
                        case "20h45":
                            int numeroll16 = 16;
                            String heureDebut16 = "20h45";
                            AfficheSemaine(iJour,iBranche,numeroll16,heureDebut16,llGeneral,arrayPeriodes,alistGeneralHeureFinComplet,tvGeneralLibelle,tvGeneralSalle,alistGeneralLibelle,alistGeneralSalle,alistGeneralProf,alistGeneralCodeMatiere,arrayCouleurs);
                            break;
                        case "21h30":
                            int numeroll17 = 17;
                            String heureDebut17 = "21h30";
                            AfficheSemaine(iJour,iBranche,numeroll17,heureDebut17,llGeneral,arrayPeriodes,alistGeneralHeureFinComplet,tvGeneralLibelle,tvGeneralSalle,alistGeneralLibelle,alistGeneralSalle,alistGeneralProf,alistGeneralCodeMatiere,arrayCouleurs);
                            break;
                        case "22h15":
                            int numeroll18 = 18;
                            String heureDebut18 = "22h15";
                            AfficheSemaine(iJour,iBranche,numeroll18,heureDebut18,llGeneral,arrayPeriodes,alistGeneralHeureFinComplet,tvGeneralLibelle,tvGeneralSalle,alistGeneralLibelle,alistGeneralSalle,alistGeneralProf,alistGeneralCodeMatiere,arrayCouleurs);
                            break;

                        /*
                        case "08h30":
                            int numeroll19 = 1;
                            AfficheSemaine2(i200,i100,numeroll19,llGeneral,alistGeneralHeureFinComplet,tvGeneralLibelle,tvGeneralSalle,alistGeneralLibelle,alistGeneralSalle,alistGeneralProf);


                            break;
                        case "09h15":
                            int numeroll20 = 2;
                            AfficheSemaine2(i200,i100,numeroll20,llGeneral,alistGeneralHeureFinComplet,tvGeneralLibelle,tvGeneralSalle,alistGeneralLibelle,alistGeneralSalle,alistGeneralProf);
                            break;
                        case "10h00":
                            int numeroll21 = 3;
                            AfficheSemaine2(i200,i100,numeroll21,llGeneral,alistGeneralHeureFinComplet,tvGeneralLibelle,tvGeneralSalle,alistGeneralLibelle,alistGeneralSalle,alistGeneralProf);
                            break;
*/
                    }


                }


            }


            val=0;
            value=0;
            libelles = new String[libelles.length];















        } catch (JSONException e) {
            e.printStackTrace();
        }



    }

    public void AfficheSemaine(int i200, int i100, int numll, String heureDebut, LinearLayout[][] llGeneral, String[] arrayperiode, ArrayList<String>[] alistheureFin,
                               TextView[][] tvLibelle, TextView[][] tvSalle, ArrayList<String>[] alistLibelle, ArrayList<String>[] alistSalle, ArrayList<String>[] alistProf,
                               ArrayList<String>[] alistCodeMatiere, int[] arrayCouleurs){








        //Utiliser pour la couleur selon les branches.

        if (Arrays.asList(libelles).contains(alistLibelle[i200].get(i100))) {
            value = Arrays.asList(libelles).indexOf(alistLibelle[i200].get(i100));
        }else
        {
            libelles[val] = alistLibelle[i200].get(i100);
            val++;
            value = Arrays.asList(libelles).indexOf(alistLibelle[i200].get(i100));
        }
/*
//    NORMAL - AVEC LIBELLE
        int numeroll = numll;
        // llGeneral[i200][numeroll].setBackgroundColor(color);
        llGeneral[i200][numeroll].setBackgroundColor(arrayCouleurs[value]);


        int index1 = 0;
        int index2 = 0;
        int indexFinal = 0;
        index1 = Arrays.asList(arrayperiode).indexOf(heureDebut);
        String heureFin = alistheureFin[i200].get(i100);
        index2 = Arrays.asList(arrayperiode).indexOf(heureFin);
        indexFinal = index2-index1;
        tvLibelle[i200][numeroll].setText(alistLibelle[i200].get(i100));
        tvSalle[i200][numeroll].setText(alistSalle[i200].get(i100));

        for(int i20=1;i20<indexFinal;i20++){
            //  llGeneral[i200][numeroll + i20].setBackgroundColor(color);
            llGeneral[i200][numeroll + i20].setBackgroundColor(arrayCouleurs[value]);

        }


        if (indexFinal==2){
            tvLibelle[i200][numeroll + 1].setText(alistProf[i200].get(i100));


        }

        if(indexFinal==3||indexFinal==4){
            tvLibelle[i200][numeroll].setText("");
            tvSalle[i200][numeroll].setText("");
            tvLibelle[i200][numeroll + 1].setText(alistLibelle[i200].get(i100));
            tvSalle[i200][numeroll + 1].setText(alistSalle[i200].get(i100));
            tvLibelle[i200][numeroll + 2].setText(alistProf[i200].get(i100));
        }
        if(indexFinal==5){
            tvLibelle[i200][numeroll].setText("");
            tvSalle[i200][numeroll].setText("");
            tvLibelle[i200][numeroll + 2].setText(alistLibelle[i200].get(i100));
            tvSalle[i200][numeroll + 2].setText(alistSalle[i200].get(i100));
            tvLibelle[i200][numeroll + 3].setText(alistProf[i200].get(i100));
        }
*/

/*
        int length = alistLibelle[i200].get(i100).length();
        if(length >10){
            String lol1 = alistLibelle[i200].get(i100).substring(0,10);
            String lol2 = alistLibelle[i200].get(i100).substring(10);
            tvLibelle[i200][numeroll + 1].setText(String.valueOf(length));
        }
*/

// AVEC 1 période code matière et séparation après 20 char.
        int numeroll = numll;
        int NombreCharMax = 20;
        // llGeneral[i200][numeroll].setBackgroundColor(color);
        llGeneral[i200][numeroll].setBackgroundColor(arrayCouleurs[value]);


        int index1 = 0;
        int index2 = 0;
        int indexFinal = 0;
        index1 = Arrays.asList(arrayperiode).indexOf(heureDebut);
        String heureFin = alistheureFin[i200].get(i100);
        index2 = Arrays.asList(arrayperiode).indexOf(heureFin);
        indexFinal = index2-index1;
        tvLibelle[i200][numeroll].setText(alistCodeMatiere[i200].get(i100));
        tvSalle[i200][numeroll].setText(alistSalle[i200].get(i100));
        int length = alistLibelle[i200].get(i100).length();
        String partie1="";
        String partie2="";
        if(length>NombreCharMax) {
             partie1 = alistLibelle[i200].get(i100).substring(0, NombreCharMax);
             partie2 = alistLibelle[i200].get(i100).substring(NombreCharMax);
        }

        for(int i20=1;i20<indexFinal;i20++){
            //  llGeneral[i200][numeroll + i20].setBackgroundColor(color);
            llGeneral[i200][numeroll + i20].setBackgroundColor(arrayCouleurs[value]);

        }


        if (indexFinal==2){
            if(length >NombreCharMax){

                tvLibelle[i200][numeroll].setText(partie1 + "-");
                tvSalle[i200][numeroll].setText(partie2);
                tvLibelle[i200][numeroll+1].setText(alistSalle[i200].get(i100));
                tvSalle[i200][numeroll+1].setText(alistProf[i200].get(i100));
            }
            else {
                tvLibelle[i200][numeroll].setText(alistLibelle[i200].get(i100));
                tvLibelle[i200][numeroll + 1].setText(alistProf[i200].get(i100));
            }

        }

        if(indexFinal==3||indexFinal==4){
            if(length>NombreCharMax){
                tvLibelle[i200][numeroll].setText("");
                tvSalle[i200][numeroll].setText("");
                tvSalle[i200][numeroll].setText(partie1 + "-");
                tvLibelle[i200][numeroll+1].setText(partie2);
                tvSalle[i200][numeroll+1].setText(alistSalle[i200].get(i100));
                tvLibelle[i200][numeroll+2].setText(alistProf[i200].get(i100));

            }else{
            tvLibelle[i200][numeroll].setText("");
            tvSalle[i200][numeroll].setText("");
            tvLibelle[i200][numeroll + 1].setText(alistLibelle[i200].get(i100));
            tvSalle[i200][numeroll + 1].setText(alistSalle[i200].get(i100));
            tvLibelle[i200][numeroll + 2].setText(alistProf[i200].get(i100));
            }
        }
        if(indexFinal==5){
            if(length>NombreCharMax){
                tvLibelle[i200][numeroll].setText("");
                tvSalle[i200][numeroll].setText("");
                tvSalle[i200][numeroll+1].setText(partie1 + "-");
                tvLibelle[i200][numeroll+2].setText(partie2);
                tvSalle[i200][numeroll+2].setText(alistSalle[i200].get(i100));
                tvLibelle[i200][numeroll+3].setText(alistProf[i200].get(i100));
            }
            else {
                tvLibelle[i200][numeroll].setText("");
                tvSalle[i200][numeroll].setText("");
                tvLibelle[i200][numeroll + 2].setText(alistLibelle[i200].get(i100));
                tvSalle[i200][numeroll + 2].setText(alistSalle[i200].get(i100));
                tvLibelle[i200][numeroll + 3].setText(alistProf[i200].get(i100));
            }
        }



    }

    // Une fois l'output reçu, on va isoler l'id.

    public String ParseId(String string){
        string = string.split(":")[0];
        string = string.substring(0, string.length() -1);
        string = string.substring(2);
        return string;
    }

    // Fonction pour avoir toutes les dates de la semaine en fonction de la date envoyée.

    public void DateEnSemaine(String date) {


        SimpleDateFormat Formater = new SimpleDateFormat("dd-MM-yyyy");
        Date dateObj = null;
        try {
            dateObj = Formater.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateObj);
        calendar.add(Calendar.DAY_OF_MONTH,Calendar.MONDAY - calendar.get(Calendar.DAY_OF_WEEK));

        String datelundi = Formater.format(calendar.getTime());


        for(int i = 0; i<7;i++) {
            AlistDateSemaine.add(i, datelundi);
            calendar.add(Calendar.DATE, 1);
            datelundi = Formater.format(calendar.getTime());
        }

        TextView tv3 = (TextView)findViewById(R.id.Text10);
        tv3.setText( AlistDateSemaine.get(0) + " à " + AlistDateSemaine.get(6));


    }


    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_changer_vue:
                VueJour();
                return true;
            case R.id.action_effacer:
                EffacerRecherches();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void VueJour(){

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }
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
       // SetAutoCompleteTextView();


    }
    public void EcritureFichier(){
        AutoCompleteTextView Actv = (AutoCompleteTextView)findViewById(R.id.ActvClasse2);
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
    public void SuppressionDoublons(){

        HashSet<String> hashSet = new HashSet<String>();
        hashSet.addAll(AlistHistorique);
        AlistHistorique.clear();
        AlistHistorique.addAll(hashSet);

    }


}
