package com.rohith.crypto20.fragments;


import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.ayush.imagesteganographylibrary.Text.AsyncTaskCallback.TextDecodingCallback;
import com.ayush.imagesteganographylibrary.Text.ImageSteganography;
import com.ayush.imagesteganographylibrary.Text.TextDecoding;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.rohith.crypto20.FileUtils;
import com.rohith.crypto20.R;
import com.rohith.crypto20.permenant.Constants;
import com.rohith.crypto20.permenant.PreferenceManager;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import co.mobiwise.materialintro.animation.MaterialIntroListener;
import co.mobiwise.materialintro.prefs.PreferencesManager;
import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.shape.ShapeType;
import co.mobiwise.materialintro.view.MaterialIntroView;

import static android.app.Activity.RESULT_OK;
import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;
import static androidx.biometric.BiometricPrompt.ERROR_NEGATIVE_BUTTON;


public class DecodeFragment extends Fragment implements TextDecodingCallback {

    private View main_view;

    private static final String TAG = "Decode Class";
    //Initializing the UI components
    private ImageButton choose_image_button;
    private ImageButton decode_button;

    private Toolbar decode_toolbar;

    private ImageView imageView;
    private TextView message;
    private EditText secret_key;
    private Uri filepath;
    //Bitmap
    private Bitmap original_image;


    private String SECRET_MESSAGE;
    private String imageName;
    private String path;

    //fingerprint
    private BiometricPrompt biometricPrompt;
    private final Executor executor = Executors.newSingleThreadExecutor();


    private PreferenceManager preferenceManager;

    private LottieAnimationView animationView;
    private Snackbar feedback;
    private MaterialIntroView.Builder materialIntroView;
    private PreferencesManager pref;

    private AdView decode_adview;


    public DecodeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        main_view = inflater.inflate(R.layout.fragment_decode, container, false);


        preferenceManager=new PreferenceManager(requireActivity());

        pref = new PreferencesManager(requireActivity());


        decode_toolbar = main_view.findViewById(R.id.decode_toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(decode_toolbar);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("Decode");

        ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setHasOptionsMenu(true);

        imageView = main_view.findViewById(R.id.imageview_decode);
        message = main_view.findViewById(R.id.message);
        secret_key = main_view.findViewById(R.id.secret_key);
        animationView = main_view.findViewById(R.id.success_lottie);

        choose_image_button = main_view.findViewById(R.id.choose_image_button);
        decode_button = main_view.findViewById(R.id.decode_button);

        decode_button.setEnabled(false);
        decode_button.setImageResource(R.drawable.ic_lock_grey_24dp);

        //Choose Image Button
        choose_image_button.setOnClickListener(view -> ImageChooser());


        //advertise
        decode_adview = main_view.findViewById(R.id.decode_adview);
        AdRequest adRequest = new AdRequest.Builder().build();

        //test mode
//        RequestConfiguration configuration = new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("DE9CCF98FC7CDB40E03190CAEAF88C70")).build();
//        MobileAds.setRequestConfiguration(configuration);

        decode_adview.loadAd(adRequest);

        decode_adview.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        });



        //Decode Button
        decode_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (filepath != null) {

                    //Making the ImageSteganography object
                    ImageSteganography imageSteganography = new ImageSteganography(secret_key.getText().toString(),
                            original_image);

                    //Making the TextDecoding object
                    TextDecoding textDecoding = new TextDecoding(requireActivity(), DecodeFragment.this);

                    //Execute Task
                    if (!original_image.isRecycled())
                        textDecoding.execute(imageSteganography);
                }
            }
        });

        if (getArguments()!=null){
            DecodeFragmentArgs args=DecodeFragmentArgs.fromBundle(getArguments());
            if (!args.getImage().isEmpty()) {

                Uri imageUri = Uri.parse(args.getImage());
                try {
                    original_image= MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                    filepath=imageUri;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Glide.with(requireActivity()).load(imageUri).centerCrop().into(imageView);

//
            }
        }

        return main_view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {
        requireActivity().getMenuInflater().inflate(R.menu.decode_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {

        if (item.getItemId() == R.id.decode_help) {

            materialIntroView = new MaterialIntroView.Builder(requireActivity())
                    .enableIcon(true)
                    .setFocusGravity(FocusGravity.CENTER)
                    .performClick(false)
                    .setFocusType(Focus.MINIMUM)
                    .setDelayMillis(100)
                    .setInfoText("Type in the secret key if specified while encoding")
                    .setShape(ShapeType.RECTANGLE)
                    .setTarget(secret_key)
                    .setUsageId("key")
                    .setMaskColor(getResources().getColor(R.color.transparentBlue, null));

            materialIntroView.setListener(new MaterialIntroListener() {
                @Override
                public void onUserClicked(String materialIntroViewId) {


                    materialIntroView
                            .setInfoText("Choose your image that you want to decode")
                            .setShape(ShapeType.RECTANGLE)
                            .setTarget(choose_image_button)
                            .setFocusType(Focus.NORMAL)
                            .setUsageId("choose")
                            .setListener(new MaterialIntroListener() {
                                @Override
                                public void onUserClicked(String materialIntroViewId) {
                                    materialIntroView
                                            .setInfoText("Extract the text contents from the image")
                                            .setShape(ShapeType.RECTANGLE)
                                            .setTarget(decode_button)
                                            .setFocusType(Focus.NORMAL)
                                            .setUsageId("decode").show();
                                    pref.reset("choose");
                                    pref.reset("decode");
                                    pref.reset("key");
                                }
                            }).show();


                }
            }).show();

            pref.reset("key");


        }
        if (item.getItemId()==android.R.id.home){
            requireActivity().onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {

                        filepath = result.getData().getData();


                        //fileName

                        Cursor returnCursor =
                                requireActivity().getContentResolver().query(filepath, null, null, null, null);
                        /*
                         * Get the column indexes of the data in the Cursor,
                         * move to the first row in the Cursor, get the data,
                         * and display it.
                         */
                        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        returnCursor.moveToFirst();
                        imageName = returnCursor.getString(nameIndex);

                        path = FileUtils.getPath(requireActivity(), filepath);
                        //filepath

                        try {
                            original_image = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), filepath);
                            Glide.with(requireActivity()).load(original_image).centerCrop().into(imageView);
                            decode_button.setEnabled(true);
                            decode_button.setImageResource(R.drawable.ic_lock_black_24dp);


                        } catch (IOException e) {
                        }
                    }

                }
            });

    private void ImageChooser() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "EncodedImages"); // the File to save//
        directory.mkdirs();
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "EncodedImages";
        Uri uri = Uri.parse(path);
        intent.setDataAndType(uri, "image/*");


        preferenceManager.putBoolean(Constants.USER_FINGERPRINT, false);

        someActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }


    @Override
    public void onStartTextEncoding() {
        //Whatever you want to do by the start of textDecoding
    }

    @Override
    public void onCompleteTextEncoding(ImageSteganography result) {

        //By the end of textDecoding
        // imageView.setImageBitmap(original_image);
        animationView.setVisibility(View.VISIBLE);
        if (result != null) {


            feedback = Snackbar.make(requireActivity().getWindow().getDecorView().getRootView(), "", BaseTransientBottomBar.LENGTH_INDEFINITE);
            feedback.setAnchorView(decode_button).setTextColor(Color.BLACK).setBackgroundTint(getResources().getColor(R.color.white, null));
            if (!result.isDecoded()) {

                feedback.setText("No message found").show();
                animationView.setAnimation("empty.json");

            } else {

                if (!result.isSecretKeyWrong()) {

                    SECRET_MESSAGE = result.getMessage();
                    biometricPrompt = new BiometricPrompt(this, executor, callback);
                    checkAndAuthenticate();

                    feedback.setText("Decoded successfully").setTextColor(getResources().getColor(R.color.green, null)).show();
                    animationView.setAnimation("success.json");


                } else {


                    feedback.setText("Wrong secret key").setTextColor(getResources().getColor(R.color.red, null)).show();
                    animationView.setAnimation("locked.json");

                }
            }
            animationView.setRepeatCount(5);
            animationView.playAnimation();
        }

    }


    //fingerprint
    private BiometricPrompt.PromptInfo buildBiometricPrompt() {
        return new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Authentication")
                .setDescription("Please place your finger on the sensor to unlock")
                .setAllowedAuthenticators(BIOMETRIC_WEAK | BIOMETRIC_STRONG
                        | DEVICE_CREDENTIAL)
                .build();

    }

    private void checkAndAuthenticate() {
        BiometricManager biometricManager = BiometricManager.from(requireActivity());
        switch (biometricManager.canAuthenticate(BIOMETRIC_WEAK | BIOMETRIC_STRONG
                | DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                BiometricPrompt.PromptInfo promptInfo = buildBiometricPrompt();
                biometricPrompt.authenticate(promptInfo);
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(requireActivity(), "Biometric Authentication currently unavailable", Toast.LENGTH_SHORT).show();

                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(requireActivity(), "Your device doesn't support Biometric Authentication", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(requireActivity(), "Your device doesn't have any fingerprint enrolled", Toast.LENGTH_SHORT).show();
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

                    requireActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            // Stuff that updates the UI
                            TextView messageView = main_view.findViewById(R.id.message_decode);
                            messageView.setText(SECRET_MESSAGE);
                        }
                    });




                }

                @Override
                public void onAuthenticationFailed() {
                }
            };


    //review



    @Override
    public void onDestroy() {
        super.onDestroy();
        if (feedback != null) {
            if (feedback.isShown()) {
                feedback.dismiss();
            }
        }
    }



}