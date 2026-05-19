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

public class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.ViewHolder> {
    private List<StudyItem> items;
    private Context context;
    private DBHelper dbHelper;

    public RecentAdapter(Context context, List<StudyItem> items, DBHelper dbHelper) {
        this.context = context;
        this.items = items;
        this.dbHelper = dbHelper;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_recent_card, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        StudyItem item = items.get(position);
        holder.name.setText(item.name);
        
        int lastPage = dbHelper.getLastPage(item.id);
        holder.pageInfo.setText("Page: " + (lastPage + 1));

        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, PdfViewActivity.class);
            i.putExtra("pdf_uri", item.path);
            i.putExtra("item_id", item.id);
            context.startActivity(i);
        });

        if (item.path != null) {
            try {
                Bitmap b = ThumbnailUtils.getPdfThumbnail(context, Uri.parse(item.path));
                if (b != null) holder.img.setImageBitmap(b);
                else holder.img.setImageResource(android.R.drawable.ic_menu_report_image);
            } catch (Exception e) {
                holder.img.setImageResource(android.R.drawable.ic_menu_report_image);
            }
        }
    }

    @Override
    public int getItemCount() { 
        return items.size(); 
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, pageInfo;
        ImageView img;
        public ViewHolder(View v) { 
            super(v); 
            name = v.findViewById(R.id.itemName); 
            img = v.findViewById(R.id.itemThumbnail);
            pageInfo = v.findViewById(R.id.itemPageInfo);
        }
    }
}