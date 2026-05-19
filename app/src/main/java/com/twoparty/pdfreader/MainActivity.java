package com.twoparty.pdfreader;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_PDF_FILE = 2;
    private DBHelper dbHelper;
    private RecyclerView recyclerView;
    private RecyclerView recentRecyclerView;
    private StudyAdapter adapter;
    private RecentAdapter recentAdapter;
    private int currentFolderId = 0;
    private Stack<Integer> folderHistory = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }

        dbHelper = new DBHelper(this);
        
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recentRecyclerView = findViewById(R.id.recentRecyclerView);
        recentRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        findViewById(R.id.fabAdd).setOnClickListener(v -> showAddOptionsDialog());
        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    public void showOptionsDialog(StudyItem item) {
        String[] options = {"Rename ✍️", "Delete 🗑️"};
        new AlertDialog.Builder(this).setItems(options, (dialog, which) -> {
            if (which == 0) showRenameDialog(item);
            else showDeleteConfirm(item.id);
        }).show();
    }

    private void showRenameDialog(StudyItem item) {
        final EditText input = new EditText(this);
        input.setText(item.name);
        new AlertDialog.Builder(this).setTitle("Rename Item").setView(input)
            .setPositiveButton("Save", (d, w) -> {
                dbHelper.updateItemName(item.id, input.getText().toString());
                updateUI();
            }).show();
    }

    private void showDeleteConfirm(int id) {
        new AlertDialog.Builder(this).setMessage("Delete?").setPositiveButton("Yes", (d, w) -> {
            dbHelper.deleteItem(id);
            updateUI();
        }).show();
    }

    private void showAddOptionsDialog() {
        String[] opts = {"Naya Folder 📁", "PDF Add Karein 📚"};
        new AlertDialog.Builder(this).setItems(opts, (dialog, id) -> {
            if (id == 0) showCreateFolderDialog(); else openFilePicker();
        }).show();
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        startActivityForResult(intent, PICK_PDF_FILE);
    }

    private void showCreateFolderDialog() {
        final EditText input = new EditText(this);
        new AlertDialog.Builder(this).setTitle("Folder Name").setView(input)
            .setPositiveButton("OK", (d, w) -> {
                dbHelper.insertItem(input.getText().toString(), null, currentFolderId);
                updateUI();
            }).show();
    }

    public void openFolder(int id, String name) {
        folderHistory.push(currentFolderId);
        currentFolderId = id;
        updateUI();
    }

    public void updateUI() {
        List<StudyItem> items = dbHelper.getItemsInFolder(currentFolderId);
        if (adapter == null) {
            adapter = new StudyAdapter(this, items);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateData(items);
        }

        List<StudyItem> recentItems = dbHelper.getRecentItems();
        if (recentItems.isEmpty()) {
            findViewById(R.id.recentTitle).setVisibility(View.GONE);
            recentRecyclerView.setVisibility(View.GONE);
        } else {
            findViewById(R.id.recentTitle).setVisibility(View.VISIBLE);
            recentRecyclerView.setVisibility(View.VISIBLE);
            
            recentAdapter = new RecentAdapter(this, recentItems, dbHelper);
            recentRecyclerView.setAdapter(recentAdapter);
        }
    }

    @Override
    public void onBackPressed() {
        if (!folderHistory.isEmpty()) { 
            currentFolderId = folderHistory.pop();
            updateUI();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PDF_FILE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            String fileName = "Unknown Book";
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index != -1) fileName = cursor.getString(index);
                cursor.close();
            }
            
            dbHelper.insertItem(fileName, uri.toString(), currentFolderId);
            updateUI();
        }
    }
}