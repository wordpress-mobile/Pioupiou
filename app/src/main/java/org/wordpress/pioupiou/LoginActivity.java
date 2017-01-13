package org.wordpress.pioupiou;

import android.app.Activity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.wordpress.android.fluxc.Dispatcher;
import org.wordpress.android.fluxc.generated.SiteActionBuilder;
import org.wordpress.android.fluxc.store.AccountStore;
import org.wordpress.android.fluxc.store.SiteStore;
import org.wordpress.android.fluxc.store.SiteStore.OnURLChecked;

import javax.inject.Inject;

import timber.log.Timber;

public class LoginActivity extends Activity {
    // UI references
    private TextView mHelpView;
    private AutoCompleteTextView mUrlView;
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private Button mLogInButton;
    private View mProgressView;
    private View mImageEggView;

    // State
    private boolean mUrlValidated;
    private boolean mUrlIsWPCom;
    private String mUrl;

    // FluxC
    @Inject Dispatcher mDispatcher;
    @Inject AccountStore mAccountStore;
    @Inject SiteStore mSiteStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inject stuff
        ((PioupiouApp) getApplication()).component().inject(this);

        // If the user has an access token or a self hosted site, we consider they're logged in.
        if (mAccountStore.hasAccessToken() || mSiteStore.hasSelfHostedSite()) {
            // TODO: Start the "Post list activity"
            finish();
        }

        // Init the layout and UI references
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

        mProgressView = findViewById(R.id.login_progress);
        mImageEggView = findViewById(R.id.image_egg);
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

    @Override
    public void onStart() {
        super.onStart();
        mDispatcher.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mDispatcher.unregister(this);
    }

    private void attemptLogin() {
        if (mUrlIsWPCom) {
            Timber.i("Start login process using WPCOM REST API on: " + mUrl);
            // WordPress.com login
        } else {
            Timber.i("Start login process using XMLRPC API on: " + mUrl);
            // Self Hosted login
        }
        // TODO: insert cool stuff here
    }

    private void checkURLField() {
        String url = mUrlView.getText().toString();
        if (!Patterns.WEB_URL.matcher(url).matches()) {
            mUrlView.setError(getText(R.string.error_invalid_url));
            return;
        }
        Timber.i("Start URL check on: " + url);
        mUrlView.setEnabled(false);
        setProgressVisible(true);
        mDispatcher.dispatch(SiteActionBuilder.newIsWpcomUrlAction(url));
    }

    // I hate fragments but we should use them instead of doing that (to get better animations at least)
    private void setEmailPasswordFieldsVisible(boolean visible) {
        mEmailView.setVisibility(visible ? View.VISIBLE : View.GONE);
        mPasswordView.setVisibility(visible ? View.VISIBLE : View.GONE);
        mUrlView.setVisibility(visible ? View.GONE : View.VISIBLE);
        mLogInButton.setText(visible ? R.string.action_login : R.string.action_next);
        mHelpView.setText(visible ? R.string.login_help_username_and_password : R.string.login_help_url);
    }

    private void setProgressVisible(final boolean visible) {
        // TODO: do we need a progress bar when we have a rotating egg?
        // mProgressView.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible) {
            mImageEggView.animate().setDuration(30000).rotationBy(30 * 360f)
                    .setInterpolator(new LinearInterpolator()).start();
            mLogInButton.setEnabled(false);
            mLogInButton.setAlpha(0.5f);
        } else {
            mImageEggView.animate().cancel();
            mLogInButton.setEnabled(true);
            mLogInButton.setAlpha(1f);
        }
    }

    // FluxC Events

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUrlChecked(OnURLChecked event) {
        mUrlView.setEnabled(true);
        setProgressVisible(false);

        if (event.isError()) {
            mUrlView.setError(getText(R.string.error_invalid_url));
        } else {
            mUrl = event.url;
            mUrlIsWPCom = event.isWPCom;
            mUrlValidated = true;
            Timber.i("Found a " + (mUrlIsWPCom ? "WPCom" : "Self Hosted or non WordPress") + " site on: " + mUrl);
            setEmailPasswordFieldsVisible(true);
            // TODO: Trigger the discovery process here (if not mUrlIsWPCom, we want to make sure it's a self hosted
            // site and not a random site.
        }
    }

    // TODO: insert cool stuff here
}

