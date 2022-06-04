package com.rohith.crypto20.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;
import com.rohith.crypto20.R;


public class AboutFragment extends Fragment {

    private View main_view;
    private Button backButton;

    //links
    private ImageButton mail_button;
    private ImageButton facebook_button;
    private ImageButton instagram_button;
    private ImageButton whatsapp_button;



    public AboutFragment() {
        // Required empty public constructor
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        main_view =inflater.inflate(R.layout.fragment_about, container, false);

        backButton=main_view.findViewById(R.id.about_back_button);
        mail_button=main_view.findViewById(R.id.mail_button);
        facebook_button=main_view.findViewById(R.id.facebook_button);
        instagram_button=main_view.findViewById(R.id.instagram_button);
        whatsapp_button=main_view.findViewById(R.id.whatsapp_button);

        mail_button.setOnClickListener(view -> emailUs());

        facebook_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchFacebook();
            }
        });

        instagram_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchInsta();
            }
        });


        whatsapp_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSupportChat();

            }
        });


        backButton.setOnClickListener(view -> {
            requireActivity().onBackPressed();

        });


        //ReviewManager manager = new FakeReviewManager(getActivity().getApplicationContext());
        ReviewManager manager = ReviewManagerFactory.create(requireActivity());
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // We can get the ReviewInfo object
                ReviewInfo reviewInfo = task.getResult();
                if(requireActivity()!=null){
                    Task<Void> flow = manager.launchReviewFlow(requireActivity(), reviewInfo);
                    flow.addOnCompleteListener(task1 -> {
                        Log.d("rrrrrrrrrrr",task1.toString());

                    });
                }


            } else {
                // There was some problem, log or handle the error code.

                String reviewErrorCode =  task.getException().getMessage();
                Log.d("rrrrrrrrrrr",reviewErrorCode);
            }
        });


        return main_view;
    }




    //email
    private void emailUs(){
        /* Create the Intent */
        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

        /* Fill it with Data */
        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"rohithsuri3@gmail.com"});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Feedback on Selock");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");

        /* Send it off to the Activity-Chooser */
        startActivity(Intent.createChooser(emailIntent, "Feedback on Selock"));
    }


    public final void launchFacebook() {

        String url = "fb://page/114592423280403";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {

            url = "https://www.facebook.com/Indrum-114592423280403/";
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);

        }


    }

    public final void launchInsta() {
        String url = "https://www.instagram.com/mithran.indrum/";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }


    //open whatsApp
    private void startSupportChat() {

        try {
            String headerReceiver = "Hi, \n";// Replace with your message.
            String bodyMessageFormal = "Feedback on Selock\n";// Replace with your message.
            String whatsappContain = headerReceiver + bodyMessageFormal;
            String trimToNumber = "+917892259438"; //10 digit number
            Intent intent = new Intent ( Intent.ACTION_VIEW );
            intent.setData ( Uri.parse ( "https://wa.me/" + trimToNumber + "/?text=" + whatsappContain ) );
            startActivity ( intent );
        } catch (Exception e) {
            e.printStackTrace ();
        }


    }



}