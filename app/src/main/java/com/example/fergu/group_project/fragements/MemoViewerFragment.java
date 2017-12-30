package com.example.fergu.group_project.fragements;

import android.os.Bundle;
import android.os.FileObserver;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.fergu.group_project.R;
import com.example.fergu.group_project.adapters.MemoViewerAdapter;


/**
 * Created by fergu on 2017-12-28.
 */

public class MemoViewerFragment extends Fragment{
    private static final String ARG_POSITION = "position";
    private static final String LOG_TAG = "MemoViewerFragment";
    private int position;
    private MemoViewerAdapter memoViewAdapter;
    FileObserver observer =
            new FileObserver(android.os.Environment.getExternalStorageDirectory().toString()
                    + "/SoundRecorder") {
                @Override
                public void onEvent(int event, String file) {
                    if(event == FileObserver.DELETE){

                        String filePath = android.os.Environment.getExternalStorageDirectory().toString()
                                + "/SoundRecorder" + file + "]";

                        Log.d(LOG_TAG, "File deleted ["
                                + android.os.Environment.getExternalStorageDirectory().toString()
                                + "/SoundRecorder" + file + "]");

                        memoViewAdapter.removeOutOfApp(filePath);
                    }
                }
            };

    public static MemoViewerFragment newInstance(int position) {
        MemoViewerFragment f = new MemoViewerFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
        observer.startWatching();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_memo_viewer, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        memoViewAdapter = new MemoViewerAdapter(getActivity(), layoutManager);
        recyclerView.setAdapter(memoViewAdapter);

        return view;
    }
}

