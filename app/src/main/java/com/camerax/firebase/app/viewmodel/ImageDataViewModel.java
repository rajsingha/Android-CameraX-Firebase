package com.camerax.firebase.app.viewmodel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.camerax.firebase.app.model.ImageData;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ImageDataViewModel extends ViewModel {

    private MutableLiveData<List<ImageData>> liveData;
    FirebaseFirestore db;

    public ImageDataViewModel(){
        liveData=new MutableLiveData<>();
    }

    public MutableLiveData<List<ImageData>> getLiveDataObserver(){
        return liveData;
    }

    public void fetchData(){

        List<ImageData> dataList =new ArrayList<>();

        db =FirebaseFirestore.getInstance();
        db.collection("ImageData")
                .addSnapshotListener((value, error) -> {
                    if (error!=null){
                        Log.e("FIRESTORE",error.getMessage());
                            return;

                    }

                    for (DocumentChange documentChange:value.getDocumentChanges()){
                        if (documentChange.getType() ==DocumentChange.Type.ADDED){
                             dataList.add(documentChange.getDocument().toObject(ImageData.class));
                             liveData.postValue(dataList);
                        }
                    }
                });

    }
}
