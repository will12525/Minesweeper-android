package com.lawrence.sweeper.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.lawrence.sweeper.R;

public class GameFragment extends Fragment {

   /* @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity mainActivity = (MainActivity)getActivity();
        if(mainActivity!=null){
            mainActivity.resetGame();
        }
    }*/


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //homeViewModel = ViewModel.of(this).get(HomeViewModel.class);

        final View root = inflater.inflate(R.layout.fragment_canvas, container, false);
        final GameCanvas gameCanvas = root.findViewById(R.id.custom_canvas);

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

   /* @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MainActivity mainActivity = (MainActivity)getActivity();
        if(mainActivity!=null){
            mainActivity.resetGame();
        }
    }*/


}
