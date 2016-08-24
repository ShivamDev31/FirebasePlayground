package com.shivamdev.firebasedemo;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addFirebaseFragment();
    }

    private void addFirebaseFragment() {
        FragmentManager manager = getSupportFragmentManager();
        GoogleSignInFragment gsiFragment = GoogleSignInFragment.newInstance();
        manager.beginTransaction().add(R.id.ll_firebase_fragment, gsiFragment, TAG)
                .addToBackStack(null).commit();
    }
}
