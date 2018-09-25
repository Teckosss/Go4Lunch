package com.deguffroy.adrien.go4lunch.Utils;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * Created by Adrien Deguffroy on 24/09/2018.
 */
public class MinMaxFilters implements InputFilter {
    private int min, max;

    public MinMaxFilters(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public MinMaxFilters(String min, String max) {
        this.min = Integer.parseInt(min);
        this.max = Integer.parseInt(max);
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            if(end==1)
                min=Integer.parseInt(source.toString());
            int input = Integer.parseInt(dest.toString() + source.toString());
            if (isInRange(min, max, input))
                return null;
        } catch (NumberFormatException nfe) { }
        return "";
    }

    private boolean isInRange(int a, int b, int c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
}
