package org.wordpress.pioupiou.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.wordpress.android.fluxc.Dispatcher;
import org.wordpress.android.fluxc.generated.AccountActionBuilder;
import org.wordpress.android.fluxc.generated.AuthenticationActionBuilder;
import org.wordpress.android.fluxc.generated.SiteActionBuilder;
import org.wordpress.android.fluxc.store.AccountStore;
import org.wordpress.android.fluxc.store.AccountStore.OnDiscoveryResponse;
import org.wordpress.android.fluxc.store.AccountStore.AuthenticatePayload;
import org.wordpress.android.fluxc.store.AccountStore.OnAuthenticationChanged;
import org.wordpress.android.fluxc.store.SiteStore;
import org.wordpress.android.fluxc.store.SiteStore.OnSiteChanged;
import org.wordpress.android.fluxc.store.SiteStore.OnURLChecked;
import org.wordpress.pioupiou.R;
import org.wordpress.pioupiou.misc.PioupiouApp;
import org.wordpress.pioupiou.postlist.PostListActivity;

import javax.inject.Inject;

import timber.log.Timber;

import static org.wordpress.android.fluxc.store.AccountStore.AuthenticationErrorType.NEEDS_2FA;

public class LoginActivity extends Activity {
    // UI references
    private TextView mHelpView;
    private EditText mUrlView;
    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText mAuthCodeView;
    private Button mNextButton;
    private Button mLogInButton;
    private Button mLogInWithCodeButton;
    private View mImageEggView;

    // State
    private boolean mUrlValidated;
    private boolean mUrlIsWPCom;
    private String mUrl;
    private String mXMLRPCUrl;

    // FluxC
    @Inject Dispatcher mDispatcher;
    @Inject AccountStore mAccountStore;
    @Inject SiteStore mSiteStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Injection
        ((PioupiouApp) getApplication()).component().inject(this);

        // If the user has an access token or a self hosted site, we consider they're logged in.
        if (mAccountStore.hasAccessToken() || mSiteStore.hasSelfHostedSite()) {
            showPostListAndFinish();
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

    public void bindTwoFactorAuthFragmentReferences(View view) {
        mAuthCodeView = (EditText) view.findViewById(R.id.two_factor_code);
        mAuthCodeView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.checkTwoFactorCode || id == EditorInfo.IME_NULL) {
                    attempt2FALogin();
                    return true;
                }
                return false;
            }
        });

        mLogInWithCodeButton = (Button) view.findViewById(R.id.login_button);
        mLogInWithCodeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attempt2FALogin();
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
            AuthenticatePayload payload = new AuthenticatePayload(mEmailView.getText().toString(),
                    mPasswordView.getText().toString());
            mDispatcher.dispatch(AuthenticationActionBuilder.newAuthenticateAction(payload));
        } else {
            Timber.i("Start login process using XMLRPC API on: " + mUrl);
            // Self Hosted login

            // trigger the discovery process here (if not mUrlIsWPCom, we want to make sure it's a self hosted
            // site and not a random site.)
            SiteStore.RefreshSitesXMLRPCPayload payload = new SiteStore.RefreshSitesXMLRPCPayload();
            payload.url = mUrl;
            payload.username = mEmailView.getText().toString();
            payload.password = mPasswordView.getText().toString();
            mDispatcher.dispatch(AuthenticationActionBuilder.newDiscoverEndpointAction(payload));
        }
    }

    private void showPostListAndFinish() {
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

    private void set2FAFieldsVisible(boolean visible) {
        getFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out,
                        android.R.animator.fade_in, android.R.animator.fade_out)
                .replace(R.id.fragment_container, new TwoFactorAuthFragment()).addToBackStack(null).commit();
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

    private void attempt2FALogin() {
        AuthenticatePayload payload = new AuthenticatePayload(mEmailView.getText().toString(),
                mPasswordView.getText().toString());
        payload.twoStepCode = mAuthCodeView.getText().toString();
        payload.shouldSendTwoStepSms = false;
        mDispatcher.dispatch(AuthenticationActionBuilder.newAuthenticateAction(payload));
    }

    // FluxC Events

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUrlChecked(OnURLChecked event) {
        mUrlView.setEnabled(true);
        setProgressVisible(false);

        if (event.isError()) {
            Timber.w("onUrlChecked error: " + event.error.type);
            mUrlView.setError(getText(R.string.error_invalid_url));
        } else {
            mUrl = event.url;
            mUrlIsWPCom = event.isWPCom;
            mUrlValidated = true;
            Timber.i("Found a " + (mUrlIsWPCom ? "WPCom" : "Self Hosted or non WordPress") + " site on: " + mUrl);
            setEmailPasswordFieldsVisible(true);
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAuthenticationChanged(OnAuthenticationChanged event) {
        if (event.isError()) {
            if (event.error.type == NEEDS_2FA) {
                Timber.i("onAuthenticationChanged error needs 2FA code");
                set2FAFieldsVisible(true);
            } else {
                Timber.i("onAuthenticationChanged error " + event.error.message);
                Toast.makeText(this, event.error.message, Toast.LENGTH_SHORT).show();
            }
        } else {
            mDispatcher.dispatch(AccountActionBuilder.newFetchAccountAction());
            mDispatcher.dispatch(AccountActionBuilder.newFetchSettingsAction());
            mDispatcher.dispatch(SiteActionBuilder.newFetchSitesAction());
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSiteChanged(OnSiteChanged event) {
        if (!event.isError()) {
            showPostListAndFinish();
        } else {
            // TODO
            Timber.i("onSiteChanged error");
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDiscoveryChanged(OnDiscoveryResponse event) {
        if (!event.isError()) {
            Timber.i("onDiscoveryChanged success " + event.xmlRpcEndpoint);

            mXMLRPCUrl = event.xmlRpcEndpoint;

            if (!TextUtils.isEmpty(mXMLRPCUrl)) {
                // now check sites
                SiteStore.RefreshSitesXMLRPCPayload refreshSitesXMLRPCPayload =
                        new SiteStore.RefreshSitesXMLRPCPayload();
                refreshSitesXMLRPCPayload.username = mEmailView.getText().toString();
                refreshSitesXMLRPCPayload.password = mPasswordView.getText().toString();
                refreshSitesXMLRPCPayload.url = mXMLRPCUrl;

                mDispatcher.dispatch(SiteActionBuilder.newFetchSitesXmlRpcAction(refreshSitesXMLRPCPayload));
            } else {
                // TODO show some error
                Timber.i("attempt login but we don't have a XMLRPC url -  error");
            }
        } else {
            // TODO show error
            Timber.i("onDiscoveryChanged error");
        }
    }
}

