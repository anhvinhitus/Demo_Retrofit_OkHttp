package vn.com.vng.zalopay.domain.model;

import com.google.gson.annotations.SerializedName;

import java.util.HashSet;

/**
 * Created by lytm on 10/08/2017.
 */

public class Api {
    @SerializedName("api_route")
    public String apiRoute = "https"; //"https|connector"

    @SerializedName("api_names")
    public HashSet<String> apiNames;

}
