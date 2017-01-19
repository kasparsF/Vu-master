package com.example.uberv.vugraph2;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.transition.ChangeBounds;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.apigee.sdk.data.client.callbacks.ApiResponseCallback;
import com.apigee.sdk.data.client.response.ApiResponse;
import com.example.uberv.vugraph2.api.ApiBAAS;
import com.example.uberv.vugraph2.utils.PreferencesUtils;
import com.example.uberv.vugraph2.utils.ValidationHelper;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.R.attr.animateLayoutChanges;
import static com.example.uberv.vugraph2.R.id.btnLinkToLoginScreen;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private ApiBAAS mApiBAAS;

    ViewGroup rootLayout;
    ViewGroup detailContainer;
    EditText fullnameEt;
    EditText emailEt;
    EditText usernameEt;
    EditText passwordEt;
    Button loginButton;
    Button RegisterButton;
    Button linkToRegisterScreenButton;
    Button linkToLoginScreenButton;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);



         rootLayout = (ViewGroup)findViewById(R.id.root_layout);
         detailContainer = (ViewGroup)findViewById(R.id.detail_container);
         fullnameEt = (EditText)findViewById(R.id.fullnameEt);
         emailEt = (EditText)findViewById(R.id.emailEt);
         usernameEt = (EditText)findViewById(R.id.usernameEt);
         passwordEt = (EditText)findViewById(R.id.passwordEt);
        loginButton = (Button)findViewById(R.id.btnLogin);
        RegisterButton = (Button)findViewById(R.id.btnRegister);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        loginButton.setOnClickListener(this);
        RegisterButton.setOnClickListener(this);
        linkToRegisterScreenButton = (Button)findViewById(R.id.btnLinkToRegisterScreen);
        linkToRegisterScreenButton.setOnClickListener(this);
        linkToLoginScreenButton = (Button)findViewById(btnLinkToLoginScreen);
        linkToLoginScreenButton.setOnClickListener(this);
        mApiBAAS=ApiBAAS.getInstance(this);

    }


    void loginScreen(){
        fullnameEt.setVisibility(View.GONE);
        usernameEt.setVisibility(View.GONE);
        loginButton.setVisibility(View.VISIBLE);
        RegisterButton.setVisibility(View.GONE);

        linkToLoginScreenButton.setVisibility(View.GONE);
        linkToRegisterScreenButton.setVisibility(View.VISIBLE);
    }

    void registerScreen(){

        fullnameEt.setVisibility(View.VISIBLE);
        usernameEt.setVisibility(View.VISIBLE);
        loginButton.setVisibility(View.GONE);
        RegisterButton.setVisibility(View.VISIBLE);

        linkToLoginScreenButton.setVisibility(View.VISIBLE);
        linkToRegisterScreenButton.setVisibility(View.GONE);
    }
    @Override
    public void onClick(View view) {


        if(view.getId() == R.id.btnLinkToRegisterScreen){
          registerScreen();
        }



        if(view.getId() == R.id. btnLinkToLoginScreen){
        loginScreen();
        }


        if(view.getId() == R.id.btnLogin){
            // login user

            String email = emailEt.getText().toString().trim();
            String password = passwordEt.getText().toString().trim();

            View focusView=null;
            if(!ValidationHelper.isPasswordValid(password)){
                focusView=passwordEt;
                passwordEt.setError("Password must contain between 6 and 20 characters!");
            }
            if(!ValidationHelper.isEmailValid(email)){
                focusView=emailEt;
                emailEt.setError("Email is not valid!");
            }

            if(focusView!=null){
                // we have an error
                focusView.requestFocus();

            }else {
                // try to authorize user
                progressBar.setVisibility(View.VISIBLE);
                mApiBAAS.getApigeeDataClient().authorizeAppUserAsync(email, password, new ApiResponseCallback() {
                    @Override
                    public void onResponse(ApiResponse apiResponse) {
                        if (!apiResponse.completedSuccessfully()) {
                            String error = apiResponse.getError();
                            switch (error) {
                                case "invalid_grant":
                                    Toast.makeText(RegisterActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    Toast.makeText(RegisterActivity.this, "Could not log You in", Toast.LENGTH_SHORT).show();
                            }
                            progressBar.setVisibility(View.GONE);
                        } else {
                            // save user data
                            VuGraphUser currentUser = new VuGraphUser(apiResponse.getUser());
                            currentUser.setAccessToken(apiResponse.getAccessToken());
                            String rawResponse = apiResponse.getRawResponse();
                            long expiresAt = -1;
                            try {
                                JSONObject responseJson = new JSONObject(rawResponse);
                                expiresAt = responseJson.getLong("expires_in") * 1000 + System.currentTimeMillis();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            currentUser.setExpiresAt(expiresAt - 60000);
                            //currentUser.setExpiresAt(apiResponse);
                            PreferencesUtils.setCurrentUser(currentUser);
                            VuGraphUser testUser = PreferencesUtils.getCurrentUser();
                            Toast.makeText(RegisterActivity.this, testUser.toString(), Toast.LENGTH_SHORT).show();


                            // navigate to the main activity
                            Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
                            // we don't want user to get back to login screen
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            progressBar.setVisibility(View.GONE);
                        }

                    }

                    @Override
                    public void onException(Exception e) {
                        Toast.makeText(RegisterActivity.this, e.toString(), Toast.LENGTH_SHORT).show();

                    }
                });
            }
        }



        if(view.getId() == R.id.btnRegister){

            String fullname = fullnameEt.getText().toString().trim();
            String username = usernameEt.getText().toString().trim();
            String email = emailEt.getText().toString().trim();
            String password = passwordEt.getText().toString().trim();

            // validate input
            View focusView=null;
            if(!ValidationHelper.isUsernameValid(username)){
                focusView=usernameEt;
                usernameEt.setError("Username must containt between 6 and 20 characters or digits!");
            }
            if(!ValidationHelper.isFullnameValid(fullname)){
                focusView=fullnameEt;
                fullnameEt.setError("Please enter valid full name!");
            }
            if(!ValidationHelper.isPasswordValid(password)){
                focusView=passwordEt;
                passwordEt.setError("Password must contain between 6 and 20 characters!");
            }
            if(!ValidationHelper.isEmailValid(email)){
                focusView=emailEt;
                emailEt.setError("Email is not valid!");
            }

            if(focusView!=null){
                // we have an error
                focusView.requestFocus();

            }else {
                //register user
                mApiBAAS.getApigeeDataClient().createUserAsync(username, fullname, email, password, new ApiResponseCallback() {
                    @Override
                    public void onResponse(ApiResponse apiResponse) {
                        if (!apiResponse.completedSuccessfully()) {
                            // error
                            String err = apiResponse.getErrorDescription();
                            String error = apiResponse.getError();
                            switch (error) {
                                case "duplicate_unique_property_exists":
                                    Toast.makeText(RegisterActivity.this, "Username or Email is already taken!", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                            Toast.makeText(RegisterActivity.this, err, Toast.LENGTH_SHORT).show();
                        } else {
                            // registered succesfully
                            Snackbar snackbar = Snackbar.make(rootLayout, "Registered Succefully!", Snackbar.LENGTH_LONG);
                            snackbar.getView().setBackgroundColor(getResources().getColor(R.color.colorAccent));
                            snackbar.setActionTextColor(getResources().getColor(R.color.white));
                            snackbar.show();

                            // navigate to login screen
                                loginScreen();
                        }

                    }

                    @Override
                    public void onException(Exception e) {
                        Toast.makeText(RegisterActivity.this, e.toString(), Toast.LENGTH_SHORT).show();

                    }
                });
            }

        }

    }
}
