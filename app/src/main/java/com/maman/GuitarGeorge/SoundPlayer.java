package com.maman.GuitarGeorge;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import java.io.IOException;
import java.util.HashMap;
import static com.maman.GuitarGeorge.Constants.*;

/*
 * Loads .wav files into a SoundPool, maps chord name strings to the SoundPool entries,
 * and provides a Play method.
 */
public class SoundPlayer {
    private SoundPool mSoundPool = null;
    private final HashMap<String, Integer> mSounds = new HashMap<>();

    private static SoundPlayer mInstance = null;
    protected SoundPlayer() {
        // Exists only to defeat instantiation.
    }
    public static SoundPlayer getInstance() {
        if(mInstance == null) {
            // not thread-safe, but not a concern for this app
            mInstance = new SoundPlayer();
        }
        return mInstance;
    }

    public void initialize(Context pContext) {
        AssetManager assetManager = pContext.getAssets();
        try {
            // If there's only 1 stream, there can be a clicking noise if you tap really fast.
            // Setting the max number to 2, and calling SoundPool.autoPause() in the play()
            // method eliminates the click.
            mSoundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
            String[] sounds = assetManager.list("sounds/data");
            for (String sound : sounds) {
                AssetFileDescriptor assetFileDescriptor = assetManager.openFd("sounds/data/"+sound);
                int soundId = mSoundPool.load(assetFileDescriptor,1);
                String name = sound.substring(0,sound.indexOf('.'));
                mSounds.put(name, soundId);
            }
        }
        catch (IOException e) {
            if (LOGGING_ENABLED) {
                Log.v(LOG_TAG, "IOException in SoundPlayer.initialize()");
            }
        }
    }


    public void play(String chord) {
        try {
            int soundID = mSounds.get(chord);
            // auto-pause prevents clicking sound when tapping very quickly
            mSoundPool.autoPause();
            // soundID, leftVolume, rightVolume, priority, loop, rate
            mSoundPool.play(soundID, 1f, 1f, 0, 0, 1);
        }
        catch (NullPointerException e) {
            if (LOGGING_ENABLED) {
                Log.e(LOG_TAG, "NullPointerException in SoundPlayer.play()");
            }
        }
    }

}