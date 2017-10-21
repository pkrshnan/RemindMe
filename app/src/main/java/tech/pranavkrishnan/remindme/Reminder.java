package tech.pranavkrishnan.remindme;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Pranav Krishnan on 10/11/2017.
 */

public class Reminder implements Parcelable, Serializable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Reminder createFromParcel(Parcel in) {
            return new Reminder(in);
        }
        public Reminder[] newArray(int size) {
            return new Reminder[size];
        }
    };

    private String title, address, category, priority;
    private boolean[] repeat = new boolean[7];


    public Reminder() {
    }

    public Reminder(String title, String address, String category, String priority, boolean[] repeat) {
        this.title = title;
        this.address = address;
        this.category = category;
        this.priority = priority;
        this.repeat = repeat;
    }

    public String getTitle() {
        return this.title;
    }

    public String getAddress() {
        return this.address;
    }

    public String getCategory() {
        return this.category;
    }

    public String getPriority() {
        return this.priority;
    }

    public boolean[] getRepeat() {
        return this.repeat;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setRepeat(boolean sunday, boolean monday, boolean tuesday, boolean wednesday, boolean thursday, boolean friday, boolean saturday) {
        this.repeat = new boolean[] {sunday, monday, tuesday, wednesday, thursday, friday, saturday};
    }

    // Parcelling part
    public Reminder(Parcel in) {
        this.title = in.readString();
        this.address = in.readString();
        this.category =  in.readString();
        this.priority = in.readString();
        in.readBooleanArray(this.repeat);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.address);
        dest.writeString(this.category);
        dest.writeString(this.priority);
        dest.writeBooleanArray(this.repeat);

        //TODO: Change utilization of repeat in all places,making it so it gets a boolean array from getRepeat()
    }

    @Override
    public String toString() {
        return "Reminder {" + "title=' " + this.title + '\'' + ", address=' " + this.address + '\'' + ", category=' " + this.category + '\'' + '}';
    }
}
