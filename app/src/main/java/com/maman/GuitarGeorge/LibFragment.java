package com.maman.GuitarGeorge;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Arrays;
import static com.maman.GuitarGeorge.Constants.*;

/*
 * Class for managing our list of "compositions" (a.k.a. songs, a.k.a. chord progressions).
 */
public class LibFragment extends Fragment {
    private final SongList mSongList = SongList.getInstance();
    private String mSongSelection;
    private View mCurrentSelection = null;
    private ArrayAdapter<String> mAdapter;
    private Button mLoadButton;
    private Button mDeleteButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.lib_fragment, container, false);

        // Fill in the ListView with the list of songs read in from file at start-up
        mSongList.initialize(getActivity());
        // Define a new Adapter; params = Context, Layout for the row,
        // ID of the TextView to which the data is written, & array of data
        mAdapter = new ArrayAdapter<>(rootView.getContext(),
                android.R.layout.simple_list_item_1, android.R.id.text1, mSongList.getSongArray());
        // Assign adapter to ListView
        final ListView songListView = (ListView) rootView.findViewById(R.id.songList);
        songListView.setAdapter(mAdapter);

        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mCurrentSelection != null) {
                    // Un-highlight the previous selection
                    mCurrentSelection.setBackgroundColor(Color.TRANSPARENT);
                }
                // Highlight the new selection
                view.setBackgroundColor(Color.GRAY);
                mCurrentSelection = view;

                // Retrieve the selected item value
                mSongSelection = (String) songListView.getItemAtPosition(position);

                mLoadButton.setEnabled(true);
                mDeleteButton.setEnabled(true);
            }
        });

        mLoadButton = (Button) rootView.findViewById(R.id.loadButton);
        mLoadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Look up selection in Utils song map
                if (mSongList.songIsInLibrary(mSongSelection)) {
                    String songContents = mSongList.getSong(mSongSelection);
                    String[] songItems = songContents.split(",");
                    // Load song into the chord progression
                    ComposeFragmentState composeState = ComposeFragmentState.getInstance();
                    composeState.setProgression(Arrays.asList(songItems));
                    Toast.makeText(getActivity(), getText(R.string.loaded_text)
                                    + " " + mSongSelection, Toast.LENGTH_SHORT).show();
                }
                // Leave the Library page; navigate back.
                ((MainActivity)getActivity()).getSupportActionBar().
                        setSelectedNavigationItem(Utils.getPreviousTab());
            }
        });

        mDeleteButton = (Button) rootView.findViewById(R.id.deleteButton);
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pop-up confirm delete dialog (e.g. Are You Sure?)
                DialogFragment deleteDialog = new DeleteDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putString(SONG_NAME_KEY, mSongSelection);
                deleteDialog.setArguments(bundle);
                deleteDialog.show(getActivity().getSupportFragmentManager(), v.toString());
            }
        });

        // disable buttons until something in the list is selected; otherwise the delete confirm
        // dialog could say "Are You Sure You Want to Delete "null"?"
        mLoadButton.setEnabled(false);
        mDeleteButton.setEnabled(false);

        return rootView;
    }

    public void updateListView() {
        mAdapter.clear();
        mCurrentSelection.setBackgroundColor(Color.TRANSPARENT);
        mCurrentSelection = null;
        mSongSelection = null;
        mAdapter.addAll(mSongList.getSongArray());
        mAdapter.notifyDataSetChanged();
    }

}
