package com.maman.GuitarGeorge;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.FragmentTransaction;
import static com.maman.GuitarGeorge.Constants.*;

public class MainActivity extends ActionBarActivity implements
         SaveDialogFragment.SaveDialogListener, DeleteDialogFragment.DeleteDialogListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup action bar for tabs
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);

        actionBar.addTab(actionBar.newTab().setText(R.string.compose)
                .setTabListener(new TabListener<>(this, "Compose", ComposeFragment.class)));
        actionBar.addTab(actionBar.newTab().setText(R.string.play)
                .setTabListener(new TabListener<>(this, "Play", PlayFragment.class)));
        actionBar.addTab(actionBar.newTab().setText(R.string.library)
                .setTabListener(new TabListener<>(this, "Library", LibFragment.class)));

        // load the .wav files
        SoundPlayer.getInstance().initialize(this);

        // set global constant
        PACKAGE_NAME = this.getPackageName();

        // set hardware volume key to control the stream this app uses.
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }


    @Override
    public void onDialogSaveClick(String songName) {
        // check for duplicate name; show alert if duplicate; otherwise
        // putSong the composeFragment's progression as songName.
        SongList songList = SongList.getInstance();
        songList.putSong(this, songName);
     }

    @Override
    public void onDialogDeleteClick(String songName) {
        SongList songList = SongList.getInstance();
        // Remove from Utils song map
        songList.removeSong(songName);
        // Write the file
        songList.update(this);

        // Delete from the LibFragment's ListView
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("Library");
        if (fragment != null) {
            LibFragment libFragment = (LibFragment) fragment;
            libFragment.updateListView();
        }
    }

    private class TabListener<T extends Fragment> implements ActionBar.TabListener {
        private Fragment mFragment;
        private final Activity mActivity;
        private final String mTag;
        private final Class<T> mClass;

        /* Constructor used each time a new tab is created.
         * @param activity  The host Activity, used to instantiate the fragment
         * @param tag  The identifier tag for the fragment
         * @param clz  The fragment's Class, used to instantiate the fragment
         */
        public TabListener(Activity activity, String tag, Class<T> clz) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
        }

        /*
         * ActionBar.TabListener callbacks
         */
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            // Check if the fragment is already initialized
            if (mFragment == null) {
                // If not, instantiate and add it to the activity
                mFragment = Fragment.instantiate(mActivity, mClass.getName());
                ft.add(android.R.id.content, mFragment, mTag);
            } else {
                // If it exists, simply attach it in order to show it
                ft.attach(mFragment);
            }
        }

        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                // Store the tab we are leaving. After user loads a song from the
                // Library (LibFragment), we take them back to either the Compose
                // or Play page.
                Utils.setPreviousTab(tab.getPosition());
                // Detach the fragment, because another one is being attached
                ft.detach(mFragment);
            }
        }

        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            // User selected the already selected tab. Usually do nothing.
        }
    }

}
