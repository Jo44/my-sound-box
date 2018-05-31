package fr.mysoundbox.bean;

import android.media.MediaPlayer;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe modèle d'un fichier permettant de sauvegarder 12 samples
 * <p>
 * Author: Jonathan B.
 * Created: 20/05/2018
 * Last Updated: 30/05/2018
 */
public class MusicDataFile {

    /**
     * Attributs
     */
    private List<Sample> listSample;

    /**
     * Constructeur par défaut
     */
    public MusicDataFile() {
        // Créer un objet MusicDataFile comportant une liste de 12 Samples 'vierges'
        // name = null / path = null / byDefault = true / listMediaPlayer = new ArrayList<MediaPlayer>
        List<Sample> defaultListSample = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            Sample defaultSample = new Sample(null, null, true, new ArrayList<MediaPlayer>());
            defaultListSample.add(defaultSample);
        }
        this.listSample = defaultListSample;
    }

    /**
     * Constructeur
     *
     * @param listSample List<Sample>
     */
    public MusicDataFile(List<Sample> listSample) {
        this.listSample = listSample;
    }

    /**
     * ToString
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MusicDataFile : [ ");
        if (listSample == null || listSample.size() != 12) {
            sb.append("null");
        } else {
            for (Sample sample : listSample) {
                sb.append(sample.toString());
            }
        }
        sb.append(" ]");
        return sb.toString();
    }

    /**
     * ToJSON
     */
    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        // Vérifie que l'objet comporte bien une liste de 12 samples
        try {
            if (listSample != null && listSample.size() == 12) {
                JSONArray listJSONSample = new JSONArray();
                // Ajoute chaque sample à la JSONArray
                for (Sample sample : listSample) {
                    listJSONSample.put(sample.toJSONObject());
                }
                // Ajoute la JSONArray au JSONObject
                json.put("samples", listJSONSample);
            } else {
                throw new JSONException("L'objet MusicDataFile ne comporte pas 12 samples !");
            }
        } catch (JSONException jsonex) {
            Log.e("ERROR", "Impossible de créer le JSON MusicDataFile !");
            json = null;
        }
        return json;
    }

    /**
     * Getter / Setter
     */
    public Sample getSample(int sampleId) {
        return listSample.get(sampleId - 1);
    }

    public void setSample(int sampleId, Sample sample) {
        this.listSample.set(sampleId - 1, sample);
    }

}
