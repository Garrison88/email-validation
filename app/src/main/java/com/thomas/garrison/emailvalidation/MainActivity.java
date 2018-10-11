package com.thomas.garrison.emailvalidation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    // API keys provided by Kickbox
    private final static String API_KEY =
            "live_089eb5be21af29431a5b52ec8a57453c39a8a123588317ad2441a8152497329f";
    //      "test_64c582ab0028023fbbcf19e0c44c99ea7b81f71b42c83a9cf84a8a337e10cb6f";
    AutoCompleteTextView autoCompTextView;
    ProgressBar pSpinner;
    String submittedEmail;
    ApiInterface apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiService = ApiService.getClient().create(ApiInterface.class);

        final Button submitButton = findViewById(R.id.btn_submit);

        // disable validate button initially
        submitButton.setEnabled(false);
        autoCompTextView = findViewById(R.id.actv_email);
        pSpinner = findViewById(R.id.progress_bar);

        // create list of 20 most common domains from String Array resource
        String[] domainsList = getResources().getStringArray(R.array.domains_array);
        ArrayList<String> domainsArray = new ArrayList<>(Arrays.asList(domainsList));
        CustomAutoCompleteAdapter adapter = new CustomAutoCompleteAdapter(this, android.R.layout.simple_list_item_1, domainsArray);

        // set number of characters typed before suggestions will be shown
        autoCompTextView.setThreshold(1);
        autoCompTextView.setAdapter(adapter);

        autoCompTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String value = s.toString();
                // enable submit button when edittext has 5 or more characters entered and includes '@' and '.'
                submitButton.setEnabled(value.contains(getString(R.string.at_symbol))
                        && value.contains(getString(R.string.period))
                        && s.length() >= 5);
                // remove check mark drawable from button and return button text to normal
                submitButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                submitButton.setText(R.string.submit_btn_text);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            submittedEmail = autoCompTextView.getText().toString();
            // display progress spinner and hide button
            pSpinner.setVisibility(View.VISIBLE);
            submitButton.setVisibility(View.GONE);

            Call<Email> call = apiService.emailResponse(submittedEmail, API_KEY);

            // receive information about submitted email from Kickbox API using Retrofit
            call.enqueue(new Callback<Email>() {
                @Override
                public void onResponse(Call<Email> call, Response<Email> response) {
                        // 1: handle errors involving typos
                    if(response.body().getDidYouMean() != null) {
//                        Drawable dr = getDrawable(R.drawable.ic_baseline_done_dark_24px);
//                        dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
//                        autoCompTextView.setCompoundDrawables(null,null,dr,null);
                        autoCompTextView.setError(getString(R.string.err_misspelling) + response.body().getDidYouMean());
                        // 2: catch use of disposable email address (eg. @trashmail.com, mailinator.com)
                    } else if (response.body().isDisposable()) {
                        autoCompTextView.setError(getString(R.string.err_disposable_domain));
                        autoCompTextView.startAnimation(shakeError());
                        // 3: email address was undeliverable - display error message explaining why
                    } else if (response.body().getResult() != null && !response.body().getResult().equals(getString(R.string.deliverable))) {
                        autoCompTextView.setError(getReasonString(response.body().getReason()));
                        autoCompTextView.startAnimation(shakeError());
                    } else {
                        // 4: on successful validation, display a toast and change button text
                        Toast.makeText(getApplicationContext(), response.body().getEmail() + getString(R.string.is_valid), Toast.LENGTH_LONG).show();
                        submitButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_done_24px, 0, 0, 0);
                        submitButton.setText(R.string.submit_btn_text_success);
                    }
                    // restore button visibility and hide progress spinner
                    pSpinner.setVisibility(View.GONE);
                    submitButton.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFailure(Call<Email> call, Throwable t) {

                }
            });
            }
        });
    }

    // shake autoCompleteTextView on error
    public TranslateAnimation shakeError() {
        TranslateAnimation shake = new TranslateAnimation(0, 10, 0, 0);
        shake.setDuration(400);
        shake.setInterpolator(new CycleInterpolator(4));
        return shake;
    }

    // this method returns a string representing the reason for the refused email address
    public String getReasonString(String reason) {
        String result;
        switch(reason){
            case "invalid_email" : result =  getString(R.string.err_invalid_email);
                break;
            case "invalid_domain" : result = getString(R.string.err_invalid_domain);
                break;
            case "rejected_email" : result = getString(R.string.err_rejected_email);
                break;
            case "no_connect" : result = getString(R.string.err_no_connect);
                break;
            case "timeout" : result = getString(R.string.err_timeout);
                break;
            case "invalid_smtp" : result = getString(R.string.err_invalid_smtp);
                break;
            case "unavailable_smtp" : result = getString(R.string.err_unavailable_smtp);
                break;
            default : result = getString(R.string.err_unexpected);
        }
        return result;
    }

}
