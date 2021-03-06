package com.example.fergu.group_project.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fergu.group_project.R;
import com.example.fergu.group_project.fragements.PlaybackFragment;
import com.example.fergu.group_project.DBHelper;
import com.example.fergu.group_project.listeners.OnChangeListener;
import com.example.fergu.group_project.RecordingMemo;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by fergu on 2017-12-28.
 */

public class MemoViewerAdapter extends RecyclerView.Adapter<MemoViewerAdapter.RecordingsViewHolder>
        implements OnChangeListener {

    private static final String LOG_TAG = "MemoViewerAdapter";

    private DBHelper mDatabase;

    RecordingMemo recordingMemo;
    Context memoContext;
    LinearLayoutManager layoutManager;

    public MemoViewerAdapter(Context context, LinearLayoutManager linearLayoutManager) {
        super();
        memoContext = context;
        mDatabase = new DBHelper(memoContext);
        mDatabase.setOnDatabaseChangedListener(this);
        layoutManager = linearLayoutManager;
    }

    @Override
    public RecordingsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View memoView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.card_view, parent, false);
        memoContext = parent.getContext();

        return new RecordingsViewHolder(memoView);
    }

    public static class RecordingsViewHolder extends RecyclerView.ViewHolder {
        protected TextView viewName;
        protected TextView viewLength;
        protected TextView viewDateAdded;
        protected View cardView;

        public RecordingsViewHolder(View v) {
            super(v);
            viewName = (TextView) v.findViewById(R.id.file_name_text);
            viewLength = (TextView) v.findViewById(R.id.file_length_text);
            viewDateAdded = (TextView) v.findViewById(R.id.file_date_added_text);
            cardView = v.findViewById(R.id.card_view);
        }
    }

    @Override
    public void onBindViewHolder(final RecordingsViewHolder holder, int position) {

        recordingMemo = getItem(position);
        long itemDuration = recordingMemo.getLength();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration)
                - TimeUnit.MINUTES.toSeconds(minutes);
        holder.viewName.setText(recordingMemo.getName());
        holder.viewLength.setText(String.format("%02d:%02d", minutes, seconds));
        holder.viewDateAdded.setText(
                DateUtils.formatDateTime(
                        memoContext,
                        recordingMemo.getTime(),
                        DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_YEAR
                )
        );
        // define an on click listener to open PlaybackFragment
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    PlaybackFragment playbackFragment =
                            new PlaybackFragment().newInstance(getItem(holder.getPosition()));
                    FragmentTransaction transaction = ((FragmentActivity) memoContext)
                            .getSupportFragmentManager()
                            .beginTransaction();
                    playbackFragment.show(transaction, "dialog_playback");
                } catch (Exception e) {
                    Log.e(LOG_TAG, "exception", e);
                }
            }
        });
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ArrayList<String> entrys = new ArrayList<String>();
                entrys.add(memoContext.getString(R.string.dialog_file_share));
                entrys.add(memoContext.getString(R.string.dialog_file_rename));
                entrys.add(memoContext.getString(R.string.dialog_file_delete));
                final CharSequence[] memos = entrys.toArray(new CharSequence[entrys.size()]);
                // File delete confirm
                AlertDialog.Builder builder = new AlertDialog.Builder(memoContext);
                builder.setTitle(memoContext.getString(R.string.dialog_title_options));
                builder.setItems(memos, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if (item == 0) {
                            shareFileDialog(holder.getPosition());
                        } if (item == 1) {
                            renameFileDialog(holder.getPosition());
                        } else if (item == 2) {
                            deleteFileDialog(holder.getPosition());
                        }
                    }
                });
                builder.setCancelable(true);
                builder.setNegativeButton(memoContext.getString(R.string.dialog_action_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDatabase.getCount();
    }

    public RecordingMemo getItem(int position) {
        return mDatabase.getItemAt(position);
    }

    @Override
    public void onNewDatabaseEntryAdded() {
        notifyItemInserted(getItemCount() - 1);
        layoutManager.scrollToPosition(getItemCount() - 1);
    }

    @Override
    //TODO
    public void onDatabaseEntryRenamed() {

    }

    public void rename(int position, String name) {
        String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFilePath += "/SoundRecorder/" + name;
        File f = new File(mFilePath);
        if (f.exists() && !f.isDirectory()) {
            //file name is not unique, cannot rename file.
            Toast.makeText(memoContext,
                    String.format(memoContext.getString(R.string.toast_file_exists), name),
                    Toast.LENGTH_SHORT).show();

        } else {
            //file name is unique, rename file
            File oldFilePath = new File(getItem(position).getFilePath());
            oldFilePath.renameTo(f);
            mDatabase.renameItem(getItem(position), name, mFilePath);
            notifyItemChanged(position);
        }
    }

    public void renameFileDialog (final int position) {
        // File rename dialog
        AlertDialog.Builder renameFileBuilder = new AlertDialog.Builder(memoContext);

        LayoutInflater inflater = LayoutInflater.from(memoContext);
        View view = inflater.inflate(R.layout.dialog_name_file, null);

        final EditText input = (EditText) view.findViewById(R.id.new_name);

        renameFileBuilder.setTitle(memoContext.getString(R.string.dialog_title_name));
        renameFileBuilder.setCancelable(true);
        renameFileBuilder.setPositiveButton(memoContext.getString(R.string.dialog_action_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            String value = input.getText().toString().trim() + ".mp4";
                            rename(position, value);

                        } catch (Exception e) {
                            Log.e(LOG_TAG, "exception", e);
                        }

                        dialog.cancel();
                    }
                });
        renameFileBuilder.setNegativeButton(memoContext.getString(R.string.dialog_action_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        renameFileBuilder.setView(view);
        AlertDialog alert = renameFileBuilder.create();
        alert.show();
    }

    public void remove(int position) {
        File file = new File(getItem(position).getFilePath());
        file.delete();
        Toast.makeText(
                memoContext,
                String.format(
                        memoContext.getString(R.string.toast_file_delete),
                        getItem(position).getName()
                ),
                Toast.LENGTH_SHORT
        ).show();
        mDatabase.removeItemWithId(getItem(position).getId());
        notifyItemRemoved(position);
    }

    public void removeOutOfApp(String filePath) {
        //user deletes a saved recording out of the application through another application
    }

    public void deleteFileDialog (final int position) {
        // File delete confirm
        AlertDialog.Builder confirmDelete = new AlertDialog.Builder(memoContext);
        confirmDelete.setTitle(memoContext.getString(R.string.dialog_title_delete));
        confirmDelete.setMessage(memoContext.getString(R.string.dialog_text_delete));
        confirmDelete.setCancelable(true);
        confirmDelete.setPositiveButton(memoContext.getString(R.string.dialog_action_yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            //remove recordingMemo from database, recyclerview, and storage
                            remove(position);

                        } catch (Exception e) {
                            Log.e(LOG_TAG, "exception", e);
                        }

                        dialog.cancel();
                    }
                });
        confirmDelete.setNegativeButton(memoContext.getString(R.string.dialog_action_no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = confirmDelete.create();
        alert.show();
    }

    public void shareFileDialog(int position) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(getItem(position).getFilePath())));
        shareIntent.setType("audio/mp4");
        memoContext.startActivity(Intent.createChooser(shareIntent, memoContext.getText(R.string.send_to)));
    }
}
