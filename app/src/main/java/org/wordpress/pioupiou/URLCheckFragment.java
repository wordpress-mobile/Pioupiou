package org.wordpress.pioupiou;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class URLCheckFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_url_check, container, false);
        ((LoginActivity) getActivity()).bindUrlFragmentReferences(view);
        return view;
    }
}
