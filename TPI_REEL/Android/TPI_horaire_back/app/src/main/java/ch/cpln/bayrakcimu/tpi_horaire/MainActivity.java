package ch.cpln.bayrakcimu.tpi_horaire;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
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
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.logging.ErrorManager;

public class MainActivity extends AppCompatActivity implements AsyncReponse {

    String strId="";     // Variable qui va contenir l'id de la classe.
    String strContenuClasse ="";
    String strContenuDate="";
    ArrayList<String> AlistLibelleToutesClasses = new ArrayList<String>();   // Arraylist de string qui va contenir le nom de toute les classes existantes.

    // Ces deux variables vont être utilisés pour mettre correctement la couleur et la largeur des textviews lors de l'affichage.
    int iCouleurTableau = 0;
    Boolean bLargeurChampsHeures=true;

    Boolean bPremiereOuverture = true;  // Cette variable servira à savoir si c'est la première fois que l'on ouvre l'application.
    Boolean bEditTexteVide = false;
    Boolean bInternetOk = true;   // True si l'utilisateur à accès à internet.
    Boolean bAlternateur=true;     // Pour alterner la source de l'autocompletetextview.
    Boolean bAfficherContenuActv = false;   // si au click, on doit afficher le contenur de l'actv.

    // La dernière classe recherchée va normalement se mettre dans le champ classe lors du onCreate. Mais si le fichier qui stocke la dernière classe
    //n'existe pas encore, alors on va mettre le contenu de cette variable.
    String strClasseParDefaut="3M3I2";

    // Arraylist qui va contenir les différentes classes lorsque la requête nous renvoie plusieurs classes.
    ArrayList<String> AlistPlusieursClasses = new ArrayList<String>();

    int iCptNombreClasse = 0;                       // Le nombre de classes recues, quand plusieurs sont renvoyées.
    boolean bPlusieursClasseValidees = true;
    boolean bAppelDepuisBoutonPlusMoins=false;   // Pour savoir si la requête à été lancée depuis les boutons +, ++ , - , --.

    int iChoixTraitementOutput=0;
    String strDerniereClasseRecherchee= "";
    ArrayList<String> AlistHistorique = new ArrayList<String>();  // Variable qui va contenir le nom des classes déjà recherchées.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Mise en place du datepicker
        Button btnCalendrier = (Button) findViewById(R.id.BtnCalendrier);
        btnCalendrier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar cDateActuelle = Calendar.getInstance();
                int iAnneeActuelle = cDateActuelle.get(Calendar.YEAR);
                int iMoisActuel = cDateActuelle.get(Calendar.MONTH);
                int iJourActuel = cDateActuelle.get(Calendar.DAY_OF_MONTH);

                final DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int iAnneeChoisie, int iMoisChoisi, int iJourChoisi) {

                        int iJour = iJourChoisi;
                        int iMois = iMoisChoisi;
                        int iAnnee = iAnneeChoisie;

                        String strDateComplete = String.valueOf(iJour) + "-" + String.valueOf(iMois + 1) + "-" + String.valueOf(iAnnee);
                        EditText etDate = (EditText) findViewById(R.id.EtDate);
                        etDate.setText(strDateComplete);

                    }
                }, iAnneeActuelle, iMoisActuel, iJourActuel);
                datePickerDialog.setTitle("Veuillez Choisir la date");
                datePickerDialog.show();
            }

        });

        strDerniereClasseRecherchee = getDerniereClasseRecherchee();

        // On va masquer le clavier lors de la première execution.
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        //Test de la connectivité
        bInternetOk = EstConnecte();

        if (bInternetOk) {
            ContenuEditText();
            TraitementDate(strContenuDate, 0);  // Traitement de la date avec 0 jour, pour afficher le jour de la semaine
            Requete();

        } else {
            AfficheErreurInternet();
        }

        //Détection du balayage horizontal de l'écran.
        // Le code à l'intérieur est le même que les boutons + et -.

        LinearLayout llPrincipal = (LinearLayout) findViewById(R.id.LlPricipal);
        llPrincipal.setOnTouchListener(new SwipeListener(MainActivity.this) {
            public void onSwipeRight() {

                if (!bPlusieursClasseValidees) {
                    if (!bEditTexteVide) {
                        bPlusieursClasseValidees = true;
                        bAppelDepuisBoutonPlusMoins = true;
                        iChoixTraitementOutput = 0;
                        TraitementDate(strContenuDate, -1);
                        Requete();
                    }
                }

            }
            public void onSwipeLeft() {

                if (!bPlusieursClasseValidees) {
                    if (!bEditTexteVide) {
                        bPlusieursClasseValidees = true;
                        bAppelDepuisBoutonPlusMoins = true;
                        iChoixTraitementOutput = 0;
                        TraitementDate(strContenuDate, 1);
                        Requete();
                    }
                }
            }
        });

        // Si l'utilisateur vient de cliquer sur le bouton pour afficher lhistorique, alors on va afficher le contenu de l'actv.

        final AutoCompleteTextView actvClasse = (AutoCompleteTextView) findViewById(R.id.ActvClasse);
        actvClasse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bAfficherContenuActv) {

                    actvClasse.showDropDown();
                    CacherClavier(getBaseContext(), actvClasse);
                }
                bAfficherContenuActv = false;

                if (bPlusieursClasseValidees) {
                    CacherClavier(getBaseContext(), actvClasse);
                    bAfficherContenuActv = true;  // Ici on remet a true sinon quand on clique, on ne peut plus taper dans le champs.
                }

            }
        });

        Button btnPlusJour = (Button) findViewById(R.id.BtnPlusJour);
        btnPlusJour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bPlusieursClasseValidees) {
                    if (!bEditTexteVide) {
                        bPlusieursClasseValidees = true;
                        bAppelDepuisBoutonPlusMoins = true;
                        iChoixTraitementOutput = 0;
                        TraitementDate(strContenuDate, 1);
                        Requete();
                    }
                }
            }
        });
        Button btnPlusSemaine = (Button) findViewById(R.id.BtnPlusSemaine);
        btnPlusSemaine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bPlusieursClasseValidees) {
                    if (!bEditTexteVide) {
                        bPlusieursClasseValidees = true;
                        bAppelDepuisBoutonPlusMoins = true;
                        iChoixTraitementOutput = 0;
                        TraitementDate(strContenuDate, 7);
                        Requete();
                    }
                }
            }
        });
        Button btnMoinsJour = (Button) findViewById(R.id.BtnMoinsJour);
        btnMoinsJour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bPlusieursClasseValidees) {
                    if (!bEditTexteVide) {
                        bPlusieursClasseValidees = true;
                        bAppelDepuisBoutonPlusMoins = true;
                        iChoixTraitementOutput = 0;
                        TraitementDate(strContenuDate, -1);
                        Requete();
                    }
                }
            }
        });
        Button btnMoinsSemaine = (Button) findViewById(R.id.BtnMoinsSemaine);
        btnMoinsSemaine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!bPlusieursClasseValidees) {
                    if (!bEditTexteVide) {
                        bPlusieursClasseValidees = true;
                        bAppelDepuisBoutonPlusMoins = true;
                        iChoixTraitementOutput = 0;
                        TraitementDate(strContenuDate, -7);
                        Requete();
                    }
                }
            }
        });


        Button btnRechercher = (Button) findViewById(R.id.BtnRechercher);
        btnRechercher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bInternetOk = EstConnecte();
                if (bInternetOk) {
                    bEditTexteVide = false;
                    bAppelDepuisBoutonPlusMoins = false;
                    iChoixTraitementOutput = 0;
                    ContenuEditText();
                    if (!bEditTexteVide) {
                        TraitementDate(strContenuDate, 0);
                        Requete();
                    }
                } else {
                    AfficheErreurInternet();
                }


            }
        });


        final Button btnAlternateur = (Button) findViewById(R.id.BtnAlternateur);
        btnAlternateur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                // En fonction de la variable, Changemenet du texte du bouton, et modification de la source de l'actv.

                if (bAlternateur) {
                    MiseEnPlaceActv(AlistHistorique);
                    Toast.makeText(MainActivity.this, "Affichage de l'historique", Toast.LENGTH_SHORT).show();
                    bAfficherContenuActv = true;
                    btnAlternateur.setText("Classes");
                    actvClasse.performClick();   // pour afficher la liste de l'histo.


                } else {
                    MiseEnPlaceActv(AlistLibelleToutesClasses);
                    Toast.makeText(MainActivity.this, "Affichage de toutes les classes", Toast.LENGTH_SHORT).show();
                    btnAlternateur.setText("Historique");
                }
                bAlternateur = !bAlternateur;
            }
        });

    }

    // Fonction pour masquer le clavier.

    public static void CacherClavier(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        inputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }



  // Si l'utilisateur ne dispose pas de connection internet, un menu d'erreur s'affiche.

    public void AfficheErreurInternet() {
        AlertDialog.Builder Alerte = new AlertDialog.Builder(MainActivity.this);
        Alerte.setCancelable(false);
        Alerte.setTitle("Pas de connexion internet");
        Alerte.setMessage("Votre appareil n'est actuellement pas connecté à internet. Veuillez vérifier votre connexion. ");
        Alerte.setPositiveButton("Paramètres", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        })
                .setNegativeButton("Continuer", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        final AlertDialog Message = Alerte.create();
        Message.show();
    }


    //Fonction qui va recevoir les outputs des requetes.

   public void RetourOutput(String output) {


       // En fonction de iChoixTraitementOutput, on va aiguiller le resultat.

       if (iChoixTraitementOutput == 0) {
           strId = TraitementId(output);


           if (iCptNombreClasse > 1 && !bPlusieursClasseValidees) {
               Toast.makeText(MainActivity.this, iCptNombreClasse + " classes trouvées, veuillez choisir la bonne classe", Toast.LENGTH_LONG).show();
               bAfficherContenuActv = true;
               MiseEnPlaceActv(AlistPlusieursClasses);
               bPlusieursClasseValidees = true;
               AutoCompleteTextView actvClasse = (AutoCompleteTextView) findViewById(R.id.ActvClasse);
               actvClasse.performClick();

           } else {

               bAfficherContenuActv=false;  // Ca va permettre de reafficher le clavier une fois qu'on a choisi la bonne classe, sinon on n'avait pas de clavier.
               bPlusieursClasseValidees = false;
               // Test pour voir si la classe n'a pas été trouvée.
               if ("".equals(strId) || strId == null) {
                   Toast.makeText(MainActivity.this, "Classe non trouvée", Toast.LENGTH_SHORT).show();
                   bEditTexteVide = true;
               } else {
                   // Si id correct, alors on execute la requête suivante.
                   AsyncAffichageHoraire asyncAffichageHoraire1 = new AsyncAffichageHoraire(MainActivity.this, 0);
                   asyncAffichageHoraire1.delegate = (AsyncReponse) this;
                   asyncAffichageHoraire1.execute("http://devinter.cpln.ch/pdf/hypercool/controler.php?action=horaire&ident=" + strId + "&sub=date&date=" + strContenuDate);
                   // Si appel depuis un bouton +, ++, -, --, alors on n'écrit pas dans le fichier, car la classe est déjà écrite.
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
       if (iChoixTraitementOutput == 1) {

           TraitementToutesClasses(output);

       }
       if (iChoixTraitementOutput == 2) {

           // Si on ne recoit rien, on va afficher un message, on vide également l'affichage.

           if (!"[]".equals(output)) {
               AffichageHoraire(output);
           } else {
               LinearLayout llGeneral = (LinearLayout) findViewById(R.id.LlGeneral);
               llGeneral.removeAllViews();
               TextView tvJourDeLaSemaine = (TextView) findViewById(R.id.TvJourDeLaSemaine);
               tvJourDeLaSemaine.append(" - Pas de cours");
           }
       }

       iChoixTraitementOutput++;

   }

    public boolean EstConnecte(){
        ConnectivityManager cm = (ConnectivityManager)getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        boolean bInternetOk = networkInfo != null && networkInfo.isConnectedOrConnecting();
        return  bInternetOk;
    }


    // Fonction pour mettre en place et mettre à jour l'autocompleteTextview

    public void MiseEnPlaceActv(ArrayList<String> Alist){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, Alist);
        final AutoCompleteTextView actvClasse = (AutoCompleteTextView)findViewById(R.id.ActvClasse);
        actvClasse.setThreshold(0);
        actvClasse.setAdapter(adapter);
    }


    // Ecriture de la classe dans le fichier.

    public void EcritureFichier() {

        AutoCompleteTextView actvClasse = (AutoCompleteTextView) findViewById(R.id.ActvClasse);
        String strChampsClasse = actvClasse.getText().toString();


        // On va mettre en majuscule pour éviter la redondance (3m3i2 et 3M3I2)
        strChampsClasse = strChampsClasse.toUpperCase();


        File fChemin = getBaseContext().getFilesDir();
        File fFichier = new File(fChemin, "storage.txt");
        Writer writer;
        // Si c'est vide, on ne met pas dans le fichier. Utile par exemple lors du changement d'orientation si le champ était vide.
        if (!"".equals(strChampsClasse)) {
            try {
                writer = new BufferedWriter(new FileWriter(fFichier, true));
                writer.append(strChampsClasse + "\n");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Ecriture dans le fichier dernière classe

        File fFichierDerniereClasse = new File(fChemin, "DerniereClasse.txt");

        // si c'est vide on supprime pas le fichier non plus

        if (!"".equals(strChampsClasse)) {
            try {
                PrintWriter pw = new PrintWriter(fFichierDerniereClasse);
                pw.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Writer writer2;


            try {
                writer2 = new BufferedWriter(new FileWriter(fFichierDerniereClasse, true));
                writer2.append(strChampsClasse);
                writer2.close();
            } catch (IOException e) {
            }
        }
    }



    // Lecture du fichier et ajout des lignes dans l'arraylist

    public void LectureFichier(){
        File fChemin = getBaseContext().getFilesDir();
        File fFichier = new File(fChemin, "storage.txt");
        String strligne ="";

        try{
            BufferedReader input = new BufferedReader(new FileReader(fFichier));
            while ((strligne = input.readLine()) != null) {
                int i= 0;
                AlistHistorique.add(i, strligne);
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


    //Fonction pour récupérer la dernière classe recherchée
    public String getDerniereClasseRecherchee() {

        File fChemin = getBaseContext().getFilesDir();
        String strligne = "";
        File fFichierDerniereClasse = new File(fChemin, "DerniereClasse.txt");

        try {
            BufferedReader input = new BufferedReader(new FileReader(fFichierDerniereClasse));
            while ((strligne = input.readLine()) != null) {
                strDerniereClasseRecherchee = strligne;
            }
            input.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strDerniereClasseRecherchee;
    }


    //Fonction pour supprimmer les doublons de lignes dans l'arraylist contenant l'historique..
    public void SuppressionDoublons(){

        HashSet<String> hashSet = new HashSet<String>();
        hashSet.addAll(AlistHistorique);
        AlistHistorique.clear();
        AlistHistorique.addAll(hashSet);

    }


    // Ajout de jour / semaine à la date.

    public void TraitementDate(String strContenudate, int iJourAModifier) {

        try {
            SimpleDateFormat Formater = new SimpleDateFormat("dd-MM-yyyy");
            Date ObjetDate = Formater.parse(strContenudate);
            Calendar calendrier = Calendar.getInstance();
            calendrier.setTime(ObjetDate);
            calendrier.add(Calendar.DATE, iJourAModifier);
            strContenuDate = Formater.format(calendrier.getTime());


            EditText EtDate = (EditText) findViewById(R.id.EtDate);
            EtDate.setText(strContenuDate);


            // Affichage du jour de la semaine

            String strNomduJour = calendrier.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
            switch (strNomduJour) {
                case "Monday":
                    strNomduJour = "Lundi";
                    break;
                case "Tuesday":
                    strNomduJour = "Mardi";
                    break;
                case "Wednesday":
                    strNomduJour = "Mercredi";
                    break;
                case "Thursday":
                    strNomduJour = "Jeudi";
                    break;
                case "Friday":
                    strNomduJour = "Vendredi";
                    break;
                case "Saturday":
                    strNomduJour = "Samedi";
                    break;
                case "Sunday":
                    strNomduJour = "Dimanche";
                    break;
            }
            TextView tvJourDeLaSemaine = (TextView) findViewById(R.id.TvJourDeLaSemaine);
            tvJourDeLaSemaine.setText(strNomduJour);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    // Fonction pour créer le menu

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    // Lorseque l'on clique sur un bouton du menu, on lance la fonction correspondente.

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

//Si l'utilisateur n'a pas de connexion internet, on ne lui laisse pas accéder à la vue semaine.

    public void VueSemaine() {



        bInternetOk = EstConnecte();
        if (bInternetOk) {

            Intent intent = new Intent(this, Activite_VueSemaine.class);
            startActivity(intent);
        } else {
            AfficheErreurInternet();
        }
    }

    // Création d'un nouveau fichier historique, et vidage de l'arraylist.

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

        Button btnAlternateur = (Button) findViewById(R.id.BtnAlternateur);
        String strTexteAlternateur = btnAlternateur.getText().toString();
        if ("Classes".equals(strTexteAlternateur)) {
            btnAlternateur.setText("Historique");
        }


    }


    // Cette fonction va mettre le contenu des champs dans des variables.

    public void ContenuEditText() {

        AutoCompleteTextView actvClasse = (AutoCompleteTextView) findViewById(R.id.ActvClasse);

        if (bPremiereOuverture) {
            if ("".equals(strDerniereClasseRecherchee)) {
                actvClasse.setText(strClasseParDefaut);
            } else {
                actvClasse.setText(strDerniereClasseRecherchee);
            }
        }

        strContenuClasse = actvClasse.getText().toString();


        if ("".equals(strContenuClasse)) {
            bEditTexteVide = true;
            Toast.makeText(MainActivity.this, "Veuillez entrer une classe", Toast.LENGTH_SHORT).show();
        } else {
            // On remplace les espaces par %20 sinon la classe n'était pas valide.
            strContenuClasse = strContenuClasse.replace(" ", "%20");
        }


        SimpleDateFormat Formater = new SimpleDateFormat("dd-MM-yyyy");
        Calendar calendar = Calendar.getInstance();
        String strJourActuel = Formater.format(calendar.getTime());


        EditText etDate = (EditText) findViewById(R.id.EtDate);
        strContenuDate = etDate.getText().toString();

        if (bPremiereOuverture) {
            strContenuDate = strJourActuel;
        }

        if ("".equals(strContenuDate)) {
            bEditTexteVide = true;
            Toast.makeText(MainActivity.this, "Veuillez entrer une date", Toast.LENGTH_SHORT).show();
        } else {
            etDate.setText(strContenuDate);
        }

        bPremiereOuverture = false;


// Lancement de 2 requêtes, d'abord celle pour avoir l'id de la classe, puis celle pour remplir l'arraylist contenant les noms des classes.

    }
    public void Requete() {

        AsyncAffichageHoraire asyncAffichageHoraire0 = new AsyncAffichageHoraire(MainActivity.this, 1);
        asyncAffichageHoraire0.delegate = (AsyncReponse) this;
        asyncAffichageHoraire0.execute("http://devinter.cpln.ch/pdf/hypercool/controler.php?action=ressource&nom=" + strContenuClasse);
        AsyncAffichageHoraire asyncAffichageHoraire2 = new AsyncAffichageHoraire(MainActivity.this, 1);
        asyncAffichageHoraire2.delegate = (AsyncReponse) this;
        asyncAffichageHoraire2.execute("http://devinter.cpln.ch/pdf/hypercool/controler.php?action=ressource&nom=");


    }

    public void TraitementToutesClasses(String strOutputlisteclasse) {

        JSONObject reader = null;
        String strCode = "";

        AlistLibelleToutesClasses.clear();

        // On va récupérer les noms des toutes les classes existantes.

        try {
            reader = new JSONObject(strOutputlisteclasse);
            Iterator iterator = reader.keys();
            for (int iCpt = 0; iCpt < 329; iCpt++) {
                strCode = (String) iterator.next();
                JSONObject jsonObject = reader.getJSONObject(strCode);
                AlistLibelleToutesClasses.add(iCpt, jsonObject.getString("nom"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            // Si cette requete ne répond pas, ca signifie que hypercool n'est pas disponible.
            Toast.makeText(MainActivity.this, "Hypercool hors-ligne, impossible de récupérer les données.", Toast.LENGTH_SHORT).show();
        }


        HashSet<String> hashSet = new HashSet<String>();
        hashSet.addAll(AlistLibelleToutesClasses);
        AlistLibelleToutesClasses.clear();
        AlistLibelleToutesClasses.addAll(hashSet);

    }

    public void AffichageHoraire(String strOutput) {


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


        ArrayList<Integer> AListCalcul = new ArrayList<Integer>();
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
            JSONArray jsonArrayGeneral = new JSONArray(strOutput);
            for (i = 0; i < jsonArrayGeneral.length(); i++) {

                JSONObject jsonObjectGeneral = jsonArrayGeneral.getJSONObject(i);
                ArrayHeureDebut[i] = jsonObjectGeneral.getString("heureDebut");
                ArrayHeureFin[i] = jsonObjectGeneral.getString("heureFin");
                ArrayLibelle[i] = jsonObjectGeneral.getString("libelle");

                try {
                    JSONArray jsonArrayProf = jsonObjectGeneral.getJSONArray("professeur");
                    ArrayProfesseur[i] = jsonArrayProf.getString(0);

                } catch (JSONException e) {
                    ArrayProfesseur[i] = "/";
                }

                try {
                    JSONArray jsonArraySalle = jsonObjectGeneral.getJSONArray("salle");
                    // ArraySalle[i] = jsonArraySalle.getString(jsonArraySalle.length()-1);
                    ArraySalle[i] = jsonArraySalle.getString(0);

                } catch (JSONException e) {
                    ArraySalle[i] = "/-";
                }

                try {
                    ArrayProfesseur[i] = ArrayProfesseur[i].substring(0, ArrayProfesseur[i].indexOf(" "));

                } catch (Exception e) {
                }

                try {
                    ArraySalle[i] = ArraySalle[i].substring(0, ArraySalle[i].indexOf("-"));

                } catch (Exception e) {

                }

                ArrayHeureDebutComplet[i] = ArrayHeureDebut[i];
                ArrayHeureFinComplet[i] = ArrayHeureFin[i];
                ArrayHeureDebut[i] = ArrayHeureDebut[i].substring(0, ArrayHeureDebut[i].length() - 3);
                ArrayHeureFin[i] = ArrayHeureFin[i].substring(0, ArrayHeureFin[i].length() - 3);


                // Ajout des données présentes dans les array aux arraylists.

                AlistHeureDebutComplet.add(i, ArrayHeureDebut[i]);
                AlistHeureFinComplet.add(i, ArrayHeureDebut[i]);
                AlistProfesseur.add(i, ArrayProfesseur[i]);
                AlistArraySalle.add(i, ArraySalle[i]);
                AlistLibelle.add(i, ArrayLibelle[i]);

                try {
                    iHeureDebut = Integer.parseInt(ArrayHeureDebut[i]);
                    iHeureFin = Integer.parseInt(ArrayHeureFin[i]);


                    ArrayCalculHeure[i] = iHeureDebut + iHeureFin;
                    ArrayCalculHeureString[i] = String.valueOf(ArrayCalculHeure[i]);


                    AlistCalculHeure.add(i, ArrayCalculHeure[i]);
                    AListCalcul.add(i, iHeureDebut + iHeureFin);


                } catch (NumberFormatException nfe) {
                }


            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Tri et basculement en fonction du bon ordre des données.
        JSONArray jsonArrayGeneral = null;
        try {
            jsonArrayGeneral = new JSONArray(strOutput);
            Collections.sort(AListCalcul);    // Tri du calcul càd HeureDebut + HeureFin.

            for (int iBranche = 0; iBranche < jsonArrayGeneral.length(); iBranche++) {

                for (int i10 = 0; i10 < jsonArrayGeneral.length(); i10++) {
                    if (AlistCalculHeure.get(i10).equals(AListCalcul.get(iBranche))) {
                        AlistLibelle.add(iBranche, ArrayLibelle[i10]);
                        AlistHeureDebutComplet.add(iBranche, ArrayHeureDebutComplet[i10]);
                        AlistHeureFinComplet.add(iBranche, ArrayHeureFinComplet[i10]);
                        AlistProfesseur.add(iBranche, ArrayProfesseur[i10]);
                        AlistArraySalle.add(iBranche, ArraySalle[i10]);
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Création des layouts necessaires à l'affichage.

        LinearLayout llGeneral = (LinearLayout) findViewById(R.id.LlGeneral);
        LinearLayout llHoriz = new LinearLayout(getBaseContext());
        LinearLayout llVert1 = new LinearLayout(getBaseContext());
        LinearLayout llVert2 = new LinearLayout(getBaseContext());
        LinearLayout llVert3 = new LinearLayout(getBaseContext());
        iCouleurTableau = 0;


        if ("{\"error\":\"la date n'est pas valide\"}".equals(strOutput)) {
            Toast.makeText(MainActivity.this, "Veuillez entrer une date valide", Toast.LENGTH_SHORT).show();
            bEditTexteVide = true;
        } else {

            // Creation de l'affichage.
            llGeneral.removeAllViews();
            for (int iBranche = 0; iBranche < jsonArrayGeneral.length(); iBranche++) {

                llHoriz = CreationLayoutHoriz();
                llVert1 = CreationLayoutVertic();
                llVert2 = CreationLayoutVertic();
                llVert3 = CreationLayoutVertic();
                iCouleurTableau++;
                TextView tv1 = CreationTextView(AlistHeureDebutComplet.get(iBranche));
                TextView tv2 = CreationTextView(AlistHeureFinComplet.get(iBranche));
                bLargeurChampsHeures = false;
                TextView tv3 = CreationTextView(AlistLibelle.get(iBranche));
                TextView tv4 = CreationTextView(AlistProfesseur.get(iBranche));
                TextView tv5 = CreationTextView(AlistArraySalle.get(iBranche));
                TextView tv6 = CreationTextView(" ");
                bLargeurChampsHeures = true;
                llGeneral.addView(llHoriz);
                llHoriz.addView(llVert1);
                llHoriz.addView(llVert2);
                llHoriz.addView(llVert3);
                llVert1.addView(tv1);
                llVert1.addView(tv2);
                llVert2.addView(tv3);
                llVert2.addView(tv4);
                llVert3.addView(tv5);
                llVert3.addView(tv6);
            }

        }

    }

    // Création du TextView avec tous ces paramètres.

    public  TextView CreationTextView (String strTexte) {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int iHauteurEcran = displayMetrics.heightPixels;
        int iLargeurEcran = displayMetrics.widthPixels;

        TextView tv = new TextView(getBaseContext());
        tv.setText(strTexte);
        if (bLargeurChampsHeures) {
            tv.setWidth(iLargeurEcran / 5);
        } else {
            tv.setWidth(iLargeurEcran / 2);
        }
        tv.setHeight(iHauteurEcran / 11);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(Color.BLACK);
        return tv;
    }


    public  LinearLayout CreationLayoutHoriz() {
        LinearLayout ll = new LinearLayout(getBaseContext());
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        ShapeDrawable sd = new ShapeDrawable();
        sd.setShape(new RectShape());
        sd.getPaint().setStrokeWidth(5);
        sd.getPaint().setStyle(Paint.Style.STROKE);
        ll.setBackground(sd);
        return ll;
    }

    public LinearLayout CreationLayoutVertic() {

        LinearLayout ll = new LinearLayout(getBaseContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        ShapeDrawable sd = new ShapeDrawable();
        if ((iCouleurTableau % 2) == 1) {
            sd.getPaint().setColor(Color.argb(235, 235, 235, 235));
        } else {
            sd.getPaint().setColor(Color.argb(201, 201, 201, 201));
        }
        ll.setBackground(sd);
        return ll;
    }


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


}
