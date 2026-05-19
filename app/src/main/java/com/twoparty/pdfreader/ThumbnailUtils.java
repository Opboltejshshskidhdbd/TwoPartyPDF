package com.twoparty.pdfreader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

public class ThumbnailUtils {
    public static Bitmap getPdfThumbnail(Context context, Uri uri) {
        try {
            // PDF file ko read mode mein kholna
            ParcelFileDescriptor fd = context.getContentResolver().openFileDescriptor(uri, "r");
            if (fd != null) {
                PdfRenderer renderer = new PdfRenderer(fd);
                // Pehla page (Index 0)
                PdfRenderer.Page page = renderer.openPage(0);
                
                // Card ke size ke hisaab se bitmap banana
                Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                
                // Page ko render karna
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                
                page.close();
                renderer.close();
                return bitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
