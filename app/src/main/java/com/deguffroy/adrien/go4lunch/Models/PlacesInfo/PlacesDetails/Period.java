package com.deguffroy.adrien.go4lunch.Models.PlacesInfo.PlacesDetails;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Adrien Deguffroy on 05/08/2018.
 */
public class Period {
    @Expose
    public Close close;
    @SerializedName("open")
    @Expose
    public Open open;

    public Close getClose() {
        return close;
    }

    public void setClose(Close close) {
        this.close = close;
    }

    public Open getOpen() {
        return open;
    }

    public void setOpen(Open open) {
        this.open = open;
    }
}
