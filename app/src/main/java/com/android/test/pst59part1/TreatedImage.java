package com.android.test.pst59part1;

import android.graphics.RectF;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreatedImage implements Parcelable {

    private Uri imageUri;
    private Map<RectF, String> description;
    private List<RectF> selections;

    TreatedImage(Uri imageUri, Map<RectF, String> description) {
        this.imageUri = imageUri;
        this.description = description;
        selections = new ArrayList<>();
        selections.addAll(description.keySet());
    }

    private TreatedImage(Parcel in) {
        imageUri = in.readParcelable(Uri.class.getClassLoader());
        description = new HashMap<>();
        for (int i = 0 ; i < in.readInt() ; i++) {
            description.put((RectF) in.readParcelable(RectF.class.getClassLoader()), in.readString());
        }
        selections = new ArrayList<>();
        selections.addAll(description.keySet());
    }

    Uri getImageUri() {
        return imageUri;
    }

    public Map<RectF, String> getDescription() {
        return description;
    }

    RectF getSelection(int position) {
        return selections.get(position);
    }

    void addSelection(RectF selection) {
        description.put(selection, null);
        selections.add(selection);
    }

    public static final Creator<TreatedImage> CREATOR = new Creator<TreatedImage>() {
        @Override
        public TreatedImage createFromParcel(Parcel in) {
            return new TreatedImage(in);
        }

        @Override
        public TreatedImage[] newArray(int size) {
            return new TreatedImage[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(imageUri, flags);
        dest.writeInt(description.keySet().size());
        for (Map.Entry<RectF,String> entry : description.entrySet()) {
            dest.writeParcelable(entry.getKey(), flags);
            dest.writeString(entry.getValue());
        }
    }
}
