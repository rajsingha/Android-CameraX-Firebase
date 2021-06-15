package com.camerax.firebase.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.camerax.firebase.app.databinding.ImageDataItemsBinding;
import com.camerax.firebase.app.model.ImageData;

import java.util.List;


public class ImageDataAdapter extends RecyclerView.Adapter<ImageDataAdapter.ViewHolder> {

    private Context context;
    private List<ImageData> imageDataList;

    public ImageDataAdapter(Context context,List<ImageData> imageData){
        this.context =context;
        this.imageDataList =imageData;
    }

    public void setImageDataList (List<ImageData> imageDataList){
        this.imageDataList = imageDataList;
        notifyDataSetChanged();
    }

    @Override
    public ImageDataAdapter.ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        return new ImageDataAdapter
                .ViewHolder(ImageDataItemsBinding
                .inflate(LayoutInflater.from(context),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ImageDataAdapter.ViewHolder holder, int position) {
        ImageData imageData = imageDataList.get(position);
        Glide.with(context).load(imageData.getImageUrl()).into(holder.binding.imageView);
        holder.binding.title.setText(imageData.getTitle());
        holder.binding.date.setText(imageData.getDate());

    }

    @Override
    public int getItemCount() {
        if (this.imageDataList!=null){
            return this.imageDataList.size();
        }
        return 0;

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageDataItemsBinding binding;

        public ViewHolder(ImageDataItemsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
