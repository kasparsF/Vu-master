package com.example.uberv.vugraph2.api;

import android.content.Context;

import com.apigee.sdk.ApigeeClient;
import com.apigee.sdk.data.client.ApigeeDataClient;

public class ApiBAAS {

    public static final String ORG_NAME="fisers";
    public static final String APP_NAME="graphs";
    public static final String BASE_URL="https://apibaas-trial.apigee.net";

    private static Context context;

    private static ApiBAAS instance = null;

    private ApigeeClient apigeeClient;
    private ApigeeDataClient apigeeDataClient;

    public ApigeeClient getApigeeClient() {
        return apigeeClient;
    }

    public ApigeeDataClient getApigeeDataClient() {
        return apigeeDataClient;
    }

    // TODO refactor to initialize in Application
    private ApiBAAS(Context context){
        ApiBAAS.context=context.getApplicationContext();
        apigeeClient = new ApigeeClient(ORG_NAME,APP_NAME,BASE_URL,ApiBAAS.context);
        apigeeDataClient = apigeeClient.getDataClient();
    }

    public static ApiBAAS getInstance(Context context){
        if(instance == null)
        {
            instance = new ApiBAAS(context);
        }
        return instance;
    }


}