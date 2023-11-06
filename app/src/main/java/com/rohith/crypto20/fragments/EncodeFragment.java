package com.rohith.crypto20.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.ayush.imagesteganographylibrary.Text.AsyncTaskCallback.TextEncodingCallback;
import com.ayush.imagesteganographylibrary.Text.ImageSteganography;
import com.ayush.imagesteganographylibrary.Text.TextEncoding;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.rohith.crypto20.R;
import com.rohith.crypto20.permenant.Constants;
import com.rohith.crypto20.permenant.PreferenceManager;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class EncodeFragment extends Fragment implements TextEncodingCallback {


    private View main_view;
    private static final int SELECT_PICTURE = 100;
    private static final String TAG = "Encode Class";

    //Created variables for UI
    private Toolbar encode_toolbar;
    private TextInputLayout secret_message_layout;
    private TextView whether_encoded;
    private ImageView imageView;
    private EditText message;
    private EditText secret_key;

    //Objects needed for encoding
    private TextEncoding textEncoding;
    private ImageSteganography imageSteganography;
    private ProgressDialog save;
    private Uri filepath;

    //Bitmaps
    private Bitmap original_image;
    private Bitmap encoded_image;
    private File file;
    private File fileToBeShared;
    private NavController navController;


    //sharedPreferences
    private PreferenceManager preferenceManager;
    private Snackbar feedback;

    private ChipNavigationBar navigationBar;
    private Handler handler;
    private AdView encodeAdview;

    public EncodeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController= Navigation.findNavController(view);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        main_view = inflater.inflate(R.layout.fragment_encode, container, false);

        //initialized the UI components
        secret_message_layout = main_view.findViewById(R.id.secret_message_layout);
        whether_encoded = main_view.findViewById(R.id.whether_encoded);
        imageView = main_view.findViewById(R.id.imageview_encode);
        message = main_view.findViewById(R.id.message);
        secret_key = main_view.findViewById(R.id.secret_key);
        navigationBar = main_view.findViewById(R.id.chipNavigationBar);

        navigationBar.setItemEnabled(R.id.encode, false);
        navigationBar.setItemEnabled(R.id.save, false);
        navigationBar.setItemEnabled(R.id.share, false);


        preferenceManager = new PreferenceManager(requireActivity());


        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                navController.navigate(R.id.action_encodeFragment_to_choiceFragment);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getActivity(), callback);


        //mobile ads

        MobileAds.initialize(requireActivity(), initializationStatus -> {

        });

        encodeAdview = main_view.findViewById(R.id.encode_adview);
        AdRequest adRequest = new AdRequest.Builder().build();

        //test mode
//        RequestConfiguration configuration = new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("DE9CCF98FC7CDB40E03190CAEAF88C70")).build();
//        MobileAds.setRequestConfiguration(configuration);

        encodeAdview.loadAd(adRequest);

        encodeAdview.setAdListener(new AdListener() {
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




        //shared preferences


        ImageButton choose_image_button = main_view.findViewById(R.id.choose_image_button);

        secret_message_layout.setEnabled(false);
        secret_message_layout.setStartIconTintList(ColorStateList.valueOf(Color.GRAY));
        secret_message_layout.setEndIconTintList(ColorStateList.valueOf(Color.GRAY));

        encode_toolbar = main_view.findViewById(R.id.encode_toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(encode_toolbar);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("Encode");
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //settings
        setHasOptionsMenu(true);
        checkAndRequestPermissions();


        //end Icon click listener
        secret_message_layout.setEndIconOnClickListener(v -> startVoiceInput());


        if (getArguments()!=null){
            EncodeFragmentArgs args=EncodeFragmentArgs.fromBundle(getArguments());
            if (!args.getImage().isEmpty()) {

                Uri imageUri = Uri.parse(args.getImage());
                try {
                   original_image= MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                   filepath=imageUri;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Glide.with(requireActivity()).load(imageUri).centerCrop().into(imageView);

                secret_message_layout.setEnabled(true);
                navigationBar.setItemEnabled(R.id.encode, true);
                navigationBar.showBadge(R.id.encode);

                secret_message_layout.setStartIconTintList(ColorStateList.valueOf(Color.BLUE));
                secret_message_layout.setEndIconTintList(ColorStateList.valueOf(Color.BLUE));
//
            }
        }



        //Choose image button
        choose_image_button.setOnClickListener(view -> ImageChooser());


        navigationBar.setOnItemSelectedListener(i -> {
            switch (i) {
                case R.id.save:

                    final Bitmap imgToSave = encoded_image;
                    Thread PerformEncoding = new Thread(() -> saveToInternalStorage(imgToSave));

                    navigationBar.setItemEnabled(R.id.share, true);
                    navigationBar.showBadge(R.id.share);
                    navigationBar.dismissBadge(R.id.save);

                    save = new ProgressDialog(requireActivity());
                    save.setMessage("Saving, Please Wait...");
                    save.setTitle("Saving Image");
                    save.setIndeterminate(false);
                    save.setCancelable(false);
                    save.show();


                    PerformEncoding.start();
                    handler.postDelayed(() -> {
                        // Actions to do after 1 seconds
                        if (save.isShowing()) {
                            save.dismiss();
                        }
                        navigationBar.setItemSelected(R.id.save, false);
                    }, 1000);
                    break;

                case R.id.encode:
                    whether_encoded.setText("");
                    String SECRET = message.getText().toString();
                    handler = new Handler(Looper.getMainLooper());

                    if (filepath != null) {
                        if (!SECRET.isEmpty()) {

                            String secretKey = preferenceManager.getString("secretKey");
                            //ImageSteganography Object instantiation
                            imageSteganography = new ImageSteganography(
                                    message.getText().toString().trim(),
                                    secretKey,
                                    original_image);

                            //TextEncoding object Instantiation
                            textEncoding = new TextEncoding(requireActivity(), EncodeFragment.this);
                            //Executing the encoding
                            textEncoding.execute(imageSteganography);
                            navigationBar.setItemEnabled(R.id.save, true);
                            navigationBar.showBadge(R.id.save);
                            navigationBar.dismissBadge(R.id.encode);
                            handler.postDelayed(new Runnable() {
                                public void run() {

                                    // Actions to do after 1 seconds
                                    navigationBar.setItemSelected(R.id.encode, false);

                                }
                            }, 1000);


                        } else {

                            whether_encoded.setTextColor(getResources().getColor(R.color.red, null));
                            whether_encoded.setText("No Secret Message Entered");
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    // Actions to do after 1 seconds
                                    navigationBar.setItemSelected(R.id.encode, false);
                                }
                            }, 1000);
                            navigationBar.setItemEnabled(R.id.save, false);

                        }
                    }
                    break;

                case R.id.share:
                        emailPicture(filepath);

                    break;

            }
        });


        return main_view;
    }

    @Override
    public void onStartTextEncoding() {

    }

    @Override
    public void onCompleteTextEncoding(ImageSteganography result) {

        if (result != null && result.isEncoded()) {
            encoded_image = result.getEncoded_image();
            whether_encoded.setTextColor(Color.GREEN);
            whether_encoded.setText("Encoded");


        } else if (!result.isEncoded()) {
            feedback = Snackbar.make(requireActivity().getWindow().getDecorView().getRootView(), "", BaseTransientBottomBar.LENGTH_INDEFINITE);
            feedback.setAnchorView(navigationBar).setTextColor(Color.BLACK).setBackgroundTint(getResources().getColor(R.color.white, null));
            feedback.setText("Unable to encode message").show();
        }

    }

    ActivityResultLauncher<Intent> textToSpeechActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    ArrayList<String> textList = result.getData().getStringArrayListExtra(
                            RecognizerIntent.EXTRA_RESULTS);
                    message.setText(
                            Objects.requireNonNull(textList).get(0));
                }
            });


    ActivityResultLauncher<Intent> imageChooseActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {


                        filepath = result.getData().getData();
                        try {
                            original_image = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), filepath);
                            Glide.with(requireActivity()).load(original_image).centerCrop().into(imageView);

                            secret_message_layout.setEnabled(true);
                            navigationBar.setItemEnabled(R.id.encode, true);
                            navigationBar.showBadge(R.id.encode);

                            secret_message_layout.setStartIconTintList(ColorStateList.valueOf(Color.BLUE));
                            secret_message_layout.setEndIconTintList(ColorStateList.valueOf(Color.BLUE));
//
                        } catch (IOException e) {
                        }

                    }

                }
            });

    private void ImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //auth data
        preferenceManager.putBoolean(Constants.USER_FINGERPRINT, false);
        imageChooseActivity.launch(Intent.createChooser(intent, "Select Picture"));
    }


    //Image set to imageView


    private void saveToInternalStorage(Bitmap bitmapImage) {
        OutputStream fOut;
        Date currentTime = Calendar.getInstance().getTime();

       File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "EncodedImages"); // the File to save//
        directory.mkdirs();
        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "EncodedImages"+File.separator + "Encoded" + currentTime.toString() + ".PNG"); // the File to save//


        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q) {

            ContentResolver resolver = requireActivity().getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "Encoded" + currentTime.toString() + ".PNG");
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "EncodedImages");
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);


            try {
                fOut = (FileOutputStream) resolver.openOutputStream(Objects.requireNonNull(imageUri));
                bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                Objects.requireNonNull(fOut);
                whether_encoded.post(() -> {
                    if (save.isShowing())
                        save.dismiss();
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


        }else {



            try {
                fOut = new FileOutputStream(file);

                bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fOut); // saving the Bitmap to a file
                fOut.flush(); // Not really required
                fOut.close(); // do not forget to close the stream

                whether_encoded.post(() -> {

                    if (save.isShowing())
                    save.dismiss();

                });
            } catch (IOException e) {

                e.printStackTrace();
            }
        }

    }


    private void checkAndRequestPermissions() {
        int permissionWriteStorage = ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int ReadPermission = ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();


        if (ReadPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }


        if (permissionWriteStorage != PackageManager.PERMISSION_GRANTED) {

            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(requireActivity(), listPermissionsNeeded.toArray(new String[0]), 1);
        }
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello, How can I help you?");
        try {
            textToSpeechActivity.launch(intent);
        } catch (ActivityNotFoundException a) {

        }
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        requireActivity().getMenuInflater().inflate(R.menu.settings_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.nav_settings) {

            final Dialog dialog = new Dialog(requireActivity());

            dialog.setContentView(R.layout.setttings_dailog);
            Window window = dialog.getWindow();
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
            dialog.show();

            String publicKeyString1 = preferenceManager.getString("secretKey");

            final TextInputEditText publicText = dialog.findViewById(R.id.public_key_text);
            Button apply = dialog.findViewById(R.id.apply_dialog_button);

            publicText.setTransformationMethod(new PasswordTransformationMethod());

            if (!publicKeyString1.isEmpty()) {

                publicText.setText(publicKeyString1);


            }

            apply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String publicKeyString = publicText.getText().toString();

                    preferenceManager.putString("secretKey", publicKeyString);
                    dialog.dismiss();

                }
            });


        }


        if (item.getItemId() == android.R.id.home) {
            navController.navigate(R.id.action_encodeFragment_to_choiceFragment);
        }


        return super.onOptionsItemSelected(item);
    }


    public void emailPicture(Uri fileUri) {


        Intent emailIntent
                = new Intent(
                android.content.Intent.ACTION_SEND);
        emailIntent.setType("application/image");


        // Subject of the mail
        emailIntent.putExtra(
                android.content.Intent.EXTRA_SUBJECT,
                "Encrypted Image");

        // Body of the mail
//        emailIntent.putExtra(
//                android.content.Intent.EXTRA_TEXT,
//                "Encoded image");

        // Set the location of the image file
        // to be added as an attachment
        emailIntent.putExtra(Intent.EXTRA_STREAM, fileUri);

        // Start the email activity
        // to with the prefilled information
        final PackageManager pm = requireActivity().getPackageManager();
        final List<ResolveInfo> matches = pm.queryIntentActivities(emailIntent, 0);
        ResolveInfo best = null;
        for (final ResolveInfo info : matches)
            if (info.activityInfo.packageName.endsWith(".gm") ||
                    info.activityInfo.name.toLowerCase().contains("gmail")) best = info;
        if (best != null)
            emailIntent.setClassName(best.activityInfo.packageName, best.activityInfo.name);
        startActivity(emailIntent);
    }

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