package org.wordpress.pioupiou;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {
    // UI references.
    private TextView mHelpView;
    private AutoCompleteTextView mUrlView;
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private Button mLogInButton;
    private View mProgressView;
    private View mLoginFormView;

    // State
    private boolean mUrlValidated;
    private boolean mUrlIsWPCom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUrlView = (AutoCompleteTextView) findViewById(R.id.url);
        mHelpView = (TextView) findViewById(R.id.login_help);
        mUrlView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.checkUrl || id == EditorInfo.IME_NULL) {
                    checkURLField();
                    return true;
                }
                return false;
            }
        });
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mLogInButton = (Button) findViewById(R.id.login_button);
        mLogInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUrlValidated) {
                    attemptLogin();
                } else {
                    checkURLField();
                }
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    @Override
    public void onBackPressed() {
        if (mUrlValidated) {
            mUrlValidated = false;
            setEmailPasswordFieldsVisible(false);
        } else {
            super.onBackPressed();
        }
    }

    private boolean isWPComUrl(String url) {
        // TODO: check for ".wordpress.com" and then ask FluxC discovery?
        return true;
    }

    private void attemptLogin() {
        // TODO: Have fun here
    }

    private void checkURLField() {
        setProgressVisible(true);
        mUrlIsWPCom = isWPComUrl(mUrlView.getText().toString());
        mUrlValidated = true;
        setProgressVisible(false);
        setEmailPasswordFieldsVisible(true);
    }

    // I hate fragments
    private void setEmailPasswordFieldsVisible(boolean visible) {
        mEmailView.setVisibility(visible ? View.VISIBLE : View.GONE);
        mPasswordView.setVisibility(visible ? View.VISIBLE : View.GONE);
        mUrlView.setVisibility(visible ? View.GONE : View.VISIBLE);
        mLogInButton.setText(visible ? R.string.action_login : R.string.action_next);
        mHelpView.setText(visible ? R.string.login_help_username_and_password : R.string.login_help_url);
    }

    private void setProgressVisible(final boolean visible) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(visible ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                visible ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(visible ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(visible ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                visible ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        });
    }
}

