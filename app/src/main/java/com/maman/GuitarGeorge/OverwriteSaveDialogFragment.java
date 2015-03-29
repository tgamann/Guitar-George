package com.maman.GuitarGeorge;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import static com.maman.GuitarGeorge.Constants.SONG_NAME_KEY;

/*
 * When the user tries to putSong a composition and gives it a name that already exists in
 * their stored list, this dialog pops up and asks if they want to overwrite or cancel.
 */
public class OverwriteSaveDialogFragment extends DialogFragment {
    private String mSongName;

    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        mSongName = bundle.getString(SONG_NAME_KEY);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity(),
                R.style.CustomDialogTheme);
        alertDialogBuilder.setMessage("\"" + mSongName + "\" "+ getString(R.string.already_exists));
        alertDialogBuilder.setPositiveButton(R.string.ok_button_text, new SaveOnClickListener());
        alertDialogBuilder.setNegativeButton(R.string.cancel_button_text, new CancelOnClickListener());

        return alertDialogBuilder.create();
    }
    class SaveOnClickListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            // User has elected to over-write existing song in the library file.
            SongList songList = SongList.getInstance();
            songList.removeSong(mSongName);
            songList.putSong(getActivity(), mSongName);
            dismiss();
        }
    }
    class CancelOnClickListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dismiss();
        }
    }
}
