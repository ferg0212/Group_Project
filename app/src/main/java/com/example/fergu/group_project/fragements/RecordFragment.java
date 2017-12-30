package com.example.fergu.group_project.fragements;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fergu.group_project.R;
import com.example.fergu.group_project.RecordingService;
import com.melnykov.fab.FloatingActionButton;

import java.io.File;

/**
 * Created by fergu on 2017-12-28.
 */

public class RecordFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_POSITION = "position";
    private static final String LOG_TAG = RecordFragment.class.getSimpleName();

    private int position;

    //Recording controls
    private FloatingActionButton memoRecordButton = null;
    private TextView memoRecordingPrompt;
    private int promptCount = 0;
    private boolean startRecordingBool = true;
    private Chronometer chronometerObject = null;
    long timePaused = 0; //stores time when user clicks pause button


    public RecordFragment() {
    }

    public static RecordFragment newInstance(int position) {
        RecordFragment f = new RecordFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View recordView = inflater.inflate(R.layout.fragment_record_memo, container, false);

        chronometerObject = (Chronometer) recordView.findViewById(R.id.chronometer);
        //update recording prompt text
        memoRecordingPrompt = (TextView) recordView.findViewById(R.id.recording_status_text);
        memoRecordButton = (FloatingActionButton) recordView.findViewById(R.id.btnRecord);
        memoRecordButton.setColorNormal(getResources().getColor(R.color.primary));
        memoRecordButton.setColorPressed(getResources().getColor(R.color.primary_dark));
        memoRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordButtonPressed(startRecordingBool);
                startRecordingBool = !startRecordingBool;
            }
        });
        return recordView;
    }

    // Recording Start/Stop
    private void recordButtonPressed(boolean start){

        Intent intent = new Intent(getActivity(), RecordingService.class);

        if (start) {
            // start recording
            memoRecordButton.setImageResource(R.drawable.ic_media_stop);
            Toast.makeText(getActivity(),R.string.toast_recording_start,Toast.LENGTH_SHORT).show();
            File folder = new File(Environment.getExternalStorageDirectory() + "/SoundRecorder");
            if (!folder.exists()) {
                //folder /SoundRecorder doesn't exist, create the folder
                folder.mkdir();
            }
            //start Chronometer
            chronometerObject.setBase(SystemClock.elapsedRealtime());
            chronometerObject.start();
            chronometerObject.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                @Override
                public void onChronometerTick(Chronometer chronometer) {
                    if (promptCount == 0) {
                        memoRecordingPrompt.setText(getString(R.string.record_in_progress) + ".");
                    } else if (promptCount == 1) {
                        memoRecordingPrompt.setText(getString(R.string.record_in_progress) + "..");
                    } else if (promptCount == 2) {
                        memoRecordingPrompt.setText(getString(R.string.record_in_progress) + "...");
                        promptCount = -1;
                    }
                    promptCount++;
                }
            });
            //start RecordingService
            getActivity().startService(intent);
            //keep screen on while recording
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            memoRecordingPrompt.setText(getString(R.string.record_in_progress) + ".");
            promptCount++;
        } else {
            //stop recording
            memoRecordButton.setImageResource(R.drawable.ic_mic_white_36dp);
            chronometerObject.stop();
            chronometerObject.setBase(SystemClock.elapsedRealtime());
            timePaused = 0;
            memoRecordingPrompt.setText(getString(R.string.record_prompt));
            getActivity().stopService(intent);
            //allow the screen to turn off again once recording is finished
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

}