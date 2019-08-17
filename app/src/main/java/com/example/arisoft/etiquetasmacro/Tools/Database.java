package com.example.arisoft.etiquetasmacro.Tools;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Database extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "EtiquetasApp";
    public static final int DATABASE_VERSION = 1;

    public Database(Context context, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);

    }
    public static final String TABLA_CREAR = "login";


    public static final String SQL_CREAR="CREATE TABLE "+TABLA_CREAR+"(success text ," +
            "nomEmpresa text," +
            "dominio text," +
            "usuario text," +
            "contra text," +
            "almacen text, " +
            "imprimir text)";

    private static final String SQL_INICIOCREAR = "DROP TABLE IF EXISTS "+TABLA_CREAR;

    public void onCreate(SQLiteDatabase db) {
        //eliminar si existe
        db.execSQL(SQL_INICIOCREAR);

        //crear
        db.execSQL(SQL_CREAR);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*
        Log.i("basedatos",newVersion+" "+oldVersion);
        if(oldVersion==1 && newVersion>=2){
            db.execSQL(SQL_PRUEBA);
        }
        if(oldVersion>=1 && newVersion>=3)
        {
            db.execSQL(SQL_ADDCOL_PRUEBA);
        }*/

    }






}
