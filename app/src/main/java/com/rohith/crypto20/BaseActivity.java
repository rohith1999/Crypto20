package com.rohith.crypto20;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rohith.crypto20.fragments.UsersFragmentDirections;
import com.rohith.crypto20.models.User;
import com.rohith.crypto20.permenant.Constants;
import com.rohith.crypto20.permenant.PreferenceManager;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;
import static androidx.biometric.BiometricPrompt.ERROR_NEGATIVE_BUTTON;

public class BaseActivity extends AppCompatActivity implements LifecycleObserver {

    private BiometricPrompt biometricPrompt;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private PreferenceManager preferenceManager;
    private DocumentReference documentReference;
    private FirebaseAuth auth;
    private FirebaseFirestore database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        //auth data

        preferenceManager = new PreferenceManager(this);

         database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        if (auth.getUid()!=null) {
            documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                    .document(auth.getUid());
        }

        if (!preferenceManager.check(Constants.USER_FINGERPRINT)) {
            preferenceManager.putBoolean(Constants.USER_FINGERPRINT, true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {


//        else if (shouldShowRequestPermissionRationale(...)) {
//            // In an educational UI, explain to the user why your app requires this
//            // permission for a specific feature to behave as expected, and what
//            // features are disabled if it's declined. In this UI, include a
//            // "cancel" or "no thanks" button that lets the user continue
//            // using your app without granting the permission.
            } else {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 22);

            }
        }


        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);


    }

    //fingerprint auth
    private BiometricPrompt.PromptInfo buildBiometricPrompt() {
        return new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Authentication")
                .setDescription("Please place your finger on the sensor to unlock")
                .setAllowedAuthenticators(BIOMETRIC_WEAK | BIOMETRIC_STRONG
                        | DEVICE_CREDENTIAL)
                .build();

    }


    private void checkAndAuthenticate() {
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BIOMETRIC_WEAK | BIOMETRIC_STRONG
                | DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                BiometricPrompt.PromptInfo promptInfo = buildBiometricPrompt();
                biometricPrompt.authenticate(promptInfo);
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(this, "Biometric Authentication currently unavailable", Toast.LENGTH_SHORT).show();

                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(this, "Your device doesn't support Biometric Authentication", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(this, "Your device doesn't have any fingerprint enrolled", Toast.LENGTH_SHORT).show();
                break;
        }
    }


    BiometricPrompt.AuthenticationCallback callback = new
            BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    if (errorCode == ERROR_NEGATIVE_BUTTON && biometricPrompt != null)
                        biometricPrompt.cancelAuthentication();
                    System.exit(0);
                }

                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {


                }

                @Override
                public void onAuthenticationFailed() {
                }
            };


    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForegrounded() {

        if (preferenceManager.getBoolean(Constants.USER_FINGERPRINT)) {
            biometricPrompt = new BiometricPrompt(this, executor, callback);
            checkAndAuthenticate();
            preferenceManager.putBoolean(Constants.USER_FINGERPRINT, true);
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        if (documentReference!=null){
            documentReference.update(Constants.KEY_AVAILABILITY, 0);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (documentReference!=null)
        documentReference.update(Constants.KEY_AVAILABILITY, 1);
    }
}