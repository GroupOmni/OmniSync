package com.groupomni.omnisync;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;


public class FilesFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OmniSyncApplication app;

    public FilesFragment() {
    }

    public static FilesFragment newInstance(String path) {
        FilesFragment fragment = new FilesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, path);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }

        app = (OmniSyncApplication) requireActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_files, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        TextView noFilesText = view.findViewById(R.id.nofiles_textview);

        Log.d("FRAGMENT FILES", "folder viewer root : " + app.syncFolder);
        String path;
        if(app.syncFolder != null) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + app.syncFolder.split(":")[2];
            Log.d("FRAGMENT FILES", path);
        }else{
            return view;
        }

        File root = new File(path);
        File[] filesAndFolders = root.listFiles();

        if (filesAndFolders == null || filesAndFolders.length == 0) {
            noFilesText.setVisibility(View.VISIBLE);
            return view;
        }

        noFilesText.setVisibility(View.INVISIBLE);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(new FileAdapter(requireContext(), filesAndFolders));

        return view;
    }

}