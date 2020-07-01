package com.example.roome.roommate_searcher_activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.roome.R;
import com.example.roome.user_classes.ApartmentSearcherUser;
import com.example.roome.utils.FirebaseMediate;
import com.example.roome.utils.UsersImageConnector;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * This class is a recycler adapter used for the Matches fragment which contains a recycler for
 * viewing all matched apartments
 */
public class RecyclerAdapterRS extends RecyclerView.Adapter<RecyclerAdapterRS.ImageViewHolder> {
    private ArrayList<String> ids;

    /**
     * Constructor of the class. Receives an array of ints representing the images
     *
     * @param uids
     */
    public RecyclerAdapterRS(ArrayList<String> uids) {
        this.ids = uids;
    }

    /**
     * on create method for the ImageViewHolder
     *
     * @param parent   the parent
     * @param viewType the view type
     * @return an ImageViewHolder
     */
    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_layout, parent, false);
        ImageViewHolder imageViewHolder = new ImageViewHolder(view);
        return imageViewHolder;
    }

    /**
     * The method's purpose is to present a matched apartment in the recycler view - the photo of
     * the apartment and some relevant info
     *
     * @param holder   ImageViewHolder
     * @param position int representing the position of certain image
     */
    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        String uid = ids.get(position);
        int id_image = UsersImageConnector.getImageByUid(uid, UsersImageConnector.APARTMENT_USER);
        holder.album.setImageResource(id_image);

        ApartmentSearcherUser apartmentSearcher = FirebaseMediate.getApartmentSearcherUserByUid(uid);
        String firstNmae = apartmentSearcher.getFirstName();
        String lastName = apartmentSearcher.getLastName();
        String phone = apartmentSearcher.getPhoneNumber();
        holder.albumTitle.setText("Name: " + firstNmae + " " + lastName  + "\nPhone Number: " + phone);
    }

    /**
     * @return the number of images in the image list
     */
    @Override
    public int getItemCount() {
        return ids.size();
    }

    /**
     * Static class representing the album (a matched apartment) and its title (some apartment info)
     */
    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        public ImageView album;
        public TextView albumTitle;

        /**
         * Constructor for the ImageViewHolder class. Binds the fields to their view.
         *
         * @param itemView view object
         */
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            album = itemView.findViewById(R.id.iv_album);
            albumTitle = itemView.findViewById(R.id.tv_album_title);
        }
    }
}

