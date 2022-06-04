package com.rohith.crypto20.permenant;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    public SharedPreferences sharedPreferences;

    public PreferenceManager(Context context){

        sharedPreferences=context.getSharedPreferences(Constants.KEY_PREFERENCE_NAME,Context.MODE_PRIVATE);

    }


    public boolean check(String key){
        return sharedPreferences.contains(key);
    }

    public void putBoolean(String key, Boolean value){
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean(key,value);
        editor.apply();
    }
    public Boolean getBoolean(String key){
        return sharedPreferences.getBoolean(key,false);
    }

    public String getString(String key){
        return sharedPreferences.getString(key,"");
    }

    public void putString(String key, String value){
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString(key,value);
        editor.apply();
    }

    public void clearPreferences(){
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

}
