package com.example.finalyearproject.hollyboothroyd.sync.Activities.NewAccount;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.finalyearproject.hollyboothroyd.sync.R;

public class NewAccountJobInfoActivity extends AppCompatActivity {

    private EditText mPositionText;
    private EditText mCompanyText;
    private EditText mIndustryText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_account_job_info);

        mPositionText = (EditText)findViewById(R.id.position_text);
        mCompanyText = (EditText)findViewById(R.id.company_text);
        mIndustryText = (EditText)findViewById(R.id.industry_text);
        Button nextButton = (Button) findViewById(R.id.job_info_next_button);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Retrieve text from inputs
                String position = mPositionText.getText().toString().trim();
                String company = mCompanyText.getText().toString().trim();
                String industry = mIndustryText.getText().toString().trim();

                // Check if the user inputs pass basic validations
                if(areCredentialsValid(position, company, industry))
                {
                    // Save the inputted data to be sent to the next account creation activity
                    Intent intent = new Intent(NewAccountJobInfoActivity.this, NewAccountPhotoActivity.class);
                    intent.putExtra("firstName", getIntent().getStringExtra("firstName"));
                    intent.putExtra("lastName", getIntent().getStringExtra("lastName"));
                    intent.putExtra("email", getIntent().getStringExtra("email"));
                    intent.putExtra("password", getIntent().getStringExtra("password"));
                    intent.putExtra("position", position);
                    intent.putExtra("company", company);
                    intent.putExtra("industry", industry);

                    startActivity(intent);
                }
            }
        });
    }

    private boolean areCredentialsValid(String position, String company, String industry) {
        // Reset errors.
        mPositionText.setError(null);
        mCompanyText.setError(null);
        mIndustryText.setError(null);

        View focusView = null;

        // Check for a valid position
        if (TextUtils.isEmpty(position)) {
            mPositionText.setError(getString(R.string.error_field_required));
            focusView = mPositionText;
        }
        // Check for a valid company
        if (TextUtils.isEmpty(company)) {
            mCompanyText.setError(getString(R.string.error_field_required));
            focusView = mCompanyText;
        }
        // Check for a valid industry
        if (TextUtils.isEmpty(industry)) {
            mIndustryText.setError(getString(R.string.error_field_required));
            focusView = mIndustryText;
        }

        if (focusView != null) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            return false;
        }

        return true;
    }
}
