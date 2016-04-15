package al.ahgitdevelopment.municion;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Alberto on 12/04/2016.
 */
public class DataBaseSQLiteHelper extends SQLiteOpenHelper {
    // Table Names
    public static final String TABLE_GUIAS = "guias";
    public static final String TABLE_COMPRAS = "compras";
    public static final String TABLE_GUIAS_COMPRAS = "guias_compras";
    // Common column names
    public static final String KEY_ID = "id";
    // GUIAS Table - column names
    public static final String KEY_GUIA_NOMBRE = "nombre";
    public static final String KEY_GUIA_MARCA = "marca";
    public static final String KEY_GUIA_MODELO = "modelo";
    public static final String KEY_GUIA_NUM_GUIA = "numGuia";
    public static final String KEY_GUIA_CALIBRE = "calibre";
    public static final String KEY_GUIA_TIPO_ARMA = "tipoArma";
    public static final String KEY_GUIA_CARTUCHOS_GASTADOS = "cartuchosGastados";
    public static final String KEY_GUIA_CARTUCHOS_TOTALES = "cartuchosTotales";
    // COMPRAS Table - column names
    public static final String KEY_GUIA_ID = "idGuia";
    public static final String KEY_COMPRA_PRECIO = "precio";
    public static final String KEY_COMPRA_CARTUCHOS_COMPRADOS = "cartuchosComprados";
    //    http://www.androidhive.info/2013/09/android-sqlite-database-with-multiple-tables/
    // Logcat tag
    private static final String LOG = "DatabaseHelper";
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "DBMunicion.db";
    // Table Create Statements
    // Todo table create statement
    private static final String CREATE_TABLE_GUIA = "CREATE TABLE " + TABLE_GUIAS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
            + KEY_GUIA_NOMBRE + " TEXT NOT NULL,"
            + KEY_GUIA_MARCA + " TEXT NOT NULL,"
            + KEY_GUIA_MODELO + " TEXT NOT NULL,"
            + KEY_GUIA_NUM_GUIA + " INTEGER NOT NULL,"
            + KEY_GUIA_CALIBRE + " TEXT NOT NULL,"
            + KEY_GUIA_TIPO_ARMA + " TEXT NOT NULL,"
            + KEY_GUIA_CARTUCHOS_GASTADOS + " INTEGER NOT NULL,"
            + KEY_GUIA_CARTUCHOS_TOTALES + " INTEGER NOT NULL"
            + ")";

    // Tag table create statement
    private static final String CREATE_TABLE_COMPRA = "CREATE TABLE " + TABLE_COMPRAS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
            + KEY_GUIA_ID + " INTEGER NOT NULL,"
            + KEY_COMPRA_PRECIO + " REAL NOT NULL,"
            + KEY_COMPRA_CARTUCHOS_COMPRADOS + " INTEGER NOT NULL,"
            + " FOREIGN KEY (" + KEY_GUIA_ID + ") REFERENCES " + TABLE_GUIAS + "(" + KEY_ID + ")"
            + ")";


    public DataBaseSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Creating required tables
        db.execSQL(CREATE_TABLE_GUIA);
        db.execSQL(CREATE_TABLE_COMPRA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // On upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GUIAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPRAS);

        // Create new tables
        onCreate(db);
    }
}

//      android adb, retrieve database using run-as
//      http://www.hermosaprogramacion.com/2014/10/android-sqlite-bases-de-datos/