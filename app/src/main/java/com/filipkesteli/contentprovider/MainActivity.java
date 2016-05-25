package com.filipkesteli.contentprovider;

import android.content.ContentValues;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    //cursor adapter iz baze ce se brinuti za textView...

    private EditText etTitle;
    private EditText etAuthor;
    private Button btnAdd;
    private Button btnUpdate;
    private Button btnDelete;
    private ListView lvBooks;

    private SimpleCursorAdapter adapter;
    private long selectedId = -1; //ako je id -1, onda nemam id.... stvar dogovora

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWidgets();
        initList();
        setupListeners();
    }

    private void initWidgets() {
        etAuthor = (EditText) findViewById(R.id.etAuthor);
        etTitle = (EditText) findViewById(R.id.etTitle);

        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnUpdate = (Button) findViewById(R.id.btnUpdate);
        btnDelete = (Button) findViewById(R.id.btnDelete);

        lvBooks = (ListView) findViewById(R.id.lvBooks);
    }

    private void initList() {
        //pricamo sa cursor adapterom
        //sad vucemo podatke iz jave
        String[] columns = {BooksDatabaseHelper.COLUMN_TITLE, BooksDatabaseHelper.COLUMN_AUTHOR};
        int[] viewIds = {R.id.tvTitle, R.id.tvAuthor};

        //s jedne strane gleda bazu, a s druge gleda template
        adapter = new SimpleCursorAdapter(
                this,
                R.layout.list_view,
                null,
                columns,
                viewIds,
                0);
        lvBooks.setAdapter(adapter);
        refreshList();
    }

    private void refreshList() {
        //sad ide asinchrona komunikacija
        //preko content resolvera se trebamo docepati cursora
        //u backgroundu
        //pogodit cu ti uri, a adapter ce ti popikati preko cursora
        CursorLoader cursorLoader = new CursorLoader(
                this,
                BooksContentProvider.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        //naloadaj mi to u pozadini
        Cursor cursor = cursorLoader.loadInBackground();
        adapter.swapCursor(cursor);
    }

    private void setupListeners() {
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validacija da li je form okej:
                if (formIsOK()) {
                    insert(etTitle.getText().toString(), etAuthor.getText().toString());
                }
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //prvo daj se selektiraj. pa onda da li ti je forma neprazna
                if (isItemSelected() && formIsOK()) {
                    update(etTitle.getText().toString(), etAuthor.getText().toString());
                }
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isItemSelected()) {
                    delete();
                }
            }
        });

        lvBooks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedId = id;
                fillForm();
            }
        });
    }

    private void delete() {
        //ovo mi je taj uri..
        Uri uri = Uri.parse(BooksContentProvider.CONTENT_URI + "/" + selectedId);

        //ovo je standardno pozivanje Content providera - standard
        getContentResolver().delete(
                uri,
                null,
                null);
        //moram jos refreshati listu
        //adapter ce trznut...
        refreshList();
        clearForm();
    }

    private void update(String title, String author) {
        ContentValues values = new ContentValues();
        values.put(BooksDatabaseHelper.COLUMN_TITLE, title);
        values.put(BooksDatabaseHelper.COLUMN_AUTHOR, author);

        //sad nam je uri malo drugaciji, mora imati /_id
        Uri uri = Uri.parse(BooksContentProvider.CONTENT_URI + "/" + selectedId);

        //ovo je standardno pozivanje Content providera - standard
        getContentResolver().update(
                uri,
                values,
                null,
                null);
        //moram jos refreshati listu
        //adapter ce trznut...
        refreshList();
        clearForm();
    }

    private boolean isItemSelected() {
        if (selectedId == -1) {
            Toast.makeText(MainActivity.this, R.string.please_select_book, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void insert(String title, String author) {
        ContentValues values = new ContentValues();
        values.put(BooksDatabaseHelper.COLUMN_TITLE, title);
        values.put(BooksDatabaseHelper.COLUMN_AUTHOR, author);

        //ovo je standardno pozivanje Content providera - standard
        getContentResolver().insert(
                BooksContentProvider.CONTENT_URI,
                values);
        //moram jos refreshati listu
        //adapter ce trznut...
        refreshList();
        clearForm();
    }

    private void clearForm() {
        //ja kad sam insertirao, sve ce se pocistit
        etAuthor.setText("");
        etTitle.setText("");
        selectedId = -1;
    }

    private boolean formIsOK() {
        if (etTitle.getText().toString().length() == 0) {
            Toast.makeText(MainActivity.this, R.string.please_insert_title, Toast.LENGTH_SHORT).show();
            etTitle.requestFocus();
            return false;
        }
        if (etAuthor.getText().toString().length() == 0) {
            Toast.makeText(MainActivity.this, R.string.please_insert_author, Toast.LENGTH_SHORT).show();
            etAuthor.requestFocus();
            return false;
        }
        return true;
    }

    private void fillForm() {
        //sad nam je uri malo drugaciji, mora imati /_id
        Uri uri = Uri.parse(BooksContentProvider.CONTENT_URI + "/" + selectedId);
        //naloadat ce samo podatke koji se ticu 1 recorda
        //rucno moramo napravit -> pomaknut prema njemu i procuclat ga
        CursorLoader cursorLoader = new CursorLoader(
                this,
                uri,
                null,
                null,
                null,
                null
        );
        //naloadaj mi to u pozadini
        Cursor cursor = cursorLoader.loadInBackground();

        if (cursor.moveToFirst()) {
            //NE ZNAM index te kolone -> daj mi nadi prema imenu...
            String title = cursor.getString(cursor.getColumnIndexOrThrow(BooksDatabaseHelper.COLUMN_TITLE));
            String author = cursor.getString(cursor.getColumnIndexOrThrow(BooksDatabaseHelper.COLUMN_AUTHOR));
            //tek smo read napravili, sad treba CRUD operacije
            etTitle.setText(title);
            etAuthor.setText(author);
        }
    }
}
