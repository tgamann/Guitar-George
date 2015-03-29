package com.maman.GuitarGeorge;

import android.content.ClipData;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.view.GestureDetector;
import java.util.ArrayList;
import java.util.List;
import static com.maman.GuitarGeorge.Constants.*;

/*
 * ComposeFragment:
 * Composition page that allows the user to select chords and arrange them on the display
 * in a chord progression (a.k.a. a song). Save button at the bottom stores the progression
 * in a file, which can be retrieved later from the LibFragment.
 */

public class ComposeFragment extends Fragment {
    private final ComposeFragmentState mComposeState = ComposeFragmentState.getInstance();
    private View mRootView = null;
    private String mChordBase;
    private View mDragStartView = null;
    private String mDragTargetViewName = DEFAULT_IMAGE_NAME;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.compose_fragment, container, false);

        /*
         * onCreateView() populates the ChordProgression object with ImageViews from this fragment.
         * When this fragment goes through an onDestroyView()-onCreateView() lifecycle loop, its
         * views get re-created, but the ChordProgression object still has references to the
         * old views. Since we'd like this fragment to return to the state it was in before it
         * got destroyed, here we save away that state so we can program the newly created views
         * to match it at the end of this call to onCreateView().
         */
        List<String> progressionState = mComposeState.getRawProgression(); // save the state
        mComposeState.clearProgression(); // remove old view references

        for (View v : getAllChildViews(mRootView)) {
            if (v.getTag() != null) {
                final String viewTag = v.getTag().toString();
                if (viewTag.contains(getResources().getText(R.string.chord_root_tag)))
                {
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickChordRootButton(v);
                        }
                    });
                }
                else if (viewTag.contains(getResources().getText(R.string.chord_variation_tag)))
                {
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickChordVariationButton(v);
                        }
                    });
                }
                else if (viewTag.contains(getResources().getText(R.string.repeat_tag)))
                {
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickRepeatButton();
                        }
                    });
                }
                else if (viewTag.contains(getResources().getText(R.string.selection_image_tag)))
                {
                    v.setOnTouchListener(new ChordTouchListener());
                }
                else if (viewTag.contains(getResources().getText(R.string.progression_image_tag)))
                {
                    v.setOnTouchListener(new ChordTouchListener());
                    v.setOnDragListener(new ChordDragListener());
                    Utils.setViewImage(v, DEFAULT_IMAGE_NAME);
                    mComposeState.addItemToProgression(v);
                }
                else if (viewTag.contains(getResources().getText(R.string.save_button_tag)))
                {
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DialogFragment saveDialog = new SaveDialogFragment();
                            saveDialog.show(getActivity().getSupportFragmentManager(), viewTag);
                        }
                    });
                }
                else if (viewTag.contains(getResources().getText(R.string.help_button_tag)))
                {
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DialogFragment helpDialog = new HelpDialogFragment();
                            helpDialog.show(getActivity().getSupportFragmentManager(), viewTag);
                        }
                    });
                }
            }
        }

        if (savedInstanceState != null) {
            // restore the chord selection from the saveInstanceState
            mChordBase = savedInstanceState.getString(CHORD_SELECTION_KEY);
            List<String> progressionViewValues =
                    savedInstanceState.getStringArrayList(CHORD_PROGRESSION_KEY);
            mComposeState.setProgression(progressionViewValues);
        }
        else if (!progressionState.isEmpty()) {
            // restore the chord selection from the state object
            mChordBase = mComposeState.getSelection();
            mComposeState.setProgression(progressionState);
        }
        else {
            mChordBase = "c";
        }

        UpdateSelections(mChordBase);

        return mRootView;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the current chord selection
        savedInstanceState.putString(CHORD_SELECTION_KEY,
                ComposeFragmentState.getInstance().getSelection());
        // Save the chord progression information
        ArrayList<String> saveInstance = new ArrayList<>();
        saveInstance.addAll(ComposeFragmentState.getInstance().getRawProgression());
        savedInstanceState.putStringArrayList(CHORD_PROGRESSION_KEY, saveInstance);

        // Call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    /*
     * Event handlers for defining a chord
     */
    private void onClickChordRootButton(View v) {
        // Button choices are Ab, A, Bb, B, C, C#, D, Eb, E, F, F#, G
        String base = ((Button)v).getText().toString();

        // Buttons labeled C# and F# correspond to image files named csharp*.png & fsharp*.png
        base = base.replaceFirst("#","sharp");

        // resource files cannot contain upper-case letters
        mChordBase = base.toLowerCase();

        // update the display to show the .png images for each voicing of the chord
        UpdateSelections(mChordBase);
    }
    private void onClickChordVariationButton(View v) {
        // Button choices are 5,6,7,9,min,min7,sus
        String variation = ((Button)v).getText().toString().toLowerCase();

        // update the display to show the .png images for each voicing of the chord
        UpdateSelections(mChordBase + variation);
    }
    private void onClickRepeatButton() {
        ShowRepeatSelections();
    }

    /*
     * Event Handlers for Chord ImageViews
     *
     * To detect gestures on an ImageView, use GestureDetector like this:
     * - Make your own GestureDetector, derived from SimpleOnGestureListener, and
     *   override the methods you're interested in.
     * - Setup a touch listener for your ImageViews and route the messages to your
     *   gesture detector:
     */
    private class ChordTouchListener implements View.OnTouchListener {
        private View mTouchTarget = null;
        private String mTag = null;

        private GestureDetector mDetector =
                new GestureDetector(null, new ViewGestureDetector());

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            try {
                mTouchTarget = v;
                mTag = v.getTag().toString();
                mDetector.onTouchEvent(event);
            }
            catch (Exception e) {
                if (LOGGING_ENABLED) {
                    Log.v(LOG_TAG, "Exception in ComposeFragment.onTouch()");
                }
            }
            return true; // return true if the listener has consumed the event
        }

        private class ViewGestureDetector extends GestureDetector.SimpleOnGestureListener {
            private final SoundPlayer mSoundPlayer = SoundPlayer.getInstance();
            @Override
            public boolean onDown(MotionEvent e) {
                mSoundPlayer.play(mTag);

                // Specify the data which is passed to the drop target via an instance of ClipData
                ClipData dragData = ClipData.newPlainText(mTag, mTag);

                // Instantiates the drag mShadow builder.
                View.DragShadowBuilder shadowBuilder = new ImageDragShadowBuilder(mTouchTarget);

                // Starts the drag
                mDragStartView = mTouchTarget;
                mTouchTarget.startDrag(dragData, shadowBuilder, mTouchTarget, 0);

                return true; // returning false causes the system to ignore the rest of the gesture
            }
        }

        private class ImageDragShadowBuilder extends View.DragShadowBuilder {
            private Drawable mShadow;

            ImageDragShadowBuilder(View v) {
                super(v);
                mShadow = ((ImageView)v).getDrawable();
            }

            @Override
            public void onProvideShadowMetrics(@NonNull Point shadowSize,
                                               @NonNull Point shadowTouchPoint) {
                // Defines local variables
                int width, height;

                // Sets the width & height of the shadow to the size of the original View
                width = getView().getWidth();
                height = getView().getHeight();

                // The drag shadow is a Drawable. This sets its dimensions to be the same as the
                // Canvas that the system will provide. As a result, the drag shadow will fill the
                // Canvas.
                mShadow.setBounds(0, 0, width, height);

                // Sets the size parameter's width and height values. These get back to the system
                // through the size parameter.
                shadowSize.set(width, height);

                // Sets the touch point's position to be in the middle of the drag mShadow
                shadowTouchPoint.set(width/2, height/2);
            }

            @Override
            public void onDrawShadow(@NonNull Canvas canvas) {
                mShadow.draw(canvas);
            }
        }
    }

    private class ChordDragListener implements View.OnDragListener {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            final int action = event.getAction();
            switch(action) {
                case DragEvent.ACTION_DRAG_STARTED:
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    // When the dragged view enters a target, we switch the target to the dragged
                    // image. If the dragged image later leaves the target without being dropped,
                    //  we need to switch the image back. The target is saved here so we know what
                    // to switch it back to.
                    mDragTargetViewName = v.getTag().toString();

                    // Switch the target to the dragged view.
                    Utils.setViewImage(v, event.getClipDescription().getLabel().toString());
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    // Dragged view left the target.
                    if (mDragStartView.equals(v)) {
                        // Drag was started from this progression box and dragged out of it;
                        // remove the view's image.
                        Utils.setViewImage(v, DEFAULT_IMAGE_NAME);
                    }
                    else {
                        // We just dragged an image from another source through this progression
                        // box; restore this view to what it was prior to having the image
                        // dragged into it.
                        Utils.setViewImage(v, mDragTargetViewName);
                    }
                    break;
                case DragEvent.ACTION_DROP:
                    // note for future reference: this is the only event action case where
                    // event.getClipData() will return a non-null value.
                    Utils.setViewImage(v, event.getClipDescription().getLabel().toString());
                    break;
            }
            return true;
        }
    }

    /*
     * Helper method for event handlers above.
     * Populates chord selectionImages with one or more chord voicings
     */
    private void UpdateSelections(String chord)
    {
        // current selection is saved so we can restore the state of the ComposeFragment
        mComposeState.setSelection(chord);

        // Each chord can have up to 4 voicings; the corresponding image and sound
        // file names are <chord>, <chord>v1, & <chord>v2
        for (int i = 0; i < MAX_VOICINGS; i++) {
            String voicing = chord;
            ImageView imageView = (ImageView) mRootView.findViewById(R.id.selection_image1+i);
            if (i > 0) {
                voicing += "v" + Integer.toString(i);
            }
            imageView.setImageResource(
                    getResources().getIdentifier(voicing, "drawable", PACKAGE_NAME));
            imageView.setTag(voicing);
        }
    }
    /*
     * Helper method for event handler above.
     * Populates chord selectionImages with begin_repeat symbols
     */
    private void ShowRepeatSelections()
    {
        // Each chord can have up to 4 voicings; the corresponding image and sound
        // file names are <chord>, <chord>v1, & <chord>v2
        for (int i = 0; i < MAX_VOICINGS; i++) {
            String symbol;
            ImageView imageView = (ImageView) mRootView.findViewById(R.id.selection_image1+i);
            if (i == 0) {
                symbol = BEGIN_REPEAT;
            }
            else {
                symbol = END_REPEAT + Integer.toString(i);
            }
            imageView.setImageResource(getResources().getIdentifier(symbol, "drawable", PACKAGE_NAME));
            imageView.setTag(symbol);
        }
    }

    /*
     * Helper method for traversing a View hierarchy;
     * returns all leaf View elements of a View tree.
     */
    private List<View> getAllChildViews(View v) {
        List<View> viewArrayList = new ArrayList<>();
        if (!(v instanceof ViewGroup)) {
            // we've reached the end (no more children); just return this view
            viewArrayList.add(v);
        }
        else {
            // the view argument passed in has children
            ViewGroup viewGroup = (ViewGroup)v;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                viewArrayList.addAll(getAllChildViews(child));
            }
        }
        return viewArrayList;
    }

}
