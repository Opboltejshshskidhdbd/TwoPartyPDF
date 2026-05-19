package com.twoparty.pdfreader;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class StudyAdapter extends RecyclerView.Adapter<StudyAdapter.ViewHolder> {
    private List<StudyItem> items;
    private Context context;

    public StudyAdapter(Context context, List<StudyItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_card, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        StudyItem item = items.get(position);
        holder.name.setText(item.name);
        
        holder.itemView.setOnClickListener(v -> {
            if (item.path == null) {
                ((MainActivity) context).openFolder(item.id, item.name);
            } else {
                Intent i = new Intent(context, PdfViewActivity.class);
                i.putExtra("pdf_uri", item.path);
                i.putExtra("item_id", item.id);
                context.startActivity(i);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            ((MainActivity) context).showOptionsDialog(item);
            return true;
        });

        if (item.path != null) {
            try {
                Bitmap b = ThumbnailUtils.getPdfThumbnail(context, Uri.parse(item.path));
                if (b != null) holder.img.setImageBitmap(b);
                else holder.img.setImageResource(android.R.drawable.ic_menu_report_image);
            } catch (Exception e) {
                holder.img.setImageResource(android.R.drawable.ic_menu_report_image);
            }
        } else {
            holder.img.setImageResource(android.R.drawable.ic_menu_directions);
        }
    }

    @Override
    public int getItemCount() { 
        return items.size();
    }

    public void updateData(List<StudyItem> l) { 
        this.items = l;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView img;
        public ViewHolder(View v) { 
            super(v); 
            name = v.findViewById(R.id.itemName);
            img = v.findViewById(R.id.itemThumbnail);
        }
    }
}