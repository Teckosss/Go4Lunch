package com.deguffroy.adrien.go4lunch.Models.PlacesInfo;

import com.deguffroy.adrien.go4lunch.Models.PlacesInfo.PlacesDetails.Period;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Adrien Deguffroy on 20/07/2018.
 */
public class OpeningHours {
    @SerializedName("open_now")
    @Expose
    public Boolean openNow;
    @Expose
    public List<Period> periods = null;
    @SerializedName("weekday_text")
    @Expose
    public List<String> weekdayText = null;

    public Boolean getOpenNow() {
        return openNow;
    }

    public void setOpenNow(Boolean openNow) {
        this.openNow = openNow;
    }

    public List<Period> getPeriods() {
        return periods;
    }

    public void setPeriods(List<Period> periods) {
        this.periods = periods;
    }

    public List<String> getWeekdayText() {
        return weekdayText;
    }

    public void setWeekdayText(List<String> weekdayText) {
        this.weekdayText = weekdayText;
    }
}