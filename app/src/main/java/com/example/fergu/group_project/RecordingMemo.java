package com.example.fergu.group_project;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fergu on 2017-12-28.
 */

public class RecordingMemo implements Parcelable{

    private String mName; // file name
    private String mFilePath; //file path
    private int mId; //id in database
    private int mLength; // length of recording in seconds
    private long mTime; // date/time of the recording

    public RecordingMemo()
    {
    }

    public RecordingMemo(Parcel in) {
        mName = in.readString();
        mFilePath = in.readString();
        mId = in.readInt();
        mLength = in.readInt();
        mTime = in.readLong();
    }

    public String getFilePath() {
        return mFilePath;
    }

    public void setFilePath(String filePath) {
        mFilePath = filePath;
    }

    public int getLength() {
        return mLength;
    }

    public void setLength(int length) {
        mLength = length;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public static final Parcelable.Creator<RecordingMemo> CREATOR = new Parcelable.Creator<RecordingMemo>() {
        public RecordingMemo createFromParcel(Parcel in) {
            return new RecordingMemo(in);
        }

        public RecordingMemo[] newArray(int size) {
            return new RecordingMemo[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeInt(mLength);
        dest.writeLong(mTime);
        dest.writeString(mFilePath);
        dest.writeString(mName);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}

