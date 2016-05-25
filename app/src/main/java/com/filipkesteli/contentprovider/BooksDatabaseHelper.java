package com.filipkesteli.contentprovider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by programer on 25.5.2016..
 */

//Prilagodavamo tablicu

public class BooksDatabaseHelper extends SQLiteOpenHelper {

    //konstante u onCreate-u
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "books.db";

    //Nasa tablica -> to cemo jedino mijenjati u ovome templateu
    private static final String TABLE_NAME = "books";
    private static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_AUTHOR = "author";

    private static final String CREATE_SQL = "create table " + TABLE_NAME +
            "(" + COLUMN_ID + " integer primary key autoincrement, " +
            COLUMN_TITLE + " text not null, " +
            COLUMN_AUTHOR + " text not null);";

    private static final String DROP_TABLE = "drop table " + TABLE_NAME + ";";

    //iskonfigurirat cemo klasu (konstruktor) da radi ono sto mi zelimo
    //Ja cu biti kontroler baze, ne netko izvana
    //Prvi put ce se pozvati onCreate, a kasnije nikad
    //on odlucuje uz pomoc parametra DB_VERSION
    //Kreiraj mi ovu bazu i zakucaj mi da je to verzija DB_VERSION
    //U MANIFEST-u cemo mijenjati verziju baze
    //Nakon verzije 2, poziva se metoda onUpgrade
    //Inace bi sacuvali podatke u temporary tablicama, ali ovdje cemo radi jednostavnosti samo zdropati
    public BooksDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    //Skreirat ce neki objekt koji ce kreirati bazu
    @Override
    public void onCreate(SQLiteDatabase db) {
        //u onCreateu trebamo kreirati tablicu:
        db.execSQL(CREATE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Ovdje upgradeamo tablicu (ovo inace nije dobro jer najprije unistavamo tablicu pa kreiramo novu)
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }

    //Na temelju URI-ja ce ContentProvider... ali ovdje ga necemo koristiti
    //getReadable i getWriteable database:
    public int delete(String id) {
        if (id == null) {
            return getWritableDatabase().delete(TABLE_NAME, null, null);
        } else {
            //prepared statements -> prima upitnike
            return getWritableDatabase().delete(TABLE_NAME, "_id=?",
                    new String[] {id});
        }
    }

    //ti ovdje mozes mijenjati samo preko id-ja
    //URI nam uopce ne treba
    //mi cemo stvarati WHERE statemente ako ce trebati
    //ContentValues je mapa key-value
    public int update(String id, ContentValues values) {
        if (id == null) {
            return getWritableDatabase().update(TABLE_NAME, values, null, null);
        } else {
            //prepared statements -> prima upitnike
            return getWritableDatabase().update(TABLE_NAME, values, "_id=?",
                    new String[]{id});
        }
    }

    //Content provideru je to pljuga trznut ako sve imamo prekopirano
    //mi cemo vratiti id, a ne uri
    public long insert(ContentValues values) {
        return getWritableDatabase().insert(TABLE_NAME, null, values);
    }

    //Dal cemo traziti jednoga ili sve:
    //Cursor je pointer na prvi record -> moveToFirst
    //opet necemo raditi statemente, nego QUERY BUILDER:
    public Cursor query(String id, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        sqLiteQueryBuilder.setTables(TABLE_NAME);
        if (id != null) {
            sqLiteQueryBuilder.appendWhere("_id = " + id);
        }

        Cursor cursor = sqLiteQueryBuilder.query(
                getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        return cursor;
    }
}
