package com.rohith.crypto20.fragments;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rohith.crypto20.R;
import com.rohith.crypto20.databinding.FragmentLoginBinding;
import com.rohith.crypto20.permenant.Constants;
import com.rohith.crypto20.permenant.PreferenceManager;

import java.util.HashMap;


public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private NavController navController;
    private PreferenceManager preferenceManager;
    private FirebaseAuth auth;


    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(binding.getRoot());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(requireActivity());
        auth = FirebaseAuth.getInstance();
        setListeners();
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                navController.navigate(R.id.action_loginFragment_to_choiceFragment);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getActivity(), callback);
        return binding.getRoot();
    }


    void setListeners() {
        binding.createNewAccount.setOnClickListener(view -> {

            navController.navigate(R.id.action_loginFragment_to_registerFragment);

        });

        binding.buttonLogin.setOnClickListener(view -> {

            if (isValidSignup()) {
                signIn();
            }

        });
    }

    private void showToast(String message) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidSignup() {

        if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter your email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Invalid email");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter your password");
            return false;
        } else {
            return true;
        }
    }

    private void signIn() {
        loading(true);

        auth.signInWithEmailAndPassword(binding.inputEmail.getText().toString(), binding.inputPassword.getText().toString())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseFirestore database = FirebaseFirestore.getInstance();

                        database.collection(Constants.KEY_COLLECTION_USERS)
                                .document(auth.getUid())
                                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful() && task.getResult() != null) {

                                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                                    preferenceManager.putString(Constants.KEY_USER_ID, task.getResult().getId());
                                    preferenceManager.putString(Constants.KEY_NAME, task.getResult().getString(Constants.KEY_NAME));
                                    preferenceManager.putString(Constants.KEY_IMAGE, task.getResult().getString(Constants.KEY_IMAGE));
                                    navController.navigate(LoginFragmentDirections.actionLoginFragmentToMainFragment());

                                }
                            }
                        });


                    } else {
                        loading(false);
                        showToast("Invalid login credentials");
                    }
                });


    }


    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonLogin.setEnabled(false);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonLogin.setEnabled(true);

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            navController.navigate(LoginFragmentDirections.actionLoginFragmentToMainFragment());
        }
    }
}