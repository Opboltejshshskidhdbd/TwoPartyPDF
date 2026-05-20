package com.twoparty.pdfreader;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
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

    // --- CLEAN CUSTOM OPTIONS DIALOG (NO MORE PX/SP CRASH) ---
    public void showOptionsDialog(StudyItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_options, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextView title = dialogView.findViewById(R.id.dialogTitle);
        TextView btnRename = dialogView.findViewById(R.id.btnRename);
        TextView btnDelete = dialogView.findViewById(R.id.btnDelete);

        title.setText(item.name);

        btnRename.setOnClickListener(v -> {
            dialog.dismiss();
            showRenameDialog(item);
        });

        btnDelete.setOnClickListener(v -> {
            dialog.dismiss();
            showDeleteConfirm(item.id);
        });

        dialog.show();
    }

    // --- FIXED RENAME WITH VALIDATION & XML PADDING ---
    private void showRenameDialog(StudyItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("✏️ Rename Item");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_input, null);
        final EditText input = view.findViewById(R.id.dialogEditText);
        input.setText(item.name);
        input.setHint("Enter new name...");
        builder.setView(view);

        builder.setPositiveButton("Save", (d, w) -> {
            String newName = input.getText().toString().trim();
            if (TextUtils.isEmpty(newName)) {
                Toast.makeText(MainActivity.this, "Name cannot be empty! ❌", Toast.LENGTH_SHORT).show();
            } else {
                dbHelper.updateItemName(item.id, newName);
                updateUI();
                Toast.makeText(MainActivity.this, "Renamed successfully! 👍", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDeleteConfirm(int id) {
        new AlertDialog.Builder(this)
            .setTitle("⚠️ Delete Confirmation")
            .setMessage("Kya aap sach me isko delete karna chahte hain?")
            .setPositiveButton("Yes, Delete", (d, w) -> {
                dbHelper.deleteItem(id);
                updateUI();
                Toast.makeText(MainActivity.this, "Item Deleted! 🗑️", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("No", null)
            .show();
    }

    // --- MODERN ACTION CHOICE DIALOG ---
    private void showAddOptionsDialog() {
        String[] opts = {"📁  Naya Folder Banayein", "📚  Nayi PDF Add Karein"};
        new AlertDialog.Builder(this)
            .setTitle("➕ Add to Library")
            .setItems(opts, (dialog, id) -> {
                if (id == 0) showCreateFolderDialog(); 
                else openFilePicker();
            }).show();
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        startActivityForResult(intent, PICK_PDF_FILE);
    }

    // --- FIXED FOLDER CREATION WITH XML PADDING ---
    private void showCreateFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📁 New Folder");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_input, null);
        final EditText input = view.findViewById(R.id.dialogEditText);
        input.setHint("Folder ka naam likhein...");
        builder.setView(view);

        builder.setPositiveButton("Create", (d, w) -> {
            String folderName = input.getText().toString().trim();
            if (TextUtils.isEmpty(folderName)) {
                Toast.makeText(MainActivity.this, "Khaali folder nahi ban sakta! ❌", Toast.LENGTH_LONG).show();
            } else {
                dbHelper.insertItem(folderName, null, currentFolderId);
                updateUI();
                Toast.makeText(MainActivity.this, "Folder ready! 🚀", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
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