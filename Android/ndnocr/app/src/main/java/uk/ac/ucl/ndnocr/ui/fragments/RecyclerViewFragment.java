/*
* Copyright (C) 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package uk.ac.ucl.ndnocr.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;

import uk.ac.ucl.kbapp.utils.Config;
import uk.ac.ucl.ndnocr.R;
import uk.ac.ucl.ndnocr.data.Content;
import uk.ac.ucl.ndnocr.data.DatabaseHandler;
import uk.ac.ucl.ndnocr.data.NdnOcrService;
import uk.ac.ucl.ndnocr.utils.G;
import uk.ac.ucl.ndnocr.utils.PathUtil;

/**
 * Demonstrates the use of {@link RecyclerView} with a {@link LinearLayoutManager} and a
 * {@link GridLayoutManager}.
 */
public class RecyclerViewFragment extends Fragment {

    private static final String TAG = "RecyclerViewFragment";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;
    private static final int DATASET_COUNT = 60;
    private static final int CHOOSE_FILE_REQUESTCODE = 42;

    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    protected LayoutManagerType mCurrentLayoutManagerType;
    BroadcastReceiver mBroadcastReceiver;

    protected Button mCameraButton;
    protected Button mLoadButton;

    protected RecyclerView mRecyclerView;
    protected CustomAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected String[] mDataset;
    DatabaseHandler db;
    public static RecyclerViewFragment newInstance() {
        return new RecyclerViewFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DatabaseHandler(getActivity());

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                G.Log(TAG,"onReceive:" + intent);

                switch (intent.getAction()) {
                    case NdnOcrService.NEW_RESULT:
                        /*Fragment currentFragment = getFragmentManager().findFragmentByTag("ndnocr-1");
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction.detach(currentFragment);
                        fragmentTransaction.attach(currentFragment);
                        fragmentTransaction.commit();*/
                        mAdapter.setContent(db.getContent());
                        mAdapter.notifyDataSetChanged();

                        break;

                }
            }
        };
        // Initialize dataset, this data would usually come from a local content provider or
        // remote server.
        initDataset();
    }

    @Override
    public void onResume(){
        getContext().registerReceiver(mBroadcastReceiver, new IntentFilter(NdnOcrService.NEW_RESULT));
        super.onResume();
    }


    @Override
    public void onPause(){
        getContext().unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.recycler_view_frag, container, false);
        rootView.setTag(TAG);

        // BEGIN_INCLUDE(initializeRecyclerView)
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new LinearLayoutManager(getActivity());

        mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mCurrentLayoutManagerType = (LayoutManagerType) savedInstanceState
                    .getSerializable(KEY_LAYOUT_MANAGER);
        }
        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);

        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // END_INCLUDE(initializeRecyclerView)
        mCameraButton = rootView.findViewById(R.id.camera);
        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment newFragment = new Camera2BasicFragment();
                //fragment =
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.main_fragment_container, newFragment, "camera")
                        .commit();
            }
        });

        mLoadButton = rootView.findViewById(R.id.load);
        mLoadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFile("image/*");

            }
        });


        return rootView;
    }

    /**
     * Set RecyclerView's LayoutManager to the one given.
     *
     * @param layoutManagerType Type of layout manager to switch to.
     */
    public void setRecyclerViewLayoutManager(LayoutManagerType layoutManagerType) {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        switch (layoutManagerType) {
            case GRID_LAYOUT_MANAGER:
                mLayoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);
                mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
                break;
            case LINEAR_LAYOUT_MANAGER:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
                break;
            default:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(scrollPosition);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save currently selected layout manager.
        savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Generates Strings for RecyclerView's adapter. This data would usually come
     * from a local content provider or remote server.
     */
    private void initDataset() {
        mDataset = new String[0];
        /*for (int i = 0; i < DATASET_COUNT; i++) {
            mDataset[i] = "This is element #" + i;
        }
        */
        File dir = getContext().getFilesDir();
        File[] subFiles = dir.listFiles();

        List<Content> content = db.getContent();

        G.Log(TAG,"Files " +subFiles.length +" "+db.getContentCount()+" "+content.size());


        mAdapter = new CustomAdapter(db.getContent());

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == CHOOSE_FILE_REQUESTCODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                G.Log(TAG, "Uri: " + uri.toString() + " " +uri.getPath() +" "+uri.getEncodedPath());
                saveFile(uri);

            }
        }
    }
    private void saveFile(Uri path)
    {
        try {
            File mFile = new File(getActivity().getFilesDir(), "pic"+System.currentTimeMillis()+".jpg");

            (new Handler()).post(new ImageSaver(path, mFile,getContext()));

            Content c = new Content(mFile.getName(), Config.prefix+mFile.getName(),"Image to be processed",mFile.getAbsolutePath());
            DatabaseHandler db = new DatabaseHandler(getContext());
            db.addContent(c);
            //db.setContentDownloaded(c.getUri());

        }catch (Exception e){
            G.Log("Exception "+e);}
    }
    private void openFile(String minmeType) {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(minmeType);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // special intent for Samsung file manager
        Intent sIntent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
        // if you want any file type, you can skip next line
        sIntent.putExtra("CONTENT_TYPE", minmeType);
        sIntent.addCategory(Intent.CATEGORY_DEFAULT);

        Intent chooserIntent;
        if (getContext().getPackageManager().resolveActivity(sIntent, 0) != null){
            // it is device with samsung file manager
            chooserIntent = Intent.createChooser(sIntent, "Open file");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { intent});
        }
        else {
            chooserIntent = Intent.createChooser(intent, "Open file");
        }

        try {
            startActivityForResult(chooserIntent, CHOOSE_FILE_REQUESTCODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getContext(), "No suitable File Manager was found.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Saves a JPEG {@link Image} into the specified {@link File}.
     */
    private static class ImageSaver implements Runnable {

        /**
         * The JPEG image
         */
        //private final Image mImage;
        /**
         * The file we save the image into.
         */
        private final File mFile;
        private final Uri mName;
        FileOutputStream output = null;
        Context mContext;

        ImageSaver(Uri name,File file,Context context) {
            mFile = file;
            mName = name;
            mContext = context;
        }

        @Override
        public void run() {
            try {
                File inputFile = new File(PathUtil.getPath(mContext,mName));
                long fileSize = inputFile.length();
                G.Log("Filename "+inputFile);
                InputStream inputStream = new FileInputStream(inputFile);
                byte[] buf = new byte[(int) fileSize];
                inputStream.read(buf);
                //ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
                //byte[] bytes = new byte[buffer.remaining()];
                //buffer.get(buf);
                    output = new FileOutputStream(mFile);
                    output.write(buf);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (URISyntaxException ue) {
                ue.printStackTrace();
                }finally  {
                   // mImage.close();
                    if (null != output) {
                        try {
                            output.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

        }

    }

}
