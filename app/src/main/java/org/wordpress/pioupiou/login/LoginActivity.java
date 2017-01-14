package org.wordpress.pioupiou.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
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
import org.wordpress.pioupiou.misc.PioupiouApp;
import org.wordpress.pioupiou.postlist.PostListActivity;
import org.wordpress.pioupiou.R;

import javax.inject.Inject;

import timber.log.Timber;

public class LoginActivity extends Activity {
    // UI references
    private TextView mHelpView;
    private EditText mUrlView;
    private EditText mEmailView;
    private EditText mPasswordView;
    private Button mNextButton;
    private Button mLogInButton;
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
        mHelpView = (TextView) findViewById(R.id.login_help);
        mImageEggView = findViewById(R.id.image_egg);
        getFragmentManager().beginTransaction().add(R.id.fragment_container, new URLCheckFragment()).commit();
    }

    public void bindUrlFragmentReferences(View view) {
        mUrlView = (EditText) view.findViewById(R.id.url);
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

        mNextButton = (Button) view.findViewById(R.id.login_button);
        mNextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                    checkURLField();
            }
        });
    }

    public void bindEmailPasswordFragmentReferences(View view) {
        mEmailView = (EditText) view.findViewById(R.id.email);
        mPasswordView = (EditText) view.findViewById(R.id.password);
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

        mLogInButton = (Button) view.findViewById(R.id.login_button);
        mLogInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
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
            // TODO: insert cool stuff here
        } else {
            Timber.i("Start login process using XMLRPC API on: " + mUrl);
            // Self Hosted login
            // TODO: insert cool stuff here
        }
        startActivity(new Intent(this, PostListActivity.class));
        finish();
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

    private void setEmailPasswordFieldsVisible(boolean visible) {
        getFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out,
                        android.R.animator.fade_in, android.R.animator.fade_out)
                .replace(R.id.fragment_container, new EmailPasswordFragment()).addToBackStack(null).commit();
    }

    private void setButtonEnabled(Button button, boolean enabled) {
        if (button != null) {
            button.setEnabled(enabled);
            button.setAlpha(enabled ? 1f : 0.5f);
        }
    }

    private void setProgressVisible(boolean visible) {
        // We don't need a progress bar when we have a rotating egg.
        if (visible) {
            mImageEggView.animate().setDuration(60000).rotationBy(60 * 360f)
                    .setInterpolator(new LinearInterpolator()).start();
        } else {
            mImageEggView.animate().cancel();
        }
        setButtonEnabled(mLogInButton, !visible);
        setButtonEnabled(mNextButton, !visible);
    }

    // FluxC Events

    @SuppressWarnings("unused")
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

