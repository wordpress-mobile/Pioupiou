package org.wordpress.pioupiou.login;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.wordpress.pioupiou.R;

public class TwoFactorAuthFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_two_factor_auth, container, false);
        ((LoginActivity) getActivity()).bindTwoFactorAuthFragmentReferences(view);
        return view;
    }
}
