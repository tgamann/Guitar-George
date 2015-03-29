package com.maman.GuitarGeorge;

import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import static com.maman.GuitarGeorge.Constants.*;

/*
 * Class to manage the Chord Progression ImageViews on the Compose page, provide the
 * filtered & expanded chord sequence for the Play page, and provide a String representation
 * for the Library page.
 */
public class ComposeFragmentState {
    private String mSelection;
    private final List<View> mProgression = new ArrayList<>();

    private static ComposeFragmentState mInstance = null;
    protected ComposeFragmentState() {
        // Exists only to defeat instantiation.
    }
    public static ComposeFragmentState getInstance() {
        if(mInstance == null) {
            // not thread-safe, but not a concern for this app
            mInstance = new ComposeFragmentState();
        }
        return mInstance;
    }

    public void setSelection(String selection) {
        mSelection = selection;
    }
    public String getSelection() {
        return mSelection;
    }

    public void clearProgression() {
        mProgression.clear();
    }
    public void setProgression(List<String> stringList) {
        int viewIndex = 0;
        for (String item : stringList) {
            Utils.setViewImage(mProgression.get(viewIndex++), item);
        }
    }
    public void addItemToProgression(View imageView) {
        mProgression.add(imageView);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (View v : mProgression) {
            stringBuilder.append(v.getTag().toString());
            // separate chords with comma delimiter
            stringBuilder.append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(","));
        return stringBuilder.toString();
    }

    public List<String> getRawProgression() {
        List<String> retList = new ArrayList<>();
        for (View v : mProgression) {
            retList.add(v.getTag().toString());
        }
        return retList;
    }

    public List<String> getProcessedProgression() {
        List<String> filteredList = FilterProgression(mProgression.iterator());
        return expandProgression(filteredList.iterator());
    }

    /*
     *   Filters out unused view items in a chord progression, as well as mis-matched
     *   begin & end repeat markers.
     */
    private List<String> FilterProgression(Iterator<View> iter) {
        List<View> filteredList = new ArrayList<>();
        Stack<View> begin_repeat_stack = new Stack<>();

        while (iter.hasNext()) {
            View item = iter.next();
            String tag = item.getTag().toString();
            if (tag.contains(BEGIN_REPEAT)) {
                begin_repeat_stack.push(item);
                filteredList.add(item);
            }
            else if (tag.contains(END_REPEAT)) {
                // We should have a prior "begin_repeat" to match this "end_repeat"; if not, then
                // disregard this end_repeat item; otherwise, match it up to a begin_repeat and add
                // it to the filtered list.
                if (!begin_repeat_stack.empty()) {
                    begin_repeat_stack.pop();
                    filteredList.add(item);
                }
            }
            else if (!tag.contains(DEFAULT_IMAGE_NAME)) {
                // item is not the default image, so it must contain a chord; add it to the list.
                filteredList.add(item);
            }
        }

        // See if there are any begin_repeat items that did not have a corresponding end_repeat.
        // If so, remove them from the returned list.
        while (!begin_repeat_stack.empty()) {
            View begin_repeat = begin_repeat_stack.pop();
            filteredList.remove(begin_repeat);
        }

        // Return a List of Strings (instead of a List of Views)
        List<String> retList = new ArrayList<>();
        for (View v : filteredList) {
            retList.add(v.getTag().toString());
        }
        return retList;
    }

    /*
     * Recursive routine to expand repeat blocks in the list. This routine gets called after
     * FilterProgression() has been called, which means that iter refers to a list with no
     * un-matched begin/end_repeat items.
     */
    private List<String> expandProgression(Iterator<String> iter) {
        List<String> retList = new ArrayList<>();

        while (iter.hasNext()) {
            String item = iter.next();
            if (item.contains(BEGIN_REPEAT)) {
                retList.addAll(expandProgression(iter));
            }
            else if (item.contains(END_REPEAT)) {
                List<String> repeatList = new ArrayList<>();
                repeatList.addAll(retList);
                int repeatCount = Integer.parseInt(item.substring(item.length() - 1));

                for (int i = 0; i < repeatCount; i++) {
                    retList.addAll(repeatList);
                }
            }
            else {
                retList.add(item);
            }
        }
        return retList;
    }

}
