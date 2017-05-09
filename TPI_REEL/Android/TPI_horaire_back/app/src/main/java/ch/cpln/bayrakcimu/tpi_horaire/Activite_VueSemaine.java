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
import java.util.StringTokenizer;
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
    int iNouvelleBranche=0;
    int iNumCouleur=0;


    boolean bAppelDepuisBoutonPlusMoins=false;


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

                        int  iJour = JourChoisi;
                        int iMois = MoisChoisi;
                        int iAnnee = AnneeChoisie;

                        String strDateComplet = String.valueOf(iJour) + "-" + String.valueOf(iMois + 1) + "-" + String.valueOf(iAnnee);
                        EditText etDate2 = (EditText)findViewById(R.id.EtDate2);
                        etDate2.setText(strDateComplet);

                    }
                },iAnneeActuelle, iMoisActuel, iJourActuel);
                mDatePicker.setTitle("Choisir la date");
                mDatePicker.show();
            }

        });

//Obligation de passer en orientation paysage
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


        // on n'affiche pas le clavier lors de la première fois.
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );



        strDerniereClasseRecherchee = getDerniereClasseRecherchee();

        // Si dans le pire des cas strDerniereClasseRecherchee et vide (meme si ca ne devrait pas arriver) alors on met une classe arbitrairement pour ne pas faire crash l'application.
        if(strDerniereClasseRecherchee==null || "".equals(strDerniereClasseRecherchee)) {
            strDerniereClasseRecherchee = "3m3i2";
        }

        ContenuEditText();
        DateEnSemaine(strContenuDate);
        Requete();
       // EcritureFichier();
      //  LectureFichier();
      //  MiseEnPlaceActv(AlistHistorique);



        Button btnPlusSemaine2 = (Button)findViewById(R.id.BtnPlusSemaine2);
        btnPlusSemaine2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!bPlusieursClasseValidees) {
                    if (!bEditTexteVide) {
                        bPlusieursClasseValidees = true;
                        bAppelDepuisBoutonPlusMoins = true;
                        iChoixTraitementOutput = 0;
                        if (!bDateIncorrecte) {
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
                if(!bPlusieursClasseValidees) {
                    if (!bEditTexteVide) {
                        bPlusieursClasseValidees = true;
                        bAppelDepuisBoutonPlusMoins = true;
                        iChoixTraitementOutput = 0;
                        if (!bDateIncorrecte) {
                            TraitementDate(strContenuDate, -7);
                            DateEnSemaine(strContenuDate);
                            Requete();
                        }
                    }
                }
            }
        });


        Button btnRechercher2 = (Button)findViewById(R.id.BtnRechercher2);
        btnRechercher2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bDateIncorrecte=false;
                bEditTexteVide=false;
                bAppelDepuisBoutonPlusMoins=false;
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



        final AutoCompleteTextView actvClasse2 = (AutoCompleteTextView)findViewById(R.id.ActvClasse2);
        actvClasse2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bAfficherContenuActv){
                    actvClasse2.showDropDown();
                    CacherClavier(getBaseContext(), actvClasse2);
                }
                bAfficherContenuActv=false;

                if(bPlusieursClasseValidees) {
                    CacherClavier(getBaseContext(), actvClasse2);
                    bAfficherContenuActv=true;

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
                    actvClasse2.performClick();

                }else{

                    MiseEnPlaceActv(AlistLibelleToutesClasses);
                    Toast.makeText(Activite_VueSemaine.this,"Affichage toutes les classes", Toast.LENGTH_SHORT).show();
                    btnAlternateur2.setText("Historique");
                }
                bAlternateur = !bAlternateur;
            }
        });



        // Mise en place du swipe, code pareil que les bouton --, ++

        LinearLayout llPrincipal2 = (LinearLayout)findViewById(R.id.LlPricipal2);
        llPrincipal2.setOnTouchListener(new SwipeListener(Activite_VueSemaine.this) {

            public void onSwipeRight() {

                if(!bPlusieursClasseValidees) {
                    if (!bEditTexteVide) {
                        bPlusieursClasseValidees = true;
                        bAppelDepuisBoutonPlusMoins = true;
                        iChoixTraitementOutput = 0;
                        if (!bDateIncorrecte) {
                            TraitementDate(strContenuDate, -7);
                            DateEnSemaine(strContenuDate);
                            Requete();
                        }
                    }
                }

            }
            public void onSwipeLeft() {

                if(!bPlusieursClasseValidees){
                    if(!bEditTexteVide){
                        bPlusieursClasseValidees=true;
                        bAppelDepuisBoutonPlusMoins=true;
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


    }

// Fonction pour cacher le clavier
    public static void CacherClavier(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        inputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    public void RetourOutput(String strOutput){

        //avec iChoixTraitementOutput, on va aiguiller le resultat.
        if(iChoixTraitementOutput==0){
            strId = TraitementId(strOutput);

            if(iCptNombreClasse>1 && !bPlusieursClasseValidees) {
                Toast.makeText(Activite_VueSemaine.this, iCptNombreClasse + " classes trouvées, veuillez choisir la bonne classe", Toast.LENGTH_LONG).show();
                bAfficherContenuActv = true;
                MiseEnPlaceActv(AlistPlusieursClasses);
                bPlusieursClasseValidees = true;

                AutoCompleteTextView actvClasse2 = (AutoCompleteTextView) findViewById(R.id.ActvClasse2);
                actvClasse2.performClick();

            }
            else {
                bAfficherContenuActv=false;  // permet de remettre le clavier.
                bPlusieursClasseValidees = false;

                if ("".equals(strId) || strId == null) {
                    Toast.makeText(Activite_VueSemaine.this, "Classe non trouvée", Toast.LENGTH_SHORT).show();
                    bEditTexteVide = true;
                } else {

                    // Si tout est bon, lancement des requêtes.
                    AsyncAffichageHoraire[] asyncGeneral = new AsyncAffichageHoraire[8];
                    for (int i = 0; i < 5; i++) {
                        asyncGeneral[i] = new AsyncAffichageHoraire(Activite_VueSemaine.this, 0);
                        asyncGeneral[i].delegate = (AsyncReponse) this;
                        asyncGeneral[i].execute("http://devinter.cpln.ch/pdf/hypercool/controler.php?action=horaire&ident=" + strId + "&sub=date&date=" + AlistDateSemaine.get(i));

                    }

                    if (!bAppelDepuisBoutonPlusMoins) {
                        EcritureFichier();
                    }
                    bAppelDepuisBoutonPlusMoins = false;
                    LectureFichier();
                    if (!bAlternateur) {
                        MiseEnPlaceActv(AlistHistorique);
                    } else {
                        MiseEnPlaceActv(AlistLibelleToutesClasses);
                    }


                }
            }
        }
        if(iChoixTraitementOutput==1){
            TraitementToutesClasses(strOutput);
        }

        // On remplit le tableau
        if(iChoixTraitementOutput==2 ||iChoixTraitementOutput==3 ||iChoixTraitementOutput==4 ||iChoixTraitementOutput==5 ||iChoixTraitementOutput==6){
            Outputs[iChoixTraitementOutput-2] = strOutput;
        }

        // Une fois que toute les requêtes ont été effectuées, on fait appel à la fonction AffichageHoraire.
        if(iChoixTraitementOutput==6) {
            AffichageHoraire(Outputs[0], Outputs[1], Outputs[2], Outputs[3], Outputs[4]);
            TextView tvJourDeLaSemaine2 = (TextView)findViewById(R.id.TvJourDeLaSemaine2);
            if(!bDateIncorrecte) {
                tvJourDeLaSemaine2.setText("Lundi " + AlistDateSemaine.get(0) + " au Vendredi " + AlistDateSemaine.get(6));
            }
        }

        iChoixTraitementOutput++;
    }


    // Fonction pour mettre en place et mettre à jour l'actv.
    public void MiseEnPlaceActv(ArrayList<String> Alist){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, Alist);
        AutoCompleteTextView actvClasse2 = (AutoCompleteTextView)
                findViewById(R.id.ActvClasse2);
        actvClasse2.setThreshold(0);
        actvClasse2.setAdapter(adapter);
    }

    public void TraitementDate(String strContenudate, int iJourAModifier){


        // Fonction permettant d'ajouter des semaines à la date actuelle

        Date ObjetDate = null;
        try {
            SimpleDateFormat Formater = new SimpleDateFormat("dd-MM-yyyy");
            ObjetDate = Formater.parse(strContenudate);
            Calendar calendrier = Calendar.getInstance();
            calendrier.setTime(ObjetDate);
            calendrier.add(Calendar.DATE,iJourAModifier);
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


        if("".equals(strContenuClasse)) {
            bEditTexteVide = true;
            Toast.makeText(Activite_VueSemaine.this, "Veuillez entrer une classe", Toast.LENGTH_LONG).show();
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
    public void TraitementToutesClasses(String strOutputlisteclasse){

        JSONObject reader = null;
        String strCode ="";


        AlistLibelleToutesClasses.clear();
        try {
            reader = new JSONObject(strOutputlisteclasse);
            Iterator iterator = reader.keys();
            for(int i=0;i<329;i++) {
                strCode = (String) iterator.next();
                JSONObject jsonObject = reader.getJSONObject(strCode);
                AlistLibelleToutesClasses.add(i,jsonObject.getString("nom"));
            }

        } catch (JSONException e) {
            e.printStackTrace();

            // Si cette requête ne renvoie rien, alors hypercool est hors ligne.
            Toast.makeText(Activite_VueSemaine.this,"Hypercool hors-ligne, impossible de récupérer les données.", Toast.LENGTH_SHORT).show();
        }

        HashSet<String> hashSet = new HashSet<String>();
        hashSet.addAll(AlistLibelleToutesClasses);
        AlistLibelleToutesClasses.clear();
        AlistLibelleToutesClasses.addAll(hashSet);


    }

    public void AffichageHoraire(String strOutputJ1, String strOutputJ2, String strOutputJ3, String strOutputJ4, String strOutputJ5) {



        //Création des arraylists qui vont contenir toutes les données par thème (prof, salle...)

        ArrayList<String>[] alistGeneralProf = new ArrayList[10];
        for(int i = 0;i<5;i++){
            alistGeneralProf[i] = new ArrayList<>();
        }
        ArrayList<String>[] alistGeneralSalle = new ArrayList[10];
        for(int i= 0;i<5;i++){
            alistGeneralSalle[i] = new ArrayList<>();
        }
        ArrayList<String>[] alistGeneralCodeMatiere = new ArrayList[10];
        for(int i=0;i<5;i++){
            alistGeneralCodeMatiere[i] = new ArrayList<>();
        }
        ArrayList<String>[] alistGeneralLibelle = new ArrayList[10];
        for(int i=0;i<5;i++){
            alistGeneralLibelle[i] = new ArrayList<>();
        }
        ArrayList<String>[] alistGeneralHeureDebutComplet = new ArrayList[10];
        for(int i=0;i<5;i++){
            alistGeneralHeureDebutComplet[i] = new ArrayList<>();
        }
        ArrayList<String>[] alistGeneralHeureFinComplet = new ArrayList[10];
        for(int i=0;i<5;i++){
            alistGeneralHeureFinComplet[i] = new ArrayList<>();
        }
        ArrayList<Integer>[] alistCalcul = new ArrayList[10];
        for(int i=0;i<5;i++){
            alistCalcul[i] = new ArrayList<>();
        }
        ArrayList<Integer>[] alistCalculHeure = new ArrayList[10];
        for(int i=0; i<5;i++){
            alistCalculHeure[i] = new ArrayList<>();
        }


        // Création du tableau de couleur pour les branches.

        int[] arrayCouleurs = new int[100];
        arrayCouleurs[0] = Color.rgb(240,236,103);
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
        arrayCouleurs[20] = Color.rgb(0,200,43);
        arrayCouleurs[21] = Color.rgb(213,255,0);




        String[] output = {strOutputJ1,strOutputJ2,strOutputJ3,strOutputJ4,strOutputJ5};

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




        if("{\"error\":\"la date n'est pas valide\"}".equals(strOutputJ1)){
            Toast.makeText(Activite_VueSemaine.this,"Veuillez entrer une date valide", Toast.LENGTH_SHORT).show();
            bDateIncorrecte=true;


        }else {


            try {

                for (int iJour = 0; iJour < 5; iJour++) {


                    JSONArray jsonArraygeneral2 = new JSONArray(output[iJour]);
                    for (int iNbBranche = 0; iNbBranche < jsonArraygeneral2.length(); iNbBranche++) {

                        JSONArray jsonArrayGeneral = new JSONArray(output[iJour]);


                        JSONObject jsonObjectGeneral = jsonArrayGeneral.getJSONObject(iNbBranche);
                        arrayLibelle[iJour][iNbBranche] = jsonObjectGeneral.getString("libelle");
                        arrayLibelle[iJour][iNbBranche] = arrayLibelle[iJour][iNbBranche].substring(0, 1).toUpperCase() + arrayLibelle[iJour][iNbBranche].substring(1).toLowerCase();
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

                for (int i = 0; i < 5; i++) {
                    if (NombreInfoRecu[i] == null || NombreInfoRecu[i].equals("null")) {
                        NombreInfoRecu[i] = 0;
                    }
                }


                // Tri des arraylists en fonction du calcul.

                for (int i = 0; i < 5; i++) {
                    Collections.sort(alistCalcul[i]);
                }


                // Basculement des places en fonction du calcul,

                for (int iJour = 0; iJour < 5; iJour++) {
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

                for (int i = 0; i < 5; i++) {
                    for (int i2 = 0; i2 < 19; i2++) {
                        String Idtv = "ll_" + arrayJourSemaine[i] + "_" + arrayPeriodes[i2];
                        int resID = getResources().getIdentifier(Idtv, "id", getPackageName());
                        llGeneral[i][i2] = (LinearLayout) findViewById(resID);
                    }
                }


                TextView[][] tvGeneralLibelle = new TextView[200][200];
                for (int i = 0; i < 5; i++) {
                    for (int i2 = 0; i2 < 19; i2++) {
                        String Idtv = "tv_" + arrayJourSemaine[i] + "_" + arrayPeriodes[i2] + "_libelle";
                        int resID = getResources().getIdentifier(Idtv, "id", getPackageName());
                        tvGeneralLibelle[i][i2] = (TextView) findViewById(resID);
                        tvGeneralLibelle[i][i2].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
                    }
                }

                TextView[][] tvGeneralSalle = new TextView[200][200];

                for (int i = 0; i < 5; i++) {
                    for (int i2 = 0; i2 < 19; i2++) {
                        String Idtv = "tv_" + arrayJourSemaine[i] + "_" + arrayPeriodes[i2] + "_salle";
                        int resID = getResources().getIdentifier(Idtv, "id", getPackageName());
                        tvGeneralSalle[i][i2] = (TextView) findViewById(resID);
                        tvGeneralSalle[i][i2].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
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

                for (int iJour = 0; iJour < 5; iJour++) {
                    for (int iBranche = 0; iBranche < NombreInfoRecu[iJour]; iBranche++) {
                        for (int iCpt = 0; iCpt < 19; iCpt++) {
                            if (alistGeneralHeureDebutComplet[iJour].get(iBranche).equals(arrayPeriodes[iCpt])) {
                                int iNumeroll = iCpt;
                                String strHeureDebut = arrayPeriodes[iCpt];
                                RemplissageCases(iJour, iBranche, iNumeroll, strHeureDebut, llGeneral, arrayPeriodes, alistGeneralHeureFinComplet, tvGeneralLibelle, tvGeneralSalle, alistGeneralLibelle, alistGeneralSalle, alistGeneralProf, alistGeneralCodeMatiere, arrayCouleurs);
                            }
                        }
                    }
                }



            //traitement des périodes qui commences à des heures non conventionelles.


                String[][] arrayPeriodeEntre = new String[100][100];

                arrayPeriodeEntre[0][0] = "07h30";
                arrayPeriodeEntre[0][1] = "07h35";
                arrayPeriodeEntre[0][2] = "07h40";
                arrayPeriodeEntre[0][3] = "07h45";
                arrayPeriodeEntre[0][4] = "07h50";
                arrayPeriodeEntre[0][5] = "07h55";
                arrayPeriodeEntre[0][6] = "08h00";
                arrayPeriodeEntre[0][7] = "08h05";

                arrayPeriodeEntre[1][0] = "08h15";
                arrayPeriodeEntre[1][1] = "08h20";
                arrayPeriodeEntre[1][2] = "08h25";
                arrayPeriodeEntre[1][3] = "08h30";
                arrayPeriodeEntre[1][4] = "08h35";
                arrayPeriodeEntre[1][5] = "08h40";
                arrayPeriodeEntre[1][6] = "08h45";
                arrayPeriodeEntre[1][7] = "08h50";

                arrayPeriodeEntre[2][0] = "09h00";
                arrayPeriodeEntre[2][1] = "09h05";
                arrayPeriodeEntre[2][2] = "09h10";
                arrayPeriodeEntre[2][3] = "09h15";
                arrayPeriodeEntre[2][4] = "09h20";
                arrayPeriodeEntre[2][5] = "09h25";
                arrayPeriodeEntre[2][6] = "09h30";
                arrayPeriodeEntre[2][7] = "09h35";

                arrayPeriodeEntre[3][0] = "09h45";
                arrayPeriodeEntre[3][1] = "09h50";
                arrayPeriodeEntre[3][2] = "09h55";
                arrayPeriodeEntre[3][3] = "10h00";
                arrayPeriodeEntre[3][4] = "10h05";
                arrayPeriodeEntre[3][5] = "10h10";
                arrayPeriodeEntre[3][6] = "10h15";
                arrayPeriodeEntre[3][7] = "10h20";

                arrayPeriodeEntre[4][0] = "10h50";
                arrayPeriodeEntre[4][1] = "10h55";
                arrayPeriodeEntre[4][2] = "11h00";
                arrayPeriodeEntre[4][3] = "11h05";
                arrayPeriodeEntre[4][4] = "11h10";
                arrayPeriodeEntre[4][5] = "11h15";
                arrayPeriodeEntre[4][6] = "11h20";
                arrayPeriodeEntre[4][7] = "11h25";

                arrayPeriodeEntre[5][0] = "11h35";
                arrayPeriodeEntre[5][1] = "11h40";
                arrayPeriodeEntre[5][2] = "11h45";
                arrayPeriodeEntre[5][3] = "11h50";
                arrayPeriodeEntre[5][4] = "11h55";
                arrayPeriodeEntre[5][5] = "12h00";
                arrayPeriodeEntre[5][6] = "12h05";
                arrayPeriodeEntre[5][7] = "12h10";

                arrayPeriodeEntre[6][0] = "12h20";
                arrayPeriodeEntre[6][1] = "12h25";
                arrayPeriodeEntre[6][2] = "12h30";
                arrayPeriodeEntre[6][3] = "12h35";
                arrayPeriodeEntre[6][4] = "12h40";
                arrayPeriodeEntre[6][5] = "12h45";
                arrayPeriodeEntre[6][6] = "12h50";
                arrayPeriodeEntre[6][7] = "12h55";

                arrayPeriodeEntre[7][0] = "13h15";
                arrayPeriodeEntre[7][1] = "13h20";
                arrayPeriodeEntre[7][2] = "13h25";
                arrayPeriodeEntre[7][3] = "13h30";
                arrayPeriodeEntre[7][4] = "13h35";
                arrayPeriodeEntre[7][5] = "13h40";
                arrayPeriodeEntre[7][6] = "13h45";
                arrayPeriodeEntre[7][7] = "13h50";

                arrayPeriodeEntre[8][0] = "14h00";
                arrayPeriodeEntre[8][1] = "14h05";
                arrayPeriodeEntre[8][2] = "14h10";
                arrayPeriodeEntre[8][3] = "14h15";
                arrayPeriodeEntre[8][4] = "14h20";
                arrayPeriodeEntre[8][5] = "14h25";
                arrayPeriodeEntre[8][6] = "14h30";
                arrayPeriodeEntre[8][7] = "14h35";

                arrayPeriodeEntre[9][0] = "15h00";
                arrayPeriodeEntre[9][1] = "15h05";
                arrayPeriodeEntre[9][2] = "15h10";
                arrayPeriodeEntre[9][3] = "15h15";
                arrayPeriodeEntre[9][4] = "15h20";
                arrayPeriodeEntre[9][5] = "15h25";
                arrayPeriodeEntre[9][6] = "15h30";
                arrayPeriodeEntre[9][7] = "15h35";

                arrayPeriodeEntre[10][0] = "15h45";
                arrayPeriodeEntre[10][1] = "15h50";
                arrayPeriodeEntre[10][2] = "15h55";
                arrayPeriodeEntre[10][3] = "16h00";
                arrayPeriodeEntre[10][4] = "16h05";
                arrayPeriodeEntre[10][5] = "16h10";
                arrayPeriodeEntre[10][6] = "16h15";
                arrayPeriodeEntre[10][7] = "16h20";

                arrayPeriodeEntre[11][0] = "16h30";
                arrayPeriodeEntre[11][1] = "16h35";
                arrayPeriodeEntre[11][2] = "16h40";
                arrayPeriodeEntre[11][3] = "16h45";
                arrayPeriodeEntre[11][4] = "16h50";
                arrayPeriodeEntre[11][5] = "16h55";
                arrayPeriodeEntre[11][6] = "17h00";
                arrayPeriodeEntre[11][7] = "17h05";

                arrayPeriodeEntre[12][0] = "17h35";
                arrayPeriodeEntre[12][1] = "17h40";
                arrayPeriodeEntre[12][2] = "17h45";
                arrayPeriodeEntre[12][3] = "17h50";
                arrayPeriodeEntre[12][4] = "17h55";
                arrayPeriodeEntre[12][5] = "18h00";
                arrayPeriodeEntre[12][6] = "18h05";
                arrayPeriodeEntre[12][7] = "18h10";

                arrayPeriodeEntre[13][0] = "18h20";
                arrayPeriodeEntre[13][1] = "18h25";
                arrayPeriodeEntre[13][2] = "18h30";
                arrayPeriodeEntre[13][3] = "18h35";
                arrayPeriodeEntre[13][4] = "18h40";
                arrayPeriodeEntre[13][5] = "18h45";
                arrayPeriodeEntre[13][6] = "18h50";
                arrayPeriodeEntre[13][7] = "18h55";



                // on va tester si l'heure de début est la même, tout d'abord les variations 08h15... puis la layout, 09h00, puis le jour.

                for(int iJour=0;iJour<5;iJour++) {
                    for (int iBranche = 0; iBranche < NombreInfoRecu[iJour]; iBranche++) {
                        for (int i2 = 0; i2 < 19; i2++) {   // le nombre de périodes au total.
                            for (int i = 0; i < 8; i++) { // 8 = le nombre de la seconde dimension du tableau.
                                if(alistGeneralHeureDebutComplet[iJour].get(iBranche).equals(arrayPeriodeEntre[i2][i])){
                                    int iNumeroll = i2;
                                    RemplissageCases2(iJour, iBranche, iNumeroll,tvGeneralLibelle,tvGeneralSalle,alistGeneralLibelle,alistGeneralSalle,alistGeneralCodeMatiere,arrayCouleurs);
                                }
                            }
                        }
                    }
                }


                iNouvelleBranche = 0;
                iNumCouleur = 0;
                libelleBranche = new String[libelleBranche.length];

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    public void RemplissageCases(int iJour, int iBranche, int iNumll, String strHeureDebut, LinearLayout[][] llGeneral, String[] arrayPeriode, ArrayList<String>[] AlistHeureFin,
                               TextView[][] tvLibelle, TextView[][] tvSalle, ArrayList<String>[] AlistLibelle, ArrayList<String>[] AlistSalle, ArrayList<String>[] AlistProf,
                               ArrayList<String>[] AlistCodeMatiere, int[] arrayCouleurs) {


        //Utiliser pour la couleur selon les branches.

        if (Arrays.asList(libelleBranche).contains(AlistLibelle[iJour].get(iBranche))) {
            iNumCouleur = Arrays.asList(libelleBranche).indexOf(AlistLibelle[iJour].get(iBranche));
        } else {
            libelleBranche[iNouvelleBranche] = AlistLibelle[iJour].get(iBranche);
            iNouvelleBranche++;
            iNumCouleur = Arrays.asList(libelleBranche).indexOf(AlistLibelle[iJour].get(iBranche));
        }


        int iNumeroll = iNumll;
        int iNombreCharMax = 15;   // APrès ce nombre de caractère, on va coupé le libelle.
        llGeneral[iJour][iNumeroll].setBackgroundColor(arrayCouleurs[iNumCouleur]);
        int iIndex1 = 0;
        int iIndex2 = 0;
        int iIndexFinal = 0;
        iIndex1 = Arrays.asList(arrayPeriode).indexOf(strHeureDebut);
        String strHeureFin = AlistHeureFin[iJour].get(iBranche);
        iIndex2 = Arrays.asList(arrayPeriode).indexOf(strHeureFin);
        iIndexFinal = iIndex2 - iIndex1;
        tvLibelle[iJour][iNumeroll].setText(AlistCodeMatiere[iJour].get(iBranche));
        tvSalle[iJour][iNumeroll].setText(AlistSalle[iJour].get(iBranche));
        int iLength = AlistLibelle[iJour].get(iBranche).length();
        String strPartie1 = "";
        String strPartie2 = "";
        if (iLength > iNombreCharMax) {
            strPartie1 = AlistLibelle[iJour].get(iBranche).substring(0, iNombreCharMax);
            strPartie2 = AlistLibelle[iJour].get(iBranche).substring(iNombreCharMax);
        }

        for (int i = 1; i < iIndexFinal; i++) {
            llGeneral[iJour][iNumeroll + i].setBackgroundColor(arrayCouleurs[iNumCouleur]);

        }

        if (iIndexFinal == 2) {
            if (iLength > iNombreCharMax) {

                tvLibelle[iJour][iNumeroll].setText(strPartie1 + "-");
                tvSalle[iJour][iNumeroll].setText(strPartie2);
                tvLibelle[iJour][iNumeroll + 1].setText(AlistSalle[iJour].get(iBranche));
                tvSalle[iJour][iNumeroll + 1].setText(AlistProf[iJour].get(iBranche));
            } else {
                tvLibelle[iJour][iNumeroll].setText(AlistLibelle[iJour].get(iBranche));
                tvLibelle[iJour][iNumeroll + 1].setText(AlistProf[iJour].get(iBranche));
            }
        }

        if (iIndexFinal == 3 || iIndexFinal == 4) {
            if (iLength > iNombreCharMax) {
                tvLibelle[iJour][iNumeroll].setText("");
                tvSalle[iJour][iNumeroll].setText("");
                tvSalle[iJour][iNumeroll].setText(strPartie1 + "-");
                tvLibelle[iJour][iNumeroll + 1].setText(strPartie2);
                tvSalle[iJour][iNumeroll + 1].setText(AlistSalle[iJour].get(iBranche));
                tvLibelle[iJour][iNumeroll + 2].setText(AlistProf[iJour].get(iBranche));

            } else {
                tvLibelle[iJour][iNumeroll].setText("");
                tvSalle[iJour][iNumeroll].setText("");
                tvLibelle[iJour][iNumeroll + 1].setText(AlistLibelle[iJour].get(iBranche));
                tvSalle[iJour][iNumeroll + 1].setText(AlistSalle[iJour].get(iBranche));
                tvLibelle[iJour][iNumeroll + 2].setText(AlistProf[iJour].get(iBranche));
            }
        }
        if (iIndexFinal == 5) {
            if (iLength > iNombreCharMax) {
                tvLibelle[iJour][iNumeroll].setText("");
                tvSalle[iJour][iNumeroll].setText("");
                tvSalle[iJour][iNumeroll + 1].setText(strPartie1 + "-");
                tvLibelle[iJour][iNumeroll + 2].setText(strPartie2);
                tvSalle[iJour][iNumeroll + 2].setText(AlistSalle[iJour].get(iBranche));
                tvLibelle[iJour][iNumeroll + 3].setText(AlistProf[iJour].get(iBranche));
            } else {
                tvLibelle[iJour][iNumeroll].setText("");
                tvSalle[iJour][iNumeroll].setText("");
                tvLibelle[iJour][iNumeroll + 2].setText(AlistLibelle[iJour].get(iBranche));
                tvSalle[iJour][iNumeroll + 2].setText(AlistSalle[iJour].get(iBranche));
                tvLibelle[iJour][iNumeroll + 3].setText(AlistProf[iJour].get(iBranche));
            }
        }


    }

//Fonction pour les périodes aux heures non conventionnelles.
    public void RemplissageCases2(int iJour, int iBranche, int iNumll,
                                 TextView[][] tvLibelle, TextView[][] tvSalle, ArrayList<String>[] AlistLibelle, ArrayList<String>[] AlistSalle,
                                 ArrayList<String>[] AlistCodeMatiere, int[] arrayCouleurs) {

        if (Arrays.asList(libelleBranche).contains(AlistLibelle[iJour].get(iBranche))) {
            iNumCouleur = Arrays.asList(libelleBranche).indexOf(AlistLibelle[iJour].get(iBranche));
        } else {
            libelleBranche[iNouvelleBranche] = AlistLibelle[iJour].get(iBranche);
            iNouvelleBranche++;
            iNumCouleur = Arrays.asList(libelleBranche).indexOf(AlistLibelle[iJour].get(iBranche));
        }

        tvSalle[iJour][iNumll].setText(AlistCodeMatiere[iJour].get(iBranche));
        tvLibelle[iJour][iNumll+1].setText(AlistSalle[iJour].get(iBranche));
        tvSalle[iJour][iNumll].setBackgroundColor(arrayCouleurs[iNumCouleur]);
        tvLibelle[iJour][iNumll+1].setBackgroundColor(arrayCouleurs[iNumCouleur]);

    }


    // Récupération de l'id

    public String TraitementId(String strOutput) {

        AlistPlusieursClasses.clear();
        iCptNombreClasse = 0;
        JSONObject reader = null;
        String[] arrayCode = new String[1000];
        try {
            reader = new JSONObject(strOutput);
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

    public void DateEnSemaine(String strDateatraiter) {


        SimpleDateFormat Formater = new SimpleDateFormat("dd-MM-yyyy");
        Date ObjetDate = null;
        try {
            ObjetDate = Formater.parse(strDateatraiter);

        } catch (ParseException e) {
            e.printStackTrace();

            bDateIncorrecte = true;
            Toast.makeText(Activite_VueSemaine.this, "Veuillez entrer une date valide", Toast.LENGTH_LONG).show();
        }

        Calendar calendrier = Calendar.getInstance();
        try {
            calendrier.setTime(ObjetDate);
        } catch (Exception e) {
        }
        calendrier.add(Calendar.DAY_OF_MONTH, Calendar.MONDAY - calendrier.get(Calendar.DAY_OF_WEEK));

        String strDatelundi = Formater.format(calendrier.getTime());


        for (int i = 0; i < 7; i++) {
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
    public void EffacerRecherches() {

        File fChemin = getBaseContext().getFilesDir();
        File fFichier = new File(fChemin, "storage.txt");
        try {
            PrintWriter pw = new PrintWriter(fFichier);
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        AlistHistorique.clear();
        LectureFichier();
        MiseEnPlaceActv(AlistLibelleToutesClasses);
        bAlternateur = true;

        Button btnAlternateur2 = (Button) findViewById(R.id.BtnAlternateur2);
        String strTexteAlternateur = btnAlternateur2.getText().toString();
        if ("Classes".equals(strTexteAlternateur)) {
            btnAlternateur2.setText("Historique");
        }


    }
    public void EcritureFichier() {
        AutoCompleteTextView actvClasse2 = (AutoCompleteTextView) findViewById(R.id.ActvClasse2);
        String strChampsClasse2 = actvClasse2.getText().toString();
        strChampsClasse2 = strChampsClasse2.toUpperCase();

        File fChemin = getBaseContext().getFilesDir();
        File fFichier = new File(fChemin, "storage.txt");
        Writer writer;

        if (!"".equals(strChampsClasse2)) {
            try {
                writer = new BufferedWriter(new FileWriter(fFichier, true));
                writer.append(strChampsClasse2 + "\n");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Fichier derniere classe.

        File fFichierDerniereClasse = new File(fChemin, "DerniereClasse.txt");

        if (!"".equals(strChampsClasse2)) {
            try {
                PrintWriter pw = new PrintWriter(fFichierDerniereClasse);
                pw.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Writer writer2;


            try {
                writer2 = new BufferedWriter(new FileWriter(fFichierDerniereClasse, true));
                writer2.append(strChampsClasse2);
                writer2.close();
            } catch (IOException e) {

            }
        }


    }


    public String getDerniereClasseRecherchee() {

        File fChemin = getBaseContext().getFilesDir();
        String strLigne = "";
        File fFichierDerniereClasse = new File(fChemin, "DerniereClasse.txt");

        try {
            BufferedReader input = new BufferedReader(new FileReader(fFichierDerniereClasse));
            while ((strLigne = input.readLine()) != null) {
                strDerniereClasseRecherchee = strLigne;
            }
            input.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return strDerniereClasseRecherchee;

    }

    public void LectureFichier() {

        File fChemin = getBaseContext().getFilesDir();
        File fFichier = new File(fChemin, "storage.txt");
        String strLigne = "";

        try {
            BufferedReader input = new BufferedReader(new FileReader(fFichier));
            while ((strLigne = input.readLine()) != null) {
                int i = 0;
                AlistHistorique.add(i, strLigne);
                i++;
            }
            input.close();
        } catch (FileNotFoundException e) {
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
