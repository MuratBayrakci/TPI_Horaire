package ch.cpln.bayrakcimu.tpi_horaire;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

public class Activite_VueSemaine extends AppCompatActivity implements AsyncReponse {

    // Déclaration des variables globales



    String strId="";
    String[] Outputs = new String[10];   // Tableau qui va contenir les outputs de la requete 3
    ArrayList<String> AlistLibelleToutesClasses = new ArrayList<String>();



    String strContenuClasse ="";
    String strContenuDate="";
    int iChoixTraitementOutput=0;


    Boolean bPremiereOuverture = true;
    Boolean bEditTexteVide = false;
    Boolean bDateIncorrecte = false;
    Boolean bAlternateur = true;
    Boolean bAfficherContenuActv = false;


    ArrayList<String> AlistHistorique = new ArrayList<String>();
    ArrayList<String> AlistDateSemaine = new ArrayList<String>();  // ArrayList qui va contenir les dates de la semaine actuelle


    String[] libelleBranche = new String[100];  // Variable uqi va être utiliser pour mettre la même couleur aux mêmes branches,
    int val1=0;
    int val2=0;



    int iCptNombreClasse = 0;
    boolean bPlusieursClasseValidees = true;
    ArrayList<String> AlistPlusieursClasses = new ArrayList<String>();

    String strDerniereClasseRecherchee= "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activite__vue_semaine);


//Mise en place du datepicker

        Button btnCalendrier2 = (Button)findViewById(R.id.BtnCalendrier2);
        btnCalendrier2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar cDateActuelle=Calendar.getInstance();
                int iAnneeActuelle = cDateActuelle.get(Calendar.YEAR);
                int iMoisActuel=cDateActuelle.get(Calendar.MONTH);
                int iJourActuel=cDateActuelle.get(Calendar.DAY_OF_MONTH);

                final DatePickerDialog mDatePicker=new DatePickerDialog(Activite_VueSemaine.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int AnneeChoisie, int MoisChoisi, int JourChoisi) {

                        int  ijour = JourChoisi;
                        int imois = MoisChoisi;
                        int iannee = AnneeChoisie;

                        String strDateComplet = String.valueOf(ijour) + "-" + String.valueOf(imois + 1) + "-" + String.valueOf(iannee);
                        EditText etDate2 = (EditText)findViewById(R.id.EtDate2);
                        etDate2.setText(strDateComplet);

                    }
                },iAnneeActuelle, iMoisActuel, iJourActuel);
                mDatePicker.setTitle("Choisir la date");
                mDatePicker.show();  }

        });

//Obligation de passer en orientation paysage
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


        // on n'affiche pas le clavier lors de la première fois.
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );


        strDerniereClasseRecherchee = getDerniereClasseRecherchee();
        ContenuEditText();
        DateEnSemaine(strContenuDate);
        Requete();
        EcritureFichier();
        LectureFichier();
        MiseEnPlaceActv(AlistHistorique);



        Button btnPlusSemaine2 = (Button)findViewById(R.id.BtnPlusSemaine2);
        btnPlusSemaine2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!bPlusieursClasseValidees){
                if(!bEditTexteVide){
                    iChoixTraitementOutput=0;
                    if(!bDateIncorrecte){
                TraitementDate(strContenuDate, 7);
                DateEnSemaine(strContenuDate);
                Requete();
                    }

                }
                }
            }
        });


        Button btnMoinsSemaine2 = (Button)findViewById(R.id.BtnMoinsSemaine2);
        btnMoinsSemaine2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!bPlusieursClasseValidees){
                if(!bEditTexteVide) {
                    iChoixTraitementOutput = 0;
                    if(!bDateIncorrecte){
                    TraitementDate(strContenuDate, -7);
                    DateEnSemaine(strContenuDate);
                    Requete();
                    }
                }}
            }
        });


        Button btnRechercher2 = (Button)findViewById(R.id.BtnRechercher2);
        btnRechercher2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bDateIncorrecte=false;
                bEditTexteVide=false;
                iChoixTraitementOutput=0;
                ContenuEditText();
                if(!bEditTexteVide) {
                    DateEnSemaine(strContenuDate);
                    if(!bDateIncorrecte){
                    Requete();
                    }
                }

            }
        });

        final Button btnAlternateur2 = (Button)findViewById(R.id.BtnAlternateur2);
        btnAlternateur2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bAlternateur){

                    MiseEnPlaceActv(AlistHistorique);
                    Toast.makeText(Activite_VueSemaine.this,"Affichage Historique", Toast.LENGTH_SHORT).show();
                    bAfficherContenuActv = true;
                    btnAlternateur2.setText("Classes");
                }else{

                    MiseEnPlaceActv(AlistLibelleToutesClasses);
                    Toast.makeText(Activite_VueSemaine.this,"Affichage toutes les classes", Toast.LENGTH_SHORT).show();
                    btnAlternateur2.setText("Historique");
                }
                bAlternateur = !bAlternateur;
            }
        });

        final AutoCompleteTextView actvClasse2 = (AutoCompleteTextView)findViewById(R.id.ActvClasse2);
        actvClasse2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bAfficherContenuActv){
                    actvClasse2.showDropDown();
                }
                bAfficherContenuActv=false;

                if(bPlusieursClasseValidees) {
                    CacherClavier(getBaseContext(), actvClasse2);
                    bAfficherContenuActv=true;

                }
            }
        });




        // Mise en place du swipe

        LinearLayout llPrincipal2 = (LinearLayout)findViewById(R.id.LlPricipal2);
        llPrincipal2.setOnTouchListener(new SwipeListener(Activite_VueSemaine.this) {

            public void onSwipeRight() {
                if(!bEditTexteVide) {
                    iChoixTraitementOutput = 0;
                    TraitementDate(strContenuDate, -7);
                    DateEnSemaine(strContenuDate);
                    if(!bDateIncorrecte){
                        Requete();
                    }
                }

            }
            public void onSwipeLeft() {


                if(!bEditTexteVide){
                    iChoixTraitementOutput=0;
                    TraitementDate(strContenuDate, 7);
                    DateEnSemaine(strContenuDate);
                    if(!bDateIncorrecte){
                        Requete();
                    }
                }
            }

        });


    }


    public static void CacherClavier(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        inputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    public void RetourOutput(String output){


        if(iChoixTraitementOutput==0){
            strId = TraitementId(output);

            if(iCptNombreClasse>1 && !bPlusieursClasseValidees){
                Toast.makeText(Activite_VueSemaine.this,"Plusieurs classes trouvées, veuillez choisir la bonne classe", Toast.LENGTH_SHORT).show();
                bAfficherContenuActv=true;
                MiseEnPlaceActv(AlistPlusieursClasses);
                bPlusieursClasseValidees=true;

                AutoCompleteTextView actvClasse2 = (AutoCompleteTextView)findViewById(R.id.ActvClasse2);
                actvClasse2.performClick();

            }
            else {
                bPlusieursClasseValidees = false;

            if("".equals(strId) || strId==null){
                Toast.makeText(Activite_VueSemaine.this,"Classe non trouvée", Toast.LENGTH_SHORT).show();
                bEditTexteVide=true;
            }else {
                AsyncAffichageHoraire[] asyncGeneral = new AsyncAffichageHoraire[8];
                for (int i = 0; i < 7; i++) {
                    asyncGeneral[i] = new AsyncAffichageHoraire(Activite_VueSemaine.this, 0);
                    asyncGeneral[i].delegate = (AsyncReponse) this;
                    asyncGeneral[i].execute("http://devinter.cpln.ch/pdf/hypercool/controler.php?action=horaire&ident=" + strId + "&sub=date&date=" + AlistDateSemaine.get(i));
                       EcritureFichier();
                       LectureFichier();

                    if(!bAlternateur){
                        MiseEnPlaceActv(AlistHistorique);
                    }else{
                        MiseEnPlaceActv(AlistLibelleToutesClasses);
                    }

                }
            } }
        }
        if(iChoixTraitementOutput==1){
            TraitementToutesClasses(output);
        }

        // On remplit le tableau
        if(iChoixTraitementOutput==2 ||iChoixTraitementOutput==3 ||iChoixTraitementOutput==4 ||iChoixTraitementOutput==5 ||iChoixTraitementOutput==6 ||iChoixTraitementOutput==7 ||iChoixTraitementOutput==8){
            Outputs[iChoixTraitementOutput-2] = output;
        }

        // Une fois que toute les requêtes ont été effectuées, on fait appel à la fonction AffichageHoraire.
        if(iChoixTraitementOutput==8) {
            AffichageHoraire(Outputs[0], Outputs[1], Outputs[2], Outputs[3], Outputs[4], Outputs[5], Outputs[6]);
            TextView tvJourDeLaSemaine2 = (TextView)findViewById(R.id.TvJourDeLaSemaine2);
            if(!bDateIncorrecte) {
                tvJourDeLaSemaine2.setText("Lundi " + AlistDateSemaine.get(0) + " au Vendredi " + AlistDateSemaine.get(6));
            }
        }

        iChoixTraitementOutput++;
    }

    public void MiseEnPlaceActv(ArrayList<String> alist){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, alist);
        AutoCompleteTextView actvClasse2 = (AutoCompleteTextView)
                findViewById(R.id.ActvClasse2);
        actvClasse2.setThreshold(0);
        actvClasse2.setAdapter(adapter);
    }

    public void TraitementDate(String date, int nombre){


        // Fonction permettant d'ajouter des semaines à la date actuelle

        Date ObjetDate = null;
        try {
            SimpleDateFormat Formater = new SimpleDateFormat("dd-MM-yyyy");
            ObjetDate = Formater.parse(date);
            Calendar calendrier = Calendar.getInstance();
            calendrier.setTime(ObjetDate);
            calendrier.add(Calendar.DATE, nombre);
            strContenuDate = Formater.format(calendrier.getTime());

            EditText etDate = (EditText)findViewById(R.id.EtDate2);
            etDate.setText(strContenuDate);

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }


    public void ContenuEditText(){

        // Obtention des contenus des edittests pour les mettre dans des variables.

        AutoCompleteTextView actvClasse2 =(AutoCompleteTextView)findViewById(R.id.ActvClasse2);
        if(bPremiereOuverture){
            actvClasse2.setText(strDerniereClasseRecherchee);
        }

        strContenuClasse = actvClasse2.getText().toString();


        if("".equals(strContenuClasse))
        {bEditTexteVide=true;
            Toast.makeText(Activite_VueSemaine.this,"Veuillez entrer une classe", Toast.LENGTH_LONG).show();
        }else {
            // remplacement des espaces par %20 sinon il ne reconnaissait pas la classe.
            strContenuClasse = strContenuClasse.replace(" ", "%20");
        }



        SimpleDateFormat Formater = new SimpleDateFormat("dd-MM-yyyy");
        Calendar calendar = Calendar.getInstance();
        String strJourActuel = Formater.format(calendar.getTime());


        EditText etDate2 = (EditText)findViewById(R.id.EtDate2);
        strContenuDate = etDate2.getText().toString();

        //Si c'est la première fois qu'on fait la requete ( ouverture de l'application) alors on mets le jour actuel dans la variable contenuDate.

        if(bPremiereOuverture) {
            strContenuDate = strJourActuel;
        }


        if("".equals(strContenuDate)){
            bEditTexteVide=true;
            Toast.makeText(Activite_VueSemaine.this,"Veuillez entrer une date", Toast.LENGTH_LONG).show();
        }else {
            etDate2.setText(strContenuDate);
        }

        bPremiereOuverture = false;

    }



    public void Requete(){


        AsyncAffichageHoraire asyncAffichageHoraire2 = new AsyncAffichageHoraire(Activite_VueSemaine.this, 1);
        asyncAffichageHoraire2.delegate = (AsyncReponse) this;
        asyncAffichageHoraire2.execute("http://devinter.cpln.ch/pdf/hypercool/controler.php?action=ressource&nom=" + strContenuClasse);

        AsyncAffichageHoraire asyncAffichageHoraire = new AsyncAffichageHoraire(Activite_VueSemaine.this, 1);
        asyncAffichageHoraire.delegate = (AsyncReponse) this;
        asyncAffichageHoraire.execute("http://devinter.cpln.ch/pdf/hypercool/controler.php?action=ressource&nom=");


    }
    public void TraitementToutesClasses(String outputlisteclasse){

        JSONObject reader = null;
        String strCode ="";


        AlistLibelleToutesClasses.clear();
        try {
            reader = new JSONObject(outputlisteclasse);
            Iterator iterator = reader.keys();
            for(int i=0;i<329;i++) {
                strCode = (String) iterator.next();
                JSONObject jsonObject = reader.getJSONObject(strCode);
                AlistLibelleToutesClasses.add(i,jsonObject.getString("nom"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(Activite_VueSemaine.this,"Hypercool hors-ligne, impossible de récupérer les données.", Toast.LENGTH_SHORT).show();
        }

        HashSet<String> hashSet = new HashSet<String>();
        hashSet.addAll(AlistLibelleToutesClasses);
        AlistLibelleToutesClasses.clear();
        AlistLibelleToutesClasses.addAll(hashSet);


    }

    public void AffichageHoraire(String output2, String output3, String output4, String output5, String output6, String output7, String output8) {



        //Création des arraylists qui vont contenir toutes les données par thème (prof, salle...)

        ArrayList<String>[] alistGeneralProf = new ArrayList[10];
        for(int i = 0;i<7;i++){
            alistGeneralProf[i] = new ArrayList<>();
        }
        ArrayList<String>[] alistGeneralSalle = new ArrayList[10];
        for(int i= 0;i<7;i++){
            alistGeneralSalle[i] = new ArrayList<>();
        }
        ArrayList<String>[] alistGeneralCodeMatiere = new ArrayList[10];
        for(int i=0;i<7;i++){
            alistGeneralCodeMatiere[i] = new ArrayList<>();
        }
        ArrayList<String>[] alistGeneralLibelle = new ArrayList[10];
        for(int i=0;i<7;i++){
            alistGeneralLibelle[i] = new ArrayList<>();
        }
        ArrayList<String>[] alistGeneralHeureDebutComplet = new ArrayList[10];
        for(int i=0;i<7;i++){
            alistGeneralHeureDebutComplet[i] = new ArrayList<>();
        }
        ArrayList<String>[] alistGeneralHeureFinComplet = new ArrayList[10];
        for(int i=0;i<7;i++){
            alistGeneralHeureFinComplet[i] = new ArrayList<>();
        }
        ArrayList<Integer>[] alistCalcul = new ArrayList[10];
        for(int i=0;i<7;i++){
            alistCalcul[i] = new ArrayList<>();
        }
        ArrayList<Integer>[] alistCalculHeure = new ArrayList[10];
        for(int i=0; i<7;i++){
            alistCalculHeure[i] = new ArrayList<>();
        }


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



        int iHeureDebut = 0;
        int iHeureFin = 0;


        // Le nombre de branches reçues pour une journée

        Integer[] NombreInfoRecu = new Integer[100];




        if("{\"error\":\"la date n'est pas valide\"}".equals(output2)){
            Toast.makeText(Activite_VueSemaine.this,"Veuillez entrer une date valide", Toast.LENGTH_SHORT).show();
            bDateIncorrecte=true;


        }else {


            try {

                for (int iJour = 0; iJour < 6; iJour++) {


                    JSONArray jsonArraygeneral2 = new JSONArray(output[iJour]);
                    for (int iNbBranche = 0; iNbBranche < jsonArraygeneral2.length(); iNbBranche++) {

                        JSONArray jsonArrayGeneral = new JSONArray(output[iJour]);


                        JSONObject jsonObjectGeneral = jsonArrayGeneral.getJSONObject(iNbBranche);
                        arrayLibelle[iJour][iNbBranche] = jsonObjectGeneral.getString("libelle");
                        arrayCodeMatiere[iJour][iNbBranche] = jsonObjectGeneral.getString("codeMatiere");
                        arrayHeureDebut[iJour][iNbBranche] = jsonObjectGeneral.getString("heureDebut");
                        arrayHeureFin[iJour][iNbBranche] = jsonObjectGeneral.getString("heureFin");


                        // Si pas de prof, alors on met un / sinon ca fait crash.
                        try {
                            JSONArray jsonArrayProf = jsonObjectGeneral.getJSONArray("professeur");
                            arrayProf[iJour][iNbBranche] = jsonArrayProf.getString(0);
                        } catch (JSONException e) {
                            arrayProf[iJour][iNbBranche] = "/";
                        }

                        try {
                            JSONArray jsonArraySalle = jsonObjectGeneral.getJSONArray("salle");
                            arraySalle[iJour][iNbBranche] = jsonArraySalle.getString(0);
                        } catch (JSONException e) {
                            arraySalle[iJour][iNbBranche] = "/-";
                        }


                        // On parse les profs pour avoir uniquement le nom.
                        try {
                            arrayProf[iJour][iNbBranche] = arrayProf[iJour][iNbBranche].substring(0, arrayProf[iJour][iNbBranche].indexOf(" "));
                        } catch (Exception e) {

                        }

                        // On parse la salle car le code contient des nombres inutiles (B106B-sdasda)
                        try {
                            arraySalle[iJour][iNbBranche] = arraySalle[iJour][iNbBranche].substring(0, arraySalle[iJour][iNbBranche].indexOf("-"));
                        } catch (Exception e) {

                        }


                        arrayHeureDebutComplet[iJour][iNbBranche] = arrayHeureDebut[iJour][iNbBranche];
                        arrayHeureFinComplet[iJour][iNbBranche] = arrayHeureFin[iJour][iNbBranche];
                        arrayHeureDebut[iJour][iNbBranche] = arrayHeureDebut[iJour][iNbBranche].substring(0, arrayHeureDebut[iJour][iNbBranche].length() - 3);
                        arrayHeureFin[iJour][iNbBranche] = arrayHeureFin[iJour][iNbBranche].substring(0, arrayHeureFin[iJour][iNbBranche].length() - 3);

                        try {
                            iHeureDebut = Integer.parseInt(arrayHeureDebut[iJour][iNbBranche]);
                            iHeureFin = Integer.parseInt(arrayHeureFin[iJour][iNbBranche]);
                            arrayCalcul[iJour][iNbBranche] = iHeureDebut + iHeureFin;
                        } catch (NumberFormatException e) {

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


                // Si on ne recoit aucunes données, alors on met à 0

                for (int i = 0; i < 7; i++) {
                    if (NombreInfoRecu[i] == null || NombreInfoRecu[i].equals("null")) {
                        NombreInfoRecu[i] = 0;
                    }
                }


                // Tri des arraylists en fonction du calcul.

                for (int i = 0; i < 7; i++) {
                    Collections.sort(alistCalcul[i]);
                }


                // Basculement des places en fonction du calcul,

                for (int iJour = 0; iJour < 6; iJour++) {

                    for (int i20 = 0; i20 < NombreInfoRecu[iJour]; i20++) {

                        for (int i10 = 0; i10 < NombreInfoRecu[iJour]; i10++) {

                            if (alistCalculHeure[iJour].get(i10).equals(alistCalcul[iJour].get(i20))) {
                                alistGeneralLibelle[iJour].add(i20, arrayLibelle[iJour][i10]);
                                alistGeneralHeureDebutComplet[iJour].add(i20, arrayHeureDebutComplet[iJour][i10]);
                                alistGeneralHeureFinComplet[iJour].add(i20, arrayHeureFinComplet[iJour][i10]);
                                alistGeneralProf[iJour].add(i20, arrayProf[iJour][i10]);
                                alistGeneralSalle[iJour].add(i20, arraySalle[iJour][i10]);
                                alistGeneralCodeMatiere[iJour].add(i20, arrayCodeMatiere[iJour][i10]);
                            }

                        }


                    }


                }



                // Tableau de toutes les périodes pour pouvoir les utiliser pour savoir le nombre de période.

                String[] arrayPeriodes = {"07h25", "08h10", "08h55", "09h40", "10h45", "11h30", "12h15", "13h10", "13h55", "14h55", "15h40", "16h25", "17h30", "18h15", "19h00", "20h00", "20h45", "21h30", "22h15"};
                String[] arrayJourSemaine = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi"};


                // On va mettre les layouts du xml dans le ll[]

                final LinearLayout[][] llGeneral = new LinearLayout[100][100];

                for (int i2 = 0; i2 < 5; i2++) {
                    for (int i4 = 0; i4 < 19; i4++) {
                        String Idtv = "ll_" + arrayJourSemaine[i2] + "_" + arrayPeriodes[i4];
                        int resID = getResources().getIdentifier(Idtv, "id", getPackageName());
                        llGeneral[i2][i4] = (LinearLayout) findViewById(resID);
                    }
                }


                TextView[][] tvGeneralLibelle = new TextView[200][200];

                for (int i2 = 0; i2 < 5; i2++) {
                    for (int i4 = 0; i4 < 19; i4++) {
                        String Idtv = "tv_" + arrayJourSemaine[i2] + "_" + arrayPeriodes[i4] + "_libelle";
                        int resID = getResources().getIdentifier(Idtv, "id", getPackageName());
                        tvGeneralLibelle[i2][i4] = (TextView) findViewById(resID);
                        tvGeneralLibelle[i2][i4].setTextSize(TypedValue.COMPLEX_UNIT_DIP,13);
                    }
                }

                TextView[][] tvGeneralSalle = new TextView[200][200];

                for (int i2 = 0; i2 < 5; i2++) {
                    for (int i4 = 0; i4 < 19; i4++) {
                        String Idtv = "tv_" + arrayJourSemaine[i2] + "_" + arrayPeriodes[i4] + "_salle";
                        int resID = getResources().getIdentifier(Idtv, "id", getPackageName());
                        tvGeneralSalle[i2][i4] = (TextView) findViewById(resID);
                        tvGeneralSalle[i2][i4].setTextSize(TypedValue.COMPLEX_UNIT_DIP,13);
                    }
                }


                // Vidage de toutes les cases.

                tvGeneralLibelle[4][7].setText("");

                for (int i2 = 0; i2 < 5; i2++) {
                    for (int i = 0; i < 19; i++) {
                        llGeneral[i2][i].setBackgroundColor(Color.TRANSPARENT);
                        llGeneral[i2][i].setBackgroundResource(R.drawable.border);
                        tvGeneralLibelle[i2][i].setBackgroundColor(Color.TRANSPARENT);
                        tvGeneralSalle[i2][i].setBackgroundColor(Color.TRANSPARENT);
                        tvGeneralLibelle[i2][i].setText("");
                        tvGeneralSalle[i2][i].setText("");
                    }
                }



                // Couleur midi a cause de la pause.
                for (int i2 = 0; i2 < 5; i2++) {
                    llGeneral[i2][6].setBackground(getResources().getDrawable(R.drawable.border_midi));
                }


                // Selon l'heure début on va envoyer le numero du layout à la fonction AffichSemaine.

                for(int iJour=0;iJour<5;iJour++){
                    for(int iBranche = 0;iBranche<NombreInfoRecu[iJour];iBranche++){
                        for( int iCpt=0;iCpt<19;iCpt++){
                            if(alistGeneralHeureDebutComplet[iJour].get(iBranche).equals(arrayPeriodes[iCpt])){
                                int iNumeroll=iCpt;
                                String strHeureDebut=arrayPeriodes[iCpt];
                                RemplissageCases(iJour,iBranche,iNumeroll,strHeureDebut,llGeneral,arrayPeriodes,alistGeneralHeureFinComplet,tvGeneralLibelle,tvGeneralSalle,alistGeneralLibelle,alistGeneralSalle,alistGeneralProf,alistGeneralCodeMatiere,arrayCouleurs);

                            }
                        }
                    }
                }


                val1 = 0;
                val2 = 0;
                libelleBranche = new String[libelleBranche.length];

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    public void RemplissageCases(int i200, int i100, int numll, String heureDebut, LinearLayout[][] llGeneral, String[] arrayperiode, ArrayList<String>[] alistheureFin,
                               TextView[][] tvLibelle, TextView[][] tvSalle, ArrayList<String>[] alistLibelle, ArrayList<String>[] alistSalle, ArrayList<String>[] alistProf,
                               ArrayList<String>[] alistCodeMatiere, int[] arrayCouleurs){



        //Utiliser pour la couleur selon les branches.

        if (Arrays.asList(libelleBranche).contains(alistLibelle[i200].get(i100))) {
            val2 = Arrays.asList(libelleBranche).indexOf(alistLibelle[i200].get(i100));
        }else
        {
            libelleBranche[val1] = alistLibelle[i200].get(i100);
            val1++;
            val2 = Arrays.asList(libelleBranche).indexOf(alistLibelle[i200].get(i100));
        }


// AVEC 1 période code matière et séparation après 20 char.
        int iNumeroll = numll;
        int iNombreCharMax = 15;
        // llGeneral[i200][numeroll].setBackgroundColor(color);
        llGeneral[i200][iNumeroll].setBackgroundColor(arrayCouleurs[val2]);


        int iIndex1 = 0;
        int iIndex2 = 0;
        int iIndexFinal = 0;
        iIndex1 = Arrays.asList(arrayperiode).indexOf(heureDebut);
        String strHeureFin = alistheureFin[i200].get(i100);
        iIndex2 = Arrays.asList(arrayperiode).indexOf(strHeureFin);
        iIndexFinal = iIndex2-iIndex1;
       // tvLibelle[i200][numeroll].setTextSize(TypedValue.COMPLEX_UNIT_DIP,12);
        tvLibelle[i200][iNumeroll].setText(alistCodeMatiere[i200].get(i100));
        tvSalle[i200][iNumeroll].setText(alistSalle[i200].get(i100));
        int iLength = alistLibelle[i200].get(i100).length();
        String strPartie1="";
        String strPartie2="";
        if(iLength>iNombreCharMax) {
            strPartie1 = alistLibelle[i200].get(i100).substring(0, iNombreCharMax);
            strPartie2 = alistLibelle[i200].get(i100).substring(iNombreCharMax);
        }

        for(int i20=1;i20<iIndexFinal;i20++){
            //  llGeneral[i200][numeroll + i20].setBackgroundColor(color);
            llGeneral[i200][iNumeroll + i20].setBackgroundColor(arrayCouleurs[val2]);

        }

        if (iIndexFinal==2){
            if(iLength >iNombreCharMax){

                tvLibelle[i200][iNumeroll].setText(strPartie1 + "-");
                tvSalle[i200][iNumeroll].setText(strPartie2);
                tvLibelle[i200][iNumeroll+1].setText(alistSalle[i200].get(i100));
                tvSalle[i200][iNumeroll+1].setText(alistProf[i200].get(i100));
            }
            else {
                tvLibelle[i200][iNumeroll].setText(alistLibelle[i200].get(i100));
                tvLibelle[i200][iNumeroll + 1].setText(alistProf[i200].get(i100));
            }
        }

        if(iIndexFinal==3||iIndexFinal==4){
            if(iLength>iNombreCharMax){
                tvLibelle[i200][iNumeroll].setText("");
                tvSalle[i200][iNumeroll].setText("");
                tvSalle[i200][iNumeroll].setText(strPartie1 + "-");
                tvLibelle[i200][iNumeroll+1].setText(strPartie2);
                tvSalle[i200][iNumeroll+1].setText(alistSalle[i200].get(i100));
                tvLibelle[i200][iNumeroll+2].setText(alistProf[i200].get(i100));

            }else{
            tvLibelle[i200][iNumeroll].setText("");
            tvSalle[i200][iNumeroll].setText("");
            tvLibelle[i200][iNumeroll + 1].setText(alistLibelle[i200].get(i100));
            tvSalle[i200][iNumeroll + 1].setText(alistSalle[i200].get(i100));
            tvLibelle[i200][iNumeroll + 2].setText(alistProf[i200].get(i100));
            }
        }
        if(iIndexFinal==5){
            if(iLength>iNombreCharMax){
                tvLibelle[i200][iNumeroll].setText("");
                tvSalle[i200][iNumeroll].setText("");
                tvSalle[i200][iNumeroll+1].setText(strPartie1 + "-");
                tvLibelle[i200][iNumeroll+2].setText(strPartie2);
                tvSalle[i200][iNumeroll+2].setText(alistSalle[i200].get(i100));
                tvLibelle[i200][iNumeroll+3].setText(alistProf[i200].get(i100));
            }
            else {
                tvLibelle[i200][iNumeroll].setText("");
                tvSalle[i200][iNumeroll].setText("");
                tvLibelle[i200][iNumeroll + 2].setText(alistLibelle[i200].get(i100));
                tvSalle[i200][iNumeroll + 2].setText(alistSalle[i200].get(i100));
                tvLibelle[i200][iNumeroll + 3].setText(alistProf[i200].get(i100));
            }
        }



    }

    // Récupération de l'id

    public String TraitementId(String string){
        /*
        string = string.split(":")[0];
        string = string.substring(0, string.length() -1);
        string = string.substring(2);
        return string; */

/*
        JSONObject reader = null;
        String strCode ="";
        try {
            reader = new JSONObject(string);
            Iterator iterator = reader.keys();
            strCode = (String) iterator.next();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  strCode;*/

        AlistPlusieursClasses.clear();
        iCptNombreClasse = 0;
        JSONObject reader = null;
        String[] arrayCode = new String[1000];
        try {
            reader = new JSONObject(string);
            Iterator iteratorObj = reader.keys();
            while (iteratorObj.hasNext()) {

                arrayCode[iCptNombreClasse] = (String) iteratorObj.next();
                JSONObject jsonObject = reader.getJSONObject(arrayCode[iCptNombreClasse]);
                AlistPlusieursClasses.add(iCptNombreClasse, jsonObject.getString("nom"));
                iCptNombreClasse++;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return arrayCode[0];



    }

    // Fonction pour avoir toutes les dates de la semaine en fonction de la date envoyée.

    public void DateEnSemaine(String date) {


        SimpleDateFormat Formater = new SimpleDateFormat("dd-MM-yyyy");
        Date ObjetDate = null;
        try {
            ObjetDate = Formater.parse(date);

        } catch (ParseException e) {
            e.printStackTrace();

            bDateIncorrecte=true;
            Toast.makeText(Activite_VueSemaine.this,"Veuillez entrer une date valide", Toast.LENGTH_LONG).show();
        }

        Calendar calendrier = Calendar.getInstance();
        try{
            calendrier.setTime(ObjetDate);}catch (Exception e){}
        calendrier.add(Calendar.DAY_OF_MONTH,Calendar.MONDAY - calendrier.get(Calendar.DAY_OF_WEEK));

        String strDatelundi = Formater.format(calendrier.getTime());


        for(int i = 0; i<7;i++) {
            AlistDateSemaine.add(i, strDatelundi);
            calendrier.add(Calendar.DATE, 1);
            strDatelundi = Formater.format(calendrier.getTime());
        }




    }


    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu2, menu);
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

        File chemin = getBaseContext().getFilesDir();
        File fichier = new File(chemin, "storage.txt");
        try {
            PrintWriter pw = new PrintWriter(fichier);
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        AlistHistorique.clear();
        LectureFichier();
        MiseEnPlaceActv(AlistLibelleToutesClasses);
        bAlternateur=true;

        Button btnAlternateur2 = (Button)findViewById(R.id.BtnAlternateur2);
        String strTexteAlternateur = btnAlternateur2.getText().toString();
        if("Classes".equals(strTexteAlternateur)){btnAlternateur2.setText("Historique");}




    }
    public void EcritureFichier(){
        AutoCompleteTextView actvClasse2 = (AutoCompleteTextView)findViewById(R.id.ActvClasse2);
        String string = actvClasse2.getText().toString();
        string = string.toUpperCase();

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


        //Fichier derniere classe.

        File fichierDerniereClasse = new File(chemin, "DerniereClasse.txt");
        try {
            PrintWriter pw = new PrintWriter(fichierDerniereClasse);
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Writer writer2;
        try{
            writer2 = new BufferedWriter(new FileWriter(fichierDerniereClasse, true));
            writer2.append(string);
            writer2.close();
        }catch(IOException e){

        }



    }


    public String getDerniereClasseRecherchee(){

        File chemin = getBaseContext().getFilesDir();
        String strLigne ="";
        File fichierDerniereClasse = new File(chemin, "DerniereClasse.txt");

        try{
            BufferedReader input = new BufferedReader(new FileReader(fichierDerniereClasse));
            while ((strLigne = input.readLine()) != null) {
                strDerniereClasseRecherchee = strLigne;
            }
            input.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return  strDerniereClasseRecherchee;

    }

    public void LectureFichier(){

        File chemin = getBaseContext().getFilesDir();
        File fichier = new File(chemin, "storage.txt");
        String strLigne ="";

        try{
            BufferedReader input = new BufferedReader(new FileReader(fichier));
            while ((strLigne = input.readLine()) != null) {
                int i= 0;
                AlistHistorique.add(i, strLigne);
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
