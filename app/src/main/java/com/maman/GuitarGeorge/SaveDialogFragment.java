package com.maman.GuitarGeorge;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import java.util.regex.Pattern;

/*
 * Pop-up dialog for saving a progression to file storage.
 */
public class SaveDialogFragment extends DialogFragment {
    EditText mNameInput = null;
    Button mPositiveButton = null;

    public interface SaveDialogListener {
        public void onDialogSaveClick(String songName);
    }
    // Use this instance of the interface to deliver action events
    SaveDialogListener mListener;

    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity(),
                R.style.CustomDialogTheme);
        mNameInput = new EditText(getActivity());
        mNameInput.setHint(R.string.save_dialog_hint);
        mNameInput.addTextChangedListener(new TextInputWatcher(mNameInput));
        alertDialogBuilder.setView(mNameInput);

        alertDialogBuilder.setPositiveButton(R.string.save_button_text, new SaveOnClickListener());
        alertDialogBuilder.setNegativeButton(R.string.cancel_button_text, new CancelOnClickListener());

        final AlertDialog alertDialog = alertDialogBuilder.create();

        // FocusChangeListener makes the keyboard pop-up without having to touch the EditText box.
        mNameInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    alertDialog.getWindow().
                       setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        return alertDialog;
    }

    @Override
    public void onAttach(Activity activity) {
        // Override onAttach() to instantiate the SaveDialogListener
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (SaveDialogListener)activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement SaveDialogListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog alertDialog = (AlertDialog) getDialog();
        if (alertDialog != null) {
            mPositiveButton = alertDialog.getButton(Dialog.BUTTON_POSITIVE);
            mPositiveButton.setEnabled(false);
        }
    }

    class SaveOnClickListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            // User has entered a name for the song and clicked "Save"; use the callback
            // to send the song name back to the ComposeFragment and dismiss this dialog.
            mListener.onDialogSaveClick(mNameInput.getText().toString());
            dismiss();
        }
    }
    class CancelOnClickListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dismiss();
        }
    }

    class TextInputWatcher implements TextWatcher {
        private EditText editText;

        public TextInputWatcher(EditText editText) {
            this.editText = editText;
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Auto-generated stub
        }
        @Override
        public void onTextChanged(CharSequence ss, int start, int before, int count) {
            String name = editText.getText().toString();
            int length = name.length();

            if (length > 0 && !Pattern.matches("^[_a-zA-Z]+[_a-zA-Z0-9-]*", name)) {
                editText.setText(name.substring(0, length - 1));
                editText.setSelection(length - 1);
            }
        }
        @Override
        public void afterTextChanged(Editable s) {
            if (mPositiveButton != null) {
                if (editText.getText().length() > 0) {
                    mPositiveButton.setEnabled(true);
                }
                else {
                    mPositiveButton.setEnabled(false);
                }
            }
        }
    }

}
