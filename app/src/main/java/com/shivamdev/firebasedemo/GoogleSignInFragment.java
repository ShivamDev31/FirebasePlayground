package com.shivamdev.firebasedemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * Created by shivam on 24/8/16.
 */

public class GoogleSignInFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = GoogleSignInFragment.class.getSimpleName();
    private static final int RC_SIGN_IN = 1001;

    private GoogleApiClient googleApiClient;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private SignInButton signInButton;
    private Button bSignOut;
    private Button bRevokeAccess;
    private TextView tvUserLoginDetails;
    private ProgressDialog progressDialog;

    public static GoogleSignInFragment newInstance() {
        GoogleSignInFragment fragment = new GoogleSignInFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.google_sign_in_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        signInButton = (SignInButton) view.findViewById(R.id.sib_google);
        bSignOut = (Button) view.findViewById(R.id.b_sign_out);
        bRevokeAccess = (Button) view.findViewById(R.id.b_revoke_access);
        tvUserLoginDetails = (TextView) view.findViewById(R.id.tv_user_details);
        progressDialog = new ProgressDialog(getActivity());
        signInButton.setOnClickListener(this);
        bSignOut.setOnClickListener(this);
        bRevokeAccess.setOnClickListener(this);
        addGoogleSignInOptions();
    }

    private void addGoogleSignInOptions() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage(getActivity(), this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "Signed in : " + user.getUid());
                } else {
                    Log.d(TAG, "Signed out");
                }

                updateUi(user);
            }
        };
    }

    private void signIn() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount googleSignInAccount = result.getSignInAccount();
                firebaseAuthWithGoogle(googleSignInAccount);
                Toast.makeText(getActivity(), "Accounts fetching successful.", Toast.LENGTH_LONG).show();
            } else {
                updateUi(null);
                Toast.makeText(getActivity(), "Unable to fetch accounts. Please try again", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        progressDialog.setMessage("Signing in into your account");
        progressDialog.show();

        AuthCredential authCredential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(authCredential).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "Signin complete, status : " + task.isSuccessful());
                progressDialog.dismiss();
                if (task.isSuccessful()) {

                } else {
                    Toast.makeText(getActivity(), "Unable to signin, Please try again.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void revokeAccess() {
        mAuth.signOut();
        Auth.GoogleSignInApi.revokeAccess(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                updateUi(null);
            }
        });
    }

    private void signOut() {
        mAuth.signOut();

        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                updateUi(null);
            }
        });
    }

    private void updateUi(FirebaseUser user) {
        if (user != null) {
            bSignOut.setVisibility(View.VISIBLE);
            bRevokeAccess.setVisibility(View.VISIBLE);
            signInButton.setVisibility(View.GONE);
            tvUserLoginDetails.setText("User Name : " + user.getDisplayName() + "\nUser email : " + user.getEmail()
                    + "\nUserId : " + user.getUid());
        } else {
            bSignOut.setVisibility(View.GONE);
            bRevokeAccess.setVisibility(View.GONE);
            signInButton.setVisibility(View.VISIBLE);
            tvUserLoginDetails.setText("Please login to get user details");
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed : " + connectionResult);
        Toast.makeText(getActivity(), "Playstore connection failed, Please try again.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sib_google:
                signIn();
                break;
            case R.id.b_sign_out:
                signOut();
                break;
            case R.id.b_revoke_access:
                revokeAccess();
                break;
        }
    }
}
