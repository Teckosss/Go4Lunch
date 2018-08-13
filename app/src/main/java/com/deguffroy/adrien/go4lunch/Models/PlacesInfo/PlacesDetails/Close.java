package com.deguffroy.adrien.go4lunch.Models.PlacesInfo.PlacesDetails;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Adrien Deguffroy on 05/08/2018.
 */
public class Close {
    @SerializedName("day")
    @Expose
    public Integer day;
    @SerializedName("time")
    @Expose
    public String time;

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
