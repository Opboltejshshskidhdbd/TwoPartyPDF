package com.twoparty.pdfreader;

import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FitPolicy;

public class PdfViewActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private int itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DBHelper(this);

        // Dynamic PDFView create karke layout me set kar rahe hain
        PDFView pdfView = new PDFView(this, null);
        setContentView(pdfView);

        // Intent se PDF ka URI aur unique Item ID nikal rahe hain
        String uriString = getIntent().getStringExtra("pdf_uri");
        itemId = getIntent().getIntExtra("item_id", -1);

        // Pehle se saved page database se check kar rahe hain
        int savedPage = 0;
        if (itemId != -1) {
            savedPage = dbHelper.getLastPage(itemId);
        }

        if (uriString != null) {
            Uri uri = Uri.parse(uriString);
            pdfView.fromUri(uri)
                .defaultPage(savedPage) // User wahin se shuru karega jahan choda tha
                .enableSwipe(true)
                .swipeHorizontal(true)
                .pageSnap(true)
                .pageFling(true)
                .autoSpacing(true)
                .pageFitPolicy(FitPolicy.WIDTH)
                .onPageChange((page, pageCount) -> {
                    // Jaise hi user page badlega, live database update hoga
                    if (itemId != -1) {
                        dbHelper.updateHistory(itemId, page);
                    }
                })
                .load();
        }
    }
}