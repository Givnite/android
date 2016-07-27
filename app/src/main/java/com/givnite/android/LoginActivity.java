package com.givnite.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.internal.ImageRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.givnite.android.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private CallbackManager callbackManager;
    private final String TAG_LOG = LoginActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        initFacebook();
        LoginButton mButtonLogin = (LoginButton) findViewById(R.id.connectWithFbButton);
        mButtonLogin.setReadPermissions("email", "public_profile");

    }

    private void initFacebook(){
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        firebaseAuthWithFacebook(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.v(TAG_LOG,"Login cancellato con successo !!");
                        Toast.makeText(LoginActivity.this,"Login cancellato",Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.v(TAG_LOG,"Errore grave !!");
                        Toast.makeText(LoginActivity.this,"Errore grave",Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void firebaseAuthWithFacebook(AccessToken token){
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        GraphRequest graphRequest= GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {

                if (object != null) {
                    try {
                        String id = object.getString("id");
                        String name = object.getString("name");
                        String email = object.getString("email");
                        String profileImageUrl = ImageRequest.getProfilePictureUri(id, 500, 500).toString();

                        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                        mDatabase.child("user").child(id).child("name").setValue(name);
                        mDatabase.child("user").child(id).child("email").setValue(email);
                        mDatabase.child("user").child(id).child("picture_url").setValue(profileImageUrl);

                        Intent intent = new Intent(LoginActivity.this, BaseActivity.class);
                        startActivity(intent);
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }
}
