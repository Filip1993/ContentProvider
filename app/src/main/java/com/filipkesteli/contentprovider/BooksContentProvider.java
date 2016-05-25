package com.filipkesteli.contentprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import java.net.URI;

public class BooksContentProvider extends ContentProvider {

    private BooksDatabaseHelper booksDatabaseHelper;

    private static final String AUTHORITY = "com.filipkesteli.contentprovider" ;
    private static final String PATH = "books" ; //MOZE ODGOVORITI na pitanja vise tablica, ne samo za jednu
    //glupo je imati vise baza unutar jedne aplikacije

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);

    //trebam li jedan record ili sve recorde:
    private static final int BOOKS = 10;
    private static final int BOOK_ID = 20;

    //UriMatcher:
    private static final UriMatcher URI_MATCHER = createUriMatcher();

    //pita a koji si URI
    private static UriMatcher createUriMatcher() {
        //ovakve tipove urija, pa onakve...
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        //dodajemo 2 urija:
        uriMatcher.addURI(AUTHORITY, PATH, BOOKS);
        // "/#" ocekuje id:
        uriMatcher.addURI(AUTHORITY, PATH + "/#", BOOK_ID);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        //ima insert, itd...
        //moze biti i handler za xml...
        booksDatabaseHelper = new BooksDatabaseHelper(getContext());
        return true;
    }

    public BooksContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        //da li deletamo 1 record ili sve recorde -> provjeravamo preko URIja:
        String id = null;
        if (URI_MATCHER.match(uri) == BOOK_ID) {
            id = uri.getPathSegments().get(1); //izvukao sam sad id
        }

        return booksDatabaseHelper.delete(id);
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        long id = booksDatabaseHelper.insert(values);

        return ContentUris.withAppendedId(CONTENT_URI, id);

    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        String id = null;
        if (URI_MATCHER.match(uri) == BOOK_ID) {
            id = uri.getPathSegments().get(1); //izvukao sam sad id
        }

        return booksDatabaseHelper.query(id, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        //da li deletamo 1 record ili sve recorde -> provjeravamo preko URIja:
        String id = null;
        if (URI_MATCHER.match(uri) == BOOK_ID) {
            id = uri.getPathSegments().get(1); //izvukao sam sad id
        }

        return booksDatabaseHelper.update(id, values);
    }
}
