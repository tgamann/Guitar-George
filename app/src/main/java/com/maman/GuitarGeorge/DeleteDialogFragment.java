package com.maman.GuitarGeorge;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import static com.maman.GuitarGeorge.Constants.*;

/*
 * Dialog to confirm the user wants to delete an entry in their library.
 */
public class DeleteDialogFragment extends DialogFragment {
    String mSongName;

    public interface DeleteDialogListener {
        public void onDialogDeleteClick(String songName);
    }
    // Use this instance of the interface to deliver action events
    DeleteDialogListener mListener;

    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        mSongName = bundle.getString(SONG_NAME_KEY);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity(),
                R.style.CustomDialogTheme);
        alertDialogBuilder.setMessage(getString(R.string.are_you_sure) + " \"" + mSongName + "\"");

        alertDialogBuilder.setPositiveButton(R.string.delete_button_text,
                new DeleteOnClickListener());
        alertDialogBuilder.setNegativeButton(R.string.cancel_button_text,
                new CancelOnClickListener());

        return alertDialogBuilder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        // Override onAttach() to instantiate the SaveDialogListener
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (DeleteDialogListener)activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement DeleteDialogListener");
        }
    }

    class DeleteOnClickListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            // Use has entered a name for the song and clicked "Save"; use the callback
            // to send the song name back to the ComposeFragment and dismiss this dialog.
            mListener.onDialogDeleteClick(mSongName);
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
