package com.example.telehealth.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.telehealth.R;
import com.example.telehealth.helpers.FileHelper;

public class SelectAttachFileFragment extends DialogFragment {

    private TextView txtTitle;
    private Button btnPhoto, btnPDF;

    public SelectAttachFileFragment()
    {

    }

    public static SelectAttachFileFragment newInstance(String title) {
        SelectAttachFileFragment frag = new SelectAttachFileFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_attach_file, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
        txtTitle = (TextView) view.findViewById(R.id.txtTitle);
        btnPhoto = (Button) view.findViewById(R.id.btnPhoto);
        btnPDF = (Button) view.findViewById(R.id.btnPDF);

        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // FileHelper.browsePhoto(getActivity());
                dismiss();
            }
        });

        btnPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // FileHelper.browseFiles(getActivity());
                dismiss();
            }
        });
    }
}
