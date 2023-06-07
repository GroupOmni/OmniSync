package com.groupomni.omnisync;

import static android.content.Intent.getIntent;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;


public class FilesFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OmniSyncApplication app;

    public FilesFragment() {
        // Required empty public constructor
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_files, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        TextView noFilesText = view.findViewById(R.id.nofiles_textview);

//        assert getArguments() != null;
        Log.d("DIRECTORY", "folder viewer root : " + app.syncFolder);
        String path;
        if (mParam1 == null) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/OmniSync";
        }else{
            path = mParam1;
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