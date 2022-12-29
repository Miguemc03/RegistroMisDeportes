package com.example.registromisdeportes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ManejadorBD extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "moviles.db";
    private static final int DATABASE_VERSION = 1;
    private static final String COL_ID = "ID";
    private static final String COL_DEPORTE = "DEPORTE";
    private static final String COL_EXPLICACION = "EXPLICACION";
    private static final String TABLE_NAME = "DEPORTES";



    public ManejadorBD(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME + " (" + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_DEPORTE +  " TEXT,"
                + COL_EXPLICACION + " TEXT"
                + ")");
        sqLiteDatabase.execSQL("INSERT INTO " + TABLE_NAME + " VALUES(NULL,'Futbol','Deporte que se juega con los pies golpeando a una pelota')");
        sqLiteDatabase.execSQL("INSERT INTO " + TABLE_NAME + " VALUES(NULL,'Baloncesto','Deporte que se juega con las manos intentando meter una pelota por un aro')");
        sqLiteDatabase.execSQL("INSERT INTO " + TABLE_NAME + " VALUES(NULL,'Correr','Deporte que consiste en correr')");
    }


    public boolean insertar(String deporte, String explicacion) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_DEPORTE, deporte);
        contentValues.put(COL_EXPLICACION, explicacion);

        long resultado = sqLiteDatabase.insert(TABLE_NAME, null, contentValues);

        sqLiteDatabase.close(); //Cierro la BD

        return (resultado != -1); //en resultado está el número de filas afectadas
    }
    public Cursor listar() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        return cursor;
    }



    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
    public boolean actualizar(String id, String deporte, String explicacion) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_DEPORTE, deporte);
        contentValues.put(COL_EXPLICACION, explicacion);


        long resultado = sqLiteDatabase.update(TABLE_NAME, contentValues, COL_ID + "=?", new String[]{id});
        sqLiteDatabase.close();

        return (resultado > 0);

    }
    public boolean borrar(String id) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        int borradas = sqLiteDatabase.delete( TABLE_NAME,COL_ID+"=?",new String[]{id});
        sqLiteDatabase.close();
        return borradas>0;

    }
}
