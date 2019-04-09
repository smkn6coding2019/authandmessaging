package com.companies.lte.cobafirebase

import android.content.ContentValues
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.gms.common.ConnectionResult
import kotlinx.android.synthetic.main.activity_main.*
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.*
import com.google.firebase.iid.FirebaseInstanceId


class MainActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    private var REQ_CODE: Int = 3
    private var googleApiClient: GoogleApiClient? = null
    private var mAuth: FirebaseAuth? = null
    private var mAuthStateListener: FirebaseAuth.AuthStateListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prof_section.setVisibility(View.GONE)

        mAuth = FirebaseAuth.getInstance()

        mAuthStateListener = FirebaseAuth.AuthStateListener() {
            fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {

            }
        };

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(ContentValues.TAG, "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token

                // Log and toast
                Log.d(ContentValues.TAG, "google token > "+token)
                Toast.makeText(baseContext, token, Toast.LENGTH_SHORT).show()
            })

        val signInOptions : GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();

        googleApiClient = GoogleApiClient.Builder(this).enableAutoManage(this, this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions).build();

        btn_sign_out.setOnClickListener{ signOut() }
        btn_login.setOnClickListener { signIn() }

    }

    private fun signOut() {
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(object : ResultCallback<Status> {
            override fun onResult(status: Status) {
                updateUI(false)
            }
        })
    }

    private fun signIn() {
        val intent : Intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(intent, REQ_CODE);
    }

    private fun handleResult(result: GoogleSignInResult) {

        if (result.isSuccess()) {
            val account : GoogleSignInAccount = result.getSignInAccount()!!
            val credential : AuthCredential = GoogleAuthProvider.getCredential(account.getIdToken(), null)

            mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d("login", "success")
                        val firebaseUser : FirebaseUser = mAuth!!.getCurrentUser()!!;
                        txt_name.setText(firebaseUser.getDisplayName());
                        txt_email.setText(firebaseUser.getEmail());

                        Glide.with(this).load(firebaseUser.getPhotoUrl().toString()).into(img_profile);
                        updateUI(true);
                    } else {
                        Log.d("login", "false")
                        updateUI(false);
                    }
                }
        } else {
            Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show();
        }
    }

    private fun updateUI(isLogin: Boolean) {
        if (isLogin) {
            prof_section.setVisibility(View.VISIBLE);
            btn_login.setVisibility(View.GONE);
        } else {
            prof_section.setVisibility(View.GONE)
            btn_login.setVisibility(View.VISIBLE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CODE) {
            val result: GoogleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            handleResult(result)
        }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {

    }
}

