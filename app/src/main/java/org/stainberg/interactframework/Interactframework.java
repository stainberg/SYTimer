package org.stainberg.interactframework;

import android.app.Application;

/**
 * Created by stainberg on 9/23/14.
 */
public class Interactframework extends Application {
    private static Interactframework thiz;

    @Override
    public void onCreate() {
        thiz = this;
    }



    public static final Interactframework getInstance() {
        return thiz;
    }

}
