package com.example.registromisdeportes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ManejadorBD extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "moviles.db";
    private static final int DATABASE_VERSION = 1;
    private static final String COL_ID = "ID";
    private static final String COL_DEPORTE = "DEPORTE";
    private static final String COL_EXPLICACION = "EXPLICACION";
    private static final String TABLE_NAME = "DEPORTES";

    private static final String TABLE_NAME2 = "ACTIVIDADES";
    private static final String COL_ID2 = "ID";
    private static final String COL_IDEXT = "ID_DEPORTE";
    private static final String COL_FECHA = "FECHA";
    private static final String COL_HORA = "HORA";
    private static final String COL_LATITUD = "LATITUD";
    private static final String COL_LONGITUD = "LONGITUD";
    private static final String COL_DURACION = "DURACION";



    public ManejadorBD(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME + " (" + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_DEPORTE +  " TEXT,"
                + COL_EXPLICACION + " TEXT"
                + ")");
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME2 + " (" + COL_ID2 + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_IDEXT +  " INTEGER,"
                + COL_FECHA + " TEXT,"
                + COL_HORA + " TIME,"
                + COL_LATITUD + " DOUBLE,"
                + COL_LONGITUD + " DOUBLE,"
                + COL_DURACION + " INTEGER"
                + ")");
        sqLiteDatabase.execSQL("INSERT INTO " + TABLE_NAME + " VALUES(NULL,'Futbol','Deporte que se juega con los pies golpeando a una pelota')");
        sqLiteDatabase.execSQL("INSERT INTO " + TABLE_NAME + " VALUES(NULL,'Baloncesto','Deporte que se juega con las manos intentando meter una pelota por un aro')");
        sqLiteDatabase.execSQL("INSERT INTO " + TABLE_NAME + " VALUES(NULL,'Correr','Deporte que consiste en correr')");
    }

    public boolean crearActividad(Integer idEx,String fecha,String Hora,Double Latitud,Double Longitud, Integer Duracion){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_IDEXT, idEx);
        contentValues.put(COL_FECHA, fecha);
        contentValues.put(COL_HORA, Hora);
        contentValues.put(COL_LATITUD, Latitud);
        contentValues.put(COL_LONGITUD, Longitud);
        contentValues.put(COL_DURACION, Duracion);

        long resultado = sqLiteDatabase.insert(TABLE_NAME2, null, contentValues);

        sqLiteDatabase.close(); //Cierro la BD

        return (resultado != -1);
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
    public Cursor UltimaActividad() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT MAX("+ COL_ID2+") FROM " + TABLE_NAME2,null);
        return cursor;
    }
    public Cursor getDeporte(String id) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT "+ COL_DEPORTE+" FROM " + TABLE_NAME+" WHERE "+COL_ID+"="+id, null);
        return cursor;
    }
    public Cursor getTiempo() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        //Cursor cursor = sqLiteDatabase.rawQuery("SELECT SUM("+COL_DURACION +") FROM " + TABLE_NAME2+" WHERE "+COL_FECHA+"="+fecha, null);
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT SUM("+COL_DURACION+") FROM "+TABLE_NAME2+"  WHERE "+COL_FECHA+"='"+new SimpleDateFormat("yyyy-MM-dd").format(new Date())+"'", null);
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
