package com.lawrence.sweeper.ui.records;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.lawrence.sweeper.MainActivity;
import com.lawrence.sweeper.R;

public class RecordsFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        RecordsViewModel recordsViewModel = new ViewModelProvider(this).get(RecordsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_records, container, false);
        final TextView textView = root.findViewById(R.id.text_slideshow);
        recordsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MainActivity mainActivity = (MainActivity)getActivity();
        if(mainActivity!=null){
            mainActivity.resetGame();
        }
    }
}
