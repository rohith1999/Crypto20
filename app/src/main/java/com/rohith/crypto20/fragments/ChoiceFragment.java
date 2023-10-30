package com.rohith.crypto20.fragments;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rohith.crypto20.R;
import com.rohith.crypto20.SettingsBottomSheetFragment;
import com.rohith.crypto20.models.User;
import com.rohith.crypto20.permenant.Constants;

import co.mobiwise.materialintro.animation.MaterialIntroListener;
import co.mobiwise.materialintro.prefs.PreferencesManager;
import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.shape.ShapeType;
import co.mobiwise.materialintro.view.MaterialIntroView;


public class ChoiceFragment extends Fragment implements LifecycleObserver {

    private View main_view;
    private LottieAnimationView lockLottie;
    private NavController navController;
    private MaterialIntroView.Builder materialIntroView;
    private PreferencesManager pref;

    private Button encode;
    private Button decode;
    private Button about;
    private ImageButton help;
    private ImageButton settings;
    private FloatingActionButton messageButton;

    public ChoiceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        main_view = inflater.inflate(R.layout.fragment_choice, container, false);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        lockLottie = main_view.findViewById(R.id.lottie_lock);
        lockLottie.setMinAndMaxProgress(0.2f, 1.0f);
        lockLottie.playAnimation();
        lockLottie.setRepeatCount(5);


        return main_view;


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        encode = main_view.findViewById(R.id.encode_button);
        decode = main_view.findViewById(R.id.decode_button);
        help = main_view.findViewById(R.id.help_choice);
        settings = main_view.findViewById(R.id.settings_choice);
        about = main_view.findViewById(R.id.about_button);
        messageButton = main_view.findViewById(R.id.message_button);

        navController = Navigation.findNavController(main_view);

        Intent intent = requireActivity().getIntent();

//        if(intent.hasExtra(Constants.KEY_USER)) {
//            User user = (User) intent.getExtras().get(Constants.KEY_USER);
//            ChoiceFragmentDirections.ActionChoiceFragmentToChatFragment action = ChoiceFragmentDirections.actionChoiceFragmentToChatFragment(
//                    user
//            );
//            navController.navigate(action);
//
//        }


        // Figure out what to do based on the intent type
        if (intent != null) {

            if (Intent.ACTION_SEND.equals(intent.getAction()) && intent.getType() != null) {
                // Handle intents with image data ...
                Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);

                ChoiceFragmentDirections.ActionChoiceFragmentToEncodeFragment action = ChoiceFragmentDirections.actionChoiceFragmentToEncodeFragment(
                        imageUri.toString()
                );
                navController.navigate(action);

            }

        }

        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_choiceFragment_to_loginFragment);
            }
        });

        encode.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View view) {

                                          ChoiceFragmentDirections.ActionChoiceFragmentToEncodeFragment actionEncode = ChoiceFragmentDirections.actionChoiceFragmentToEncodeFragment(
                                                  ""
                                          );
                                          navController.navigate(actionEncode);

                                      }
                                  });

                decode.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        navController.navigate(R.id.action_choiceFragment_to_decodeFragment);
                    }
                });

        settings.setOnClickListener(view15 -> {
            SettingsBottomSheetFragment settingsBottomSheetFragment = new SettingsBottomSheetFragment();
            settingsBottomSheetFragment.show(getParentFragmentManager(), "TAG");

        });


        about.setOnClickListener(view14 -> navController.navigate(R.id.action_choiceFragment_to_aboutFragment));


        help.setOnClickListener(view13 -> {


            pref = new PreferencesManager(requireActivity());
            materialIntroView = new MaterialIntroView.Builder(requireActivity())
                    .enableIcon(true)
                    .setFocusGravity(FocusGravity.CENTER)
                    .performClick(false)
                    .setFocusType(Focus.MINIMUM)
                    .setDelayMillis(100)

                    .setInfoText("Encode button helps you to hide your text inside your specified image")
                    .setShape(ShapeType.RECTANGLE)
                    .setTarget(encode)
                    .setUsageId("encode")
                    .setMaskColor(getResources().getColor(R.color.transparentBlue, null));

            materialIntroView.setListener(new MaterialIntroListener() {
                @Override
                public void onUserClicked(String materialIntroViewId) {


                    materialIntroView
                            .setInfoText("Decode button helps you to reveal your text inside your specified image")
                            .setShape(ShapeType.RECTANGLE)
                            .setTarget(decode)
                            .setUsageId("decode")
                            .show();
                    pref.reset("encode");
                    pref.reset("decode");


                }
            }).show();

        });


    }
}