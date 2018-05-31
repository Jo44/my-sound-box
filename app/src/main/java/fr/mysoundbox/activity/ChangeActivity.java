package fr.mysoundbox.activity;

import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import fr.mysoundbox.R;
import fr.mysoundbox.activity.dialog.CustomDialogConfirmation;
import fr.mysoundbox.activity.dialog.CustomDialogInfos;
import fr.mysoundbox.bean.Sample;
import fr.mysoundbox.controller.MusicController;
import fr.mysoundbox.exception.TechnicalException;

/**
 * Activité Change qui permet à l'utilisateur de modifier le sample associé au bouton voulu
 * <p>
 * Author: Jonathan B.
 * Created: 22/05/2018
 * Last Updated: 31/05/2018
 */
public class ChangeActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * Attributs
     */
    // Attributs de classe
    private final int REQUEST_CODE_BROWSE_AUDIO = 101;
    private MusicController musicCtrl;
    private int sampleId;
    private MediaPlayer mp = null;
    private boolean customSet = false;
    private boolean saved = true;
    private Sample currentSample;

    // Attributs d'IHM
    private View changeAnchor;
    private Button backManageButton;
    private Button infosButton;
    private EditText fileName;
    private TextView filePath;
    private Button fileBrowse;
    private Button fileTest;
    private Button fileDefault;
    private Button saveButton;

    /**
     * Création de l'activité
     *
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String INFO = "[INFO]";
        Log.e(INFO, "=> ChangeActivity");
        // Récupère les paramètres du bundle
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            sampleId = extras.getInt("sampleId");
        }
        // Initialisation du controller
        musicCtrl = new MusicController();
        // Initialisation du layout
        initLayout();
        // Initialisation des listeners
        initListeners();
        // Récupère les MusicDataFile
        initMusicDataFiles();
        // Récupère le sample actif
        initSample();
        // Initialisation de l'affichage
        initDisplay();
    }

    /**
     * Démarrage de l'activité
     */
    @Override
    protected void onStart() {
        super.onStart();
        // Initialisation de l'audio
        initAudio();
    }

    /**
     * Fermeture de l'activité
     */
    @Override
    protected void onStop() {
        super.onStop();
        // Fermeture de l'audio
        closeAudio();
    }

    /**
     * Lors du retour via touche physique
     */
    @Override
    public void onBackPressed() {
        // Dialog de confirmation de clôture
        confirmExit();
    }

    /**
     * Initialise le layout et récupère les éléments
     */
    private void initLayout() {
        // Charge le layout
        setContentView(R.layout.activity_change);
        // Récupère les éléments du layout
        changeAnchor = findViewById(R.id.changeAnchor);
        backManageButton = findViewById(R.id.backManageButton);
        infosButton = findViewById(R.id.infosButton);
        fileName = findViewById(R.id.fileName);
        filePath = findViewById(R.id.filePath);
        fileBrowse = findViewById(R.id.fileBrowse);
        fileTest = findViewById(R.id.fileTest);
        fileDefault = findViewById(R.id.fileDefault);
        saveButton = findViewById(R.id.saveButton);
    }

    /**
     * Initialise les listeners
     */
    private void initListeners() {
        // Bouton 'Retour'
        backManageButton.setOnClickListener(this);
        // Bouton 'Infos'
        infosButton.setOnClickListener(this);
        // Bouton 'Parcourir'
        fileBrowse.setOnClickListener(this);
        // Bouton 'Tester'
        fileTest.setOnClickListener(this);
        // Bouton 'Par défaut'
        fileDefault.setOnClickListener(this);
        // Bouton 'Sauvegarder'
        saveButton.setOnClickListener(this);
    }

    /**
     * Initialisation des deux fichiers MusicDataFile (celui par défaut et celui personnalisé)
     */
    private void initMusicDataFiles() {
        try {
            // Initialisation des MusicDataFiles
            musicCtrl.initMusicDataFiles(this);
        } catch (TechnicalException tex) {
            Log.e("ERROR", "Impossible d'obtenir le MusicDataFile et le MusicDataDefaultFile !");
            Log.e("ERROR", "Fermeture de l'activé !!");
            // Ferme l'activité
            closeActivity();
        }
    }

    /**
     * Initialise le sample actif
     */
    private void initSample() {
        currentSample = musicCtrl.getSample(sampleId);
        if (currentSample == null) {
            Log.e("ERROR", "Impossible de récupérer le sample !");
            Log.e("ERROR", "Fermeture de l'activé !");
            // Ferme l'activité
            closeActivity();
        }
    }

    /**
     * Initialise l'affichage
     */
    private void initDisplay() {
        // Initialise le focus sur l'ancre (invisible) pour éviter le focus sur l'EditText
        changeAnchor.setFocusableInTouchMode(true);
        changeAnchor.requestFocus();

        // Récupération des infos du sample
        String title = currentSample.getName();
        String path = currentSample.getPath();

        // Remplis les champs puis désactive ou non l'édition du titre en fonction de l'état 'customSet' qui définit si le sample est celui par défaut ou un personnalisé
        fileName.setText(title);
        filePath.setText(path);
        if (!customSet) {
            // Si path par défaut, blocage du EditText jusqu'à prochain clic sur 'Parcourir'
            fileName.setEnabled(false);
        }
    }

    /**
     * Initialise l'Audio Manager
     */
    private void initAudio() {
        // Définis le volume 'Media' du périphérique par défaut (au lieu de la sonnerie)
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    /**
     * Ferme le MediaPlayer si besoin
     */
    private void closeAudio() {
        // Ferme le MediaPlayer si besoin
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
    }

    /**
     * Lors du clic sur un bouton, traitement en fonction du bouton
     *
     * @param view View
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            // Bouton 'Retour'
            case R.id.backManageButton:
                // Dialog de confirmation de clôture
                confirmExit();
                break;
            // Bouton 'Infos'
            case R.id.infosButton:
                infosDialog();
                break;
            // Bouton 'Parcourir'
            case R.id.fileBrowse:
                // Ouvre l'exploreur de fichier audio
                Intent browseIntent = new Intent();
                browseIntent.setType("audio/*");
                browseIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(browseIntent, REQUEST_CODE_BROWSE_AUDIO);
                break;
            // Bouton 'Tester'
            case R.id.fileTest:
                testSample();
                break;
            // Bouton 'Par défaut'
            case R.id.fileDefault:
                reInitDialog();
                break;
            // Bouton 'Sauvegarder'
            case R.id.saveButton:
                tryToSaveDialog();
                break;
        }
    }

    /**
     * Dialog perso d'affichage des informations
     */
    private void infosDialog() {
        // Création du dialog
        CustomDialogInfos infosDialog = new CustomDialogInfos(this, getString(R.string.infos_msg));
        infosDialog.show();
    }

    /**
     * Lors de la sélection d'un fichier audio, conserve l'Uri récupérée
     *
     * @param requestCode int
     * @param resultCode  int
     * @param data        Intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_BROWSE_AUDIO) {
            if (resultCode == RESULT_OK) {

                // Récupération de l'Uri si un fichier est sélectionné
                Uri uri = data.getData();

                if (uri != null) {

                    // Ré-active le EditText jusqu'au prochain clic sur 'Par défaut'
                    fileName.setEnabled(true);

                    // Met à jour le nom et le path du fichier sélectionné
                    fileName.setText(getFileName(uri));
                    filePath.setText(uri.toString());

                    // Met à jour le sample actuel
                    currentSample = new Sample(fileName.getText().toString(), filePath.getText().toString(), false, new ArrayList<MediaPlayer>());

                    // Modifie les variables 'customSet' et 'saved'
                    customSet = true;
                    saved = false;

                    // Focus sur le nom
                    fileName.requestFocus();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Récupère le nom du fichier à partir de l'URI
     *
     * @param uri Uri
     * @return String
     */
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    // Récupère le nom (avec extension)
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        // Supprime l'extension
        result = removeExtension(result);
        return result;
    }

    /**
     * Supprime l'extension du nom d'un fichier
     *
     * @param fileName String
     * @return String
     */
    private String removeExtension(String fileName) {
        if (fileName != null && !fileName.isEmpty()) {
            int cutIndex = fileName.lastIndexOf(".");
            if (cutIndex != -1) {
                fileName = fileName.substring(0, cutIndex);
            }
        }
        return fileName;
    }

    /**
     * Test le sample en récupérant la ressource depuis le stockage externe ou depuis les ressources du projet
     */
    private void testSample() {
        try {
            // Création du MediaPlayer
            mp = musicCtrl.createMediaPlayer(this, currentSample);
            // Lance le MediaPlayer
            mp.start();
            Toast.makeText(this, R.string.play_success, Toast.LENGTH_LONG).show();
        } catch (NullPointerException npe) {
            Toast.makeText(this, R.string.play_fail, Toast.LENGTH_LONG).show();
        } finally {
            // Arrête immédiatement le MediaPlayer
            closeAudio();
        }
    }

    /**
     * Dialog perso de confirmation de la ré-initialisation par défaut
     */
    private void reInitDialog() {
        // Initialisation du listener
        CustomDialogConfirmation.myOnClickListener confirmationListener = new CustomDialogConfirmation.myOnClickListener() {
            @Override
            public void onConfirmationClick() {
                // Ré-initialise le sample par défaut
                reInit();
            }
        };
        // Création du dialog
        CustomDialogConfirmation confirmationDialog = new CustomDialogConfirmation(this, confirmationListener, getString(R.string.reinit_msg));
        confirmationDialog.show();
    }

    /**
     * Ré-initialise le sample par défaut et met à jour les variables
     */
    private void reInit() {
        try {
            // Ré-initialise le sample par défaut
            musicCtrl.reinitSample(this, sampleId);

            // Récupère le sample par défaut
            currentSample = musicCtrl.getSample(sampleId);

            // Toast de succès
            Toast.makeText(this, R.string.reinit_success, Toast.LENGTH_LONG).show();

            // Mise à jour des champs par défaut
            fileName.setText(currentSample.getName());
            filePath.setText(currentSample.getPath());

            // Désactive l'EditText jusqu'à prochain clic sur 'Parcourir'
            fileName.setEnabled(false);

            // Met à jour les variables 'customSet' et 'saved'
            customSet = false;
            saved = true;
        } catch (TechnicalException e) {
            // Toast d'échec de ré-initialisation
            Toast.makeText(this, R.string.reinit_fail, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Dialog perso de confirmation de la sauvegarde
     */
    private void tryToSaveDialog() {
        // Initialisation du listener
        CustomDialogConfirmation.myOnClickListener confirmationListener = new CustomDialogConfirmation.myOnClickListener() {
            @Override
            public void onConfirmationClick() {
                // Essaye de sauvegarder le sample perso
                tryToSave();
            }
        };
        // Création du dialog
        CustomDialogConfirmation confirmationDialog = new CustomDialogConfirmation(this, confirmationListener, getString(R.string.save_msg));
        confirmationDialog.show();
    }

    /**
     * Essaye d'enregistrer le sample perso si un titre et un fichier sont renseignés et la variable 'customSet' est true
     */
    private void tryToSave() {
        if (!customSet) {
            // Si l'activité n'est pas en mode 'customSet'
            Toast.makeText(this, R.string.save_impossible, Toast.LENGTH_LONG).show();
        } else {
            if (fileName.getText() == null || fileName.getText().toString().trim().isEmpty()) {
                // Toast si le titre n'est pas renseigné correctement
                Toast.makeText(this, R.string.save_missing_title, Toast.LENGTH_LONG).show();
                fileName.requestFocus();
            } else if (filePath.getText() == null || filePath.getText().toString().trim().isEmpty()) {
                // Toast si le fichier n'est pas renseigné correctement
                Toast.makeText(this, R.string.save_missing_path, Toast.LENGTH_LONG).show();
            } else {
                // Initialise le nouveau sample custom
                Sample newSample = new Sample(fileName.getText().toString().trim(), filePath.getText().toString().trim(), false, new ArrayList<MediaPlayer>());
                try {
                    // Sauvegarde le nouveau sample
                    musicCtrl.saveSample(this, sampleId, newSample);
                    // Toast de succès
                    Toast.makeText(this, R.string.save_success, Toast.LENGTH_LONG).show();
                    // Met à jour à variable saved
                    saved = true;
                    // Enleve le focus du titre
                    changeAnchor.requestFocus();
                } catch (TechnicalException e) {
                    // Toast d'échec de sauvegarde
                    Toast.makeText(this, R.string.save_fail, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * Confirmation de fermeture de l'activité si non sauvegardé
     */
    private void confirmExit() {
        // Si sample personnalisé sélectionné
        if (customSet && !saved) {
            // Initialisation du listener
            CustomDialogConfirmation.myOnClickListener confirmationListener = new CustomDialogConfirmation.myOnClickListener() {
                @Override
                public void onConfirmationClick() {
                    // Ferme l'activité
                    closeActivity();
                }
            };
            // Création du dialog
            CustomDialogConfirmation confirmationDialog = new CustomDialogConfirmation(this, confirmationListener, getString(R.string.exit_confirm_msg));
            confirmationDialog.show();
        } else {
            // Ferme l'activité
            closeActivity();
        }
    }

    /**
     * Ferme l'activité
     */
    private void closeActivity() {
        finish();
    }

}
