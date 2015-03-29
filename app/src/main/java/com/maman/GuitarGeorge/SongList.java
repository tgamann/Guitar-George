package com.maman.GuitarGeorge;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static com.maman.GuitarGeorge.Constants.*;

/*
 * Class to manage a list of songs (i.e. chord progressions)
 */
public class SongList {
    private static SongList mInstance = null;
    private final Map<String,String> mSongMap = new HashMap<>();

    protected SongList() {
        // Exists only to defeat instantiation.
    }
    public static SongList getInstance() {
        if(mInstance == null) {
            // not thread-safe, but not a concern for this app
            mInstance = new SongList();
        }
        return mInstance;
    }

    public void initialize(Context context) {
        // Reads the list of songs from permanent storage and puts them in our Map
        mSongMap.clear();
        try {
            File file = new File(context.getFilesDir(), "Songs");
            FileReader fr = new FileReader(file.getAbsoluteFile());
            BufferedReader bufferedReader = new BufferedReader(fr);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] song = line.split(":");
                mSongMap.put(song[0], song[1]);
            }
            bufferedReader.close();
        }
        catch (FileNotFoundException ex) {
            if (LOGGING_ENABLED) {
                Log.e(LOG_TAG, "FileNotFoundException in SongList.readSongFileIntoMap()");
            }
        }
        catch (IOException ex) {
            if (LOGGING_ENABLED) {
                Log.e(LOG_TAG, "IOException in SongList.readSongFileIntoMap()");
            }
        }
    }

    public void putSong(Context context, String songName) {
        // check for duplicate song filename
        if (mSongMap.containsKey(songName)) {
            // name already exists; pop-up an overwrite confirm dialog.
            DialogFragment errorDialog = new OverwriteSaveDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putString(SONG_NAME_KEY, songName);
            errorDialog.setArguments(bundle);
            errorDialog.show(((FragmentActivity)context).getSupportFragmentManager(), "Save");
            return;
        }

        // add the song to our list
        mSongMap.put(songName, ComposeFragmentState.getInstance().toString());

        // update the permanent storage
        update(context);
    }

    public void update(Context context) {
        try {
            File file = new File(context.getFilesDir(), "Songs");
            // If file does not exists, then create it
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw(new java.io.IOException());
                }
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bufferedWriter = new BufferedWriter(fw);
            for (Map.Entry<String, String> entry : mSongMap.entrySet()) {
                // separate the song name from the chords with a colon delimiter
                bufferedWriter.write(entry.getKey() + ":" + entry.getValue());
                bufferedWriter.write("\n");
            }
            bufferedWriter.close();
        }
        catch (java.io.IOException ioex) {
            if (LOGGING_ENABLED) {
                Log.e(LOG_TAG, "java.io.IOException in SongList.update()");
            }
        }
    }

    public void removeSong(String songName) {
        mSongMap.remove(songName);
    }

    public List<String> getSongArray() {
        Set<String> songSet = mSongMap.keySet();
        List<String> songList = new ArrayList<>();
        for (String song : songSet) {
            songList.add(song);
        }
        Collections.sort(songList, String.CASE_INSENSITIVE_ORDER);
        return songList;
    }

    public String getSong(String songName) {
        return mSongMap.get(songName);
    }

    public boolean songIsInLibrary(String songName) {
        return mSongMap.containsKey(songName);
    }

}
