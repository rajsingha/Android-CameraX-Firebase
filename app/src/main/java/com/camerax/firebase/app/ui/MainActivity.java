package com.camerax.firebase.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.camerax.firebase.app.adapter.ImageDataAdapter;
import com.camerax.firebase.app.databinding.ActivityMainBinding;
import com.camerax.firebase.app.model.ImageData;
import com.camerax.firebase.app.viewmodel.ImageDataViewModel;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private List<ImageData> imageDataList;
    private ImageDataAdapter adapter;
    private ImageDataViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Floating Button
        binding.cameraBtn.setOnClickListener(v -> startActivity(new Intent(this,CameraActivity.class)));

        // RecyclerView Configuration
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ImageDataAdapter(this,imageDataList);
        binding.recyclerView.setAdapter(adapter);

        //ViewModel Configuration
        viewModel= ViewModelProviders.of(this).get(ImageDataViewModel.class);
        viewModel.getLiveDataObserver().observe(this, imageData -> {
            if (imageData!=null){
                imageDataList = imageData;
                adapter.setImageDataList(imageDataList); //Setting the model data to the adapter

            }
        });
        viewModel.fetchData(); //Fetch the data from CloudFirestore

    }


}