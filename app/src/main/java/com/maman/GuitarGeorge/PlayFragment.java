package com.maman.GuitarGeorge;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;

/*
 * The PlayFragment allows the user to "play" a chord progression by tapping on the guitar
 * image to sound the chord and tapping on the chord diagram to advance to the next chord.
 */
public class PlayFragment extends Fragment {

    private View mChordImage = null;
    private String mChordToPlay;
    private List<String> mPlayProgression = null;
    private int mNumChords = 0;
    private int mProgIndex = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.play_fragment, container, false);

        mChordImage = rootView.findViewById(R.id.chordImage);
        View guitarImage = rootView.findViewById(R.id.guitarImage);

        mChordImage.setOnTouchListener(new ChordTouchListener());
        guitarImage.setOnTouchListener(new GuitarTouchListener());

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // get the song
        mPlayProgression = ComposeFragmentState.getInstance().getProcessedProgression();
        mNumChords = mPlayProgression.size();
        if (mNumChords == 0) {
            // chord progression is empty - default to have a single c chord
            mPlayProgression.add("c");
            mNumChords = 1;
        }
        Utils.setViewImage(mChordImage, mPlayProgression.get(0));
        mChordToPlay = mChordImage.getTag().toString();
    }

    private class ChordTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // switch to the next chord in the progression
                mProgIndex = (mProgIndex + 1) % mNumChords;
                Utils.setViewImage(mChordImage, mPlayProgression.get(mProgIndex));
                mChordToPlay = mPlayProgression.get(mProgIndex);
            }
            return true; // return true if the listener has consumed the event
        }
    }

    private class GuitarTouchListener implements View.OnTouchListener {
        private final SoundPlayer mSoundPlayer = SoundPlayer.getInstance();
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mSoundPlayer.play(mChordToPlay);
            }
            return true; // return true if the listener has consumed the event
        }
    }

}