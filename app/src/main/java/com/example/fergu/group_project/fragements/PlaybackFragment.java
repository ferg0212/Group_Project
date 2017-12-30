package com.example.fergu.group_project.fragements;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.fergu.group_project.R;
import com.example.fergu.group_project.RecordingMemo;
import com.melnykov.fab.FloatingActionButton;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by fergu on 2017-12-28.
 */

public class PlaybackFragment extends DialogFragment {

    private static final String LOG_TAG = "PlaybackFragment";
    private static final String ARG_ITEM = "recording_item";
    private RecordingMemo memo;
    private Handler memoHandler = new Handler();
    private SeekBar timeSeekBar = null;
    private FloatingActionButton playMemoButton = null;
    private boolean isPlaying = false;
    private MediaPlayer memoPlayer = null;
    private TextView currentProgressTextView = null;
    private TextView fileNameTextView = null;
    private TextView fileLengthTextView = null;
    long minutes = 0;
    long seconds = 0;

    public PlaybackFragment newInstance(RecordingMemo item) {
        PlaybackFragment f = new PlaybackFragment();
        Bundle b = new Bundle();
        b.putParcelable(ARG_ITEM, item);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        memo = getArguments().getParcelable(ARG_ITEM);
        long itemDuration = memo.getLength();
        minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration);
        seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration)
                - TimeUnit.MINUTES.toSeconds(minutes);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_memo_player, null);

        fileNameTextView = (TextView) view.findViewById(R.id.file_name_text_view);
        fileLengthTextView = (TextView) view.findViewById(R.id.file_length_text_view);
        currentProgressTextView = (TextView) view.findViewById(R.id.current_progress_text_view);

        timeSeekBar = (SeekBar) view.findViewById(R.id.seekbar);
        ColorFilter filter = new LightingColorFilter
                (getResources().getColor(R.color.primary), getResources().getColor(R.color.primary));
        timeSeekBar.getProgressDrawable().setColorFilter(filter);
        timeSeekBar.getThumb().setColorFilter(filter);

        timeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(memoPlayer != null && fromUser) {
                    memoPlayer.seekTo(progress);
                    memoHandler.removeCallbacks(runnable);

                    long minutes = TimeUnit.MILLISECONDS.toMinutes(memoPlayer.getCurrentPosition());
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(memoPlayer.getCurrentPosition())
                            - TimeUnit.MINUTES.toSeconds(minutes);
                    currentProgressTextView.setText(String.format("%02d:%02d", minutes,seconds));

                    updateSeekBar();

                } else if (memoPlayer == null && fromUser) {
                    setMediaPlayerToPoint(progress);
                    updateSeekBar();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(memoPlayer != null) {
                    // remove message Handler from updating progress bar
                    memoHandler.removeCallbacks(runnable);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (memoPlayer != null) {
                    memoHandler.removeCallbacks(runnable);
                    memoPlayer.seekTo(seekBar.getProgress());

                    long minutes = TimeUnit.MILLISECONDS.toMinutes(memoPlayer.getCurrentPosition());
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(memoPlayer.getCurrentPosition())
                            - TimeUnit.MINUTES.toSeconds(minutes);
                    currentProgressTextView.setText(String.format("%02d:%02d", minutes,seconds));
                    updateSeekBar();
                }
            }
        });

        playMemoButton = (FloatingActionButton) view.findViewById(R.id.fab_play);
        playMemoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMemo(isPlaying);
                isPlaying = !isPlaying;
            }
        });

        fileNameTextView.setText(memo.getName());
        fileLengthTextView.setText(String.format("%02d:%02d", minutes,seconds));

        builder.setView(view);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return builder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        //set transparent background
        Window window = getDialog().getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);

        //disable buttons from dialog
        AlertDialog alertDialog = (AlertDialog) getDialog();
        alertDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
        alertDialog.getButton(Dialog.BUTTON_NEGATIVE).setEnabled(false);
        alertDialog.getButton(Dialog.BUTTON_NEUTRAL).setEnabled(false);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (memoPlayer != null) {
            stopMemo();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (memoPlayer != null) {
            stopMemo();
        }
    }

    // Play start/stop
    private void playMemo(boolean isPlaying){
        if (!isPlaying) {
            //currently MediaPlayer is not playing audio
            if(memoPlayer == null) {
                startPlayingMemo(); //start from beginning
            } else {
                resumeMemo(); //resume the currently paused MediaPlayer
            }

        } else {
            //pause the MediaPlayer
            pauseMemo();
        }
    }

    private void startPlayingMemo() {
        playMemoButton.setImageResource(R.drawable.ic_media_pause);
        memoPlayer = new MediaPlayer();

        try {
            memoPlayer.setDataSource(memo.getFilePath());
            memoPlayer.prepare();
            timeSeekBar.setMax(memoPlayer.getDuration());

            memoPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    memoPlayer.start();
                }
            });
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        memoPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopMemo();
            }
        });

        updateSeekBar();

        //keep screen on while playing audio
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void setMediaPlayerToPoint(int progress) {

        memoPlayer = new MediaPlayer();

        try {
            memoPlayer.setDataSource(memo.getFilePath());
            memoPlayer.prepare();
            timeSeekBar.setMax(memoPlayer.getDuration());
            memoPlayer.seekTo(progress);

            memoPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopMemo();
                }
            });

        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        //keep screen on while playing audio
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void stopMemo() {
        playMemoButton.setImageResource(R.drawable.ic_media_play);
        memoHandler.removeCallbacks(runnable);
        memoPlayer.stop();
        memoPlayer.reset();
        memoPlayer.release();
        memoPlayer = null;

        timeSeekBar.setProgress(timeSeekBar.getMax());
        isPlaying = !isPlaying;

        currentProgressTextView.setText(fileLengthTextView.getText());
        timeSeekBar.setProgress(timeSeekBar.getMax());

        //allow the screen to turn off again once audio is finished playing
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void resumeMemo() {
        playMemoButton.setImageResource(R.drawable.ic_media_pause);
        memoHandler.removeCallbacks(runnable);
        memoPlayer.start();
        updateSeekBar();
    }

    private void pauseMemo() {
        playMemoButton.setImageResource(R.drawable.ic_media_play);
        memoHandler.removeCallbacks(runnable);
        memoPlayer.pause();
    }

    //updating timeSeekBar
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(memoPlayer != null){

                int mCurrentPosition = memoPlayer.getCurrentPosition();
                timeSeekBar.setProgress(mCurrentPosition);

                long minutes = TimeUnit.MILLISECONDS.toMinutes(mCurrentPosition);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(mCurrentPosition)
                        - TimeUnit.MINUTES.toSeconds(minutes);
                currentProgressTextView.setText(String.format("%02d:%02d", minutes, seconds));

                updateSeekBar();
            }
        }
    };

    private void updateSeekBar() {
        memoHandler.postDelayed(runnable, 1000);
    }
}
