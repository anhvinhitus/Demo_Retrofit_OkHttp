package com.example.anhvinh.demo_retrofit_okhttp.Models.Entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by AnhVinh on 09/08/2017.
 */
@Entity
public class Worldpopulation implements Parcelable {
    @SerializedName("rank")
    @Expose
    private Integer rank;
    @SerializedName("country")
    @Expose
    private String country;
    @SerializedName("population")
    @Expose
    private String population;
    @SerializedName("flag")
    @Expose
    private String flag;

    protected Worldpopulation(Parcel in) {
        rank = in.readInt();
        country = in.readString();
        population = in.readString();
        flag = in.readString();
    }

    @Generated(hash = 2042845359)
    public Worldpopulation(Integer rank, String country, String population, String flag) {
        this.rank = rank;
        this.country = country;
        this.population = population;
        this.flag = flag;
    }

    @Generated(hash = 293430001)
    public Worldpopulation() {
    }

    /*
    Parcel:
    */
    public static final Creator<Worldpopulation> CREATOR = new Creator<Worldpopulation>() {
        @Override
        public Worldpopulation createFromParcel(Parcel in) {
            return new Worldpopulation(in);
        }

        @Override
        public Worldpopulation[] newArray(int size) {
            return new Worldpopulation[size];
        }
    };

    /*
        Getter - Setter
     */
    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPopulation() {
        return population;
    }

    public void setPopulation(String population) {
        this.population = population;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    /* Parcel func */
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(rank);
        parcel.writeString(country);
        parcel.writeString(population);
        parcel.writeString(flag);
    }
}
