package com.lawrence.sweeper.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.lawrence.sweeper.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class GameFragment extends Fragment {

    private GameCanvas gameCanvas;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //setRetainInstance(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //homeViewModel = ViewModel.of(this).get(HomeViewModel.class);

        final View root = inflater.inflate(R.layout.fragment_canvas, container, false);
        gameCanvas = root.findViewById(R.id.custom_canvas);

        root.post(new Runnable() {
            @Override
            public void run() {
                // for instance
                gameCanvas.setDisplaySize(root.getMeasuredWidth(), root.getMeasuredHeight());
            }
        });

        /*final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/
        return root;
    }


    public String testCall(){
        return "Hello World!";
    }
}
