package fr.mysoundbox.tools;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import fr.mysoundbox.bean.MusicDataFile;
import fr.mysoundbox.bean.Sample;
import fr.mysoundbox.exception.TechnicalException;

/**
 * Classe en charge d'écrire le MusicDataFile dans un fichier (dans les datas privées) et de le lire ainsi que le MusicDataFile par défaut dans les ressources
 * Les MusicDataFile permettent de sauvegarder les titres et paths des samples (par défaut ou de l'utilisateur)
 * <p>
 * Author: Jonathan B.
 * Created: 20/05/2018
 * Last Updated: 22/05/2018
 */
public class MusicDataTools {

    /**
     * Attributs
     */
    private static final String filename = "uris.data";
    private static final String CHARSET = "UTF-8";
    private static final int BUFFER_SIZE = 2048;

    /**
     * Ecris le MusicDataFile dans le fichier 'filename', dans la mémoire privée de l'application
     *
     * @param context       Context
     * @param musicDataFile MusicDataFile
     * @throws TechnicalException TechnicalException
     */
    public static void writeFile(Context context, MusicDataFile musicDataFile) throws TechnicalException {
        FileOutputStream fout = null;
        OutputStreamWriter writer = null;

        try {
            fout = context.openFileOutput(filename, Context.MODE_PRIVATE);
            writer = new OutputStreamWriter(fout, CHARSET);
            JSONObject jsonMusicData = musicDataFile.toJSONObject();
            String rawMusicData = jsonMusicData.toString();
            writer.write(rawMusicData);
            writer.flush();
            Log.e("INFO", "Fichier MusicDataFile enregistré");
        } catch (IOException ioex) {
            throw new TechnicalException("Impossible d'écrire le fichier MusicDataFile !");
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {
                    Log.e("ERROR", "Impossible de fermer correctement l'OutputStreamWriter !");
                }
            }
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException ex) {
                    Log.e("ERROR", "Impossible de fermer correctement le FileOutputStream !");
                }
            }
        }
    }

    /**
     * Lit le MusicDataFile du fichier 'res/raw/default_samples.json'
     *
     * @param context Context
     * @return MusicDataFile
     * @throws TechnicalException TechnicalException
     */
    public static MusicDataFile readDefaultFile(Context context) throws TechnicalException {
        MusicDataFile musicDataFile;

        StringBuilder sb = new StringBuilder();
        Scanner scanner = new Scanner(context.getResources().openRawResource(context.getResources().getIdentifier("default_samples", "raw", context.getPackageName())));

        while (scanner.hasNextLine()) {
            sb.append(scanner.nextLine());
            sb.append("\n");
        }

        // Test si récupération ok
        String rawData = sb.toString();
        if (!rawData.trim().isEmpty()) {
            // Parse le String récupéré en MusicDataFile
            musicDataFile = getMusicDataFileFromString(rawData);
            Log.e("INFO", "MusicDataFile par défaut récupéré");
        } else {
            throw new TechnicalException("Le fichier MusicDataFile par défaut récupéré est vide !");
        }
        return musicDataFile;
    }

    /**
     * Lit le MusicDataFile du fichier 'filename' dans la mémoire privée de l'application
     *
     * @param context Context
     * @return MusicDataFile
     * @throws TechnicalException TechnicalException
     */
    public static MusicDataFile readFile(Context context) throws TechnicalException {
        MusicDataFile musicDataFile;
        StringBuilder sb = new StringBuilder();
        FileInputStream fin = null;
        InputStreamReader reader = null;

        try {
            // Récupère le fichier en String
            fin = context.openFileInput(filename);
            reader = new InputStreamReader(fin, CHARSET);
            char[] buffer = new char[BUFFER_SIZE];
            while (reader.read(buffer) != -1) {
                sb.append(buffer);
            }
        } catch (IOException ioex) {
            throw new TechnicalException("Impossible de récupérer le fichier MusicDataFile ! (inexistant ?)");
        } finally {
            // Ferme le reader
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    Log.e("ERROR", "Impossible de fermer correctement l'InputStreamReader !");
                }
            }
            // Ferme l'input stream
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException ex) {
                    Log.e("ERROR", "Impossible de fermer correctement le FileInputStream !");
                }
            }
        }

        // Test si récupération ok
        String rawData = sb.toString();
        if (!rawData.trim().isEmpty()) {
            // Parse le String récupéré en MusicDataFile
            musicDataFile = getMusicDataFileFromString(rawData);
            Log.e("INFO", "MusicDataFile récupéré");
        } else {
            throw new TechnicalException("Le fichier MusicDataFile récupéré est vide !");
        }
        return musicDataFile;
    }

    /**
     * Parse un string brut en JSON puis objet MusicDataFile
     *
     * @param rawData String
     * @return MusicDataFile
     */
    private static MusicDataFile getMusicDataFileFromString(String rawData) throws TechnicalException {
        MusicDataFile musicDataFile;
        List<Sample> listSample;
        JSONObject jsonMusicData;
        JSONArray jsonArraySample;

        try {
            // Parse le String en JSONObject
            jsonMusicData = new JSONObject(rawData.trim());
            // Récupère la liste de JSONObject 'custom'
            jsonArraySample = jsonMusicData.getJSONArray("samples");
            // Si la liste comporte bien 12 samples
            if (jsonArraySample != null && jsonArraySample.length() == 12) {
                listSample = new ArrayList<>();
                // Récupère chaque sample
                for (int i = 0; i < jsonArraySample.length(); i++) {
                    JSONObject jsonSample = jsonArraySample.getJSONObject(i);
                    // Récupère le titre et le path du sample
                    String name = jsonSample.getString("name");
                    if (name != null && !name.trim().isEmpty() && name.trim().equals("null")) {
                        name = null;
                    }
                    String path = jsonSample.getString("path");
                    if (path != null && !path.trim().isEmpty() && path.trim().equals("null")) {
                        path = null;
                    }
                    boolean byDefault = jsonSample.getBoolean("default");
                    // Créer l'objet Sample et l'ajoute à la liste
                    Sample sample = new Sample(name, path, byDefault, new ArrayList<MediaPlayer>());
                    listSample.add(sample);
                }
                // Utilise la liste pour créer l'objet MusicDataFile
                musicDataFile = new MusicDataFile(listSample);
            } else {
                throw new JSONException("Le fichier MusicDataFile n'est pas valide !");
            }
        } catch (JSONException jsonex) {
            throw new TechnicalException("Impossible de parser le fichier en JSON Object !");
        }
        return musicDataFile;
    }

}
