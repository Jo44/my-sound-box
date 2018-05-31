package fr.mysoundbox.bean;

import android.media.MediaPlayer;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe modèle d'un sample
 * <p>
 * Author: Jonathan B.
 * Created: 22/05/2018
 * Last Updated: 30/05/2018
 */
public class Sample {

    /**
     * Attributs
     */
    private String name;
    private String path;
    private boolean byDefault;
    private List<MediaPlayer> listMediaPlayer;

    /**
     * Constructeur
     */
    public Sample(String name, String path, boolean byDefault, List<MediaPlayer> listMediaPlayer) {
        this.name = name;
        this.path = path;
        this.byDefault = byDefault;
        this.listMediaPlayer = listMediaPlayer;
    }

    /**
     * ToString
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ Name : ");
        if (name == null) {
            sb.append("null");
        } else {
            sb.append(name);
        }
        sb.append(" , Path : ");
        if (path == null) {
            sb.append("null");
        } else {
            sb.append(path);
        }
        sb.append(" , By default : ");
        if (byDefault) {
            sb.append("true");
        } else {
            sb.append("false");
        }
        sb.append(" , MediaPlayers : [ ");
        if (listMediaPlayer == null || listMediaPlayer.size() == 0) {
            sb.append("null");
        } else {
            for (MediaPlayer mp : listMediaPlayer) {
                if (mp == listMediaPlayer.get(0)) {
                    sb.append(mp.toString());
                } else {
                    sb.append(" , ");
                    sb.append(mp.toString());
                }
            }
        }
        sb.append(" }");
        return sb.toString();
    }

    /**
     * ToJSON
     */
    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        try {
            if (name == null || name.trim().isEmpty()) {
                json.put("name", "null");
            } else {
                json.put("name", name);
            }
            if (path == null || path.trim().isEmpty()) {
                json.put("path", "null");
            } else {
                json.put("path", path);
            }
            if (byDefault) {
                json.put("default", true);
            } else {
                json.put("default", false);
            }
        } catch (JSONException jsonex) {
            Log.e("ERROR", "Impossible de créer le JSON Sample !");
            json = null;
        }
        return json;
    }

    /**
     * Getters
     */
    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public boolean getByDefault() {
        return byDefault;
    }

    public List<MediaPlayer> getListMediaPlayer() {
        return listMediaPlayer;
    }

    /**
     * Setter (pour reset)
     */
    public void resetListMediaPlayer() {
        this.listMediaPlayer = new ArrayList<>();
    }

}
