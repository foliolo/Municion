package al.ahgitdevelopment.municion;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Alberto on 12/04/2016.
 */
public class DataBaseSQLiteHelper extends SQLiteOpenHelper {
    // Table Names
    public static final String TABLE_GUIAS = "guias";
    public static final String TABLE_COMPRAS = "compras";
    public static final String TABLE_LICENCIAS = "licencias";

    // Common column names
    public static final String KEY_ID = "_id";

    // Table GUIAS  - column names
    public static final String KEY_GUIA_ID_COMPRA = "id_compra";
    public static final String KEY_GUIA_ID_LICENCIA = "id_licencia";
    public static final String KEY_GUIA_APODO = "apodo";
    public static final String KEY_GUIA_MARCA = "marca";
    public static final String KEY_GUIA_MODELO = "modelo";
    public static final String KEY_GUIA_TIPO_ARMA = "tipo_arma";
    public static final String KEY_GUIA_CALIBRE1 = "calibre1";
    public static final String KEY_GUIA_CALIBRE2 = "calibre2";
    public static final String KEY_GUIA_NUM_GUIA = "num_guia";
    public static final String KEY_GUIA_NUM_ARMA = "num_arma";
    public static final String KEY_GUIA_IMAGEN = "imagen";
    public static final String KEY_GUIA_CUPO = "cupo";
    public static final String KEY_GUIA_GASTADO = "gastado";

    // Table COMPRAS  - column names
    public static final String KEY_COMPRA_CALIBRE1 = "calibre1";
    public static final String KEY_COMPRA_CALIBRE2 = "calibre2";
    public static final String KEY_COMPRA_MUNICION_PROPIA = "municion_propia";
    public static final String KEY_COMPRA_UNIDADES = "unidades";
    public static final String KEY_COMPRA_PRECIO = "precio";
    public static final String KEY_COMPRA_FECHA = "fecha";
    public static final String KEY_COMPRA_TIPO = "tipo";
    public static final String KEY_COMPRA_PESO = "peso";
    public static final String KEY_COMPRA_MARCA = "marca";
    public static final String KEY_COMPRA_MODELO = "modelo";
    public static final String KEY_COMPRA_TIENDA = "tienda";
    public static final String KEY_COMPRA_VALORACION = "valoracion";

    // Table LICENCIAS  - column names
    public static final String KEY_LICENCIAS_TIPO = "tipo";
    public static final String KEY_LICENCIAS_NUM_LICENCIA = "num_licencia";
    public static final String KEY_LICENCIAS_FECHA_EXPEDICION = "fecha_expedicion";
    public static final String KEY_LICENCIAS_FECHA_CADUCIDAD = "fecha_caducidad";

    // Logcat tag
    private static final String LOG = "DatabaseHelper";
    // Database Version
    private static final int DATABASE_VERSION = 8;
    // Database Name
    private static final String DATABASE_NAME = "DBMunicion.db";
    // Table Create Statements
    // Guias table create statement
    private static final String CREATE_TABLE_GUIA = "CREATE TABLE " + TABLE_GUIAS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
            + KEY_GUIA_ID_COMPRA + " INTEGER,"
            + KEY_GUIA_ID_LICENCIA + " INTEGER NOT NULL,"
            + KEY_GUIA_APODO + " TEXT NOT NULL,"
            + KEY_GUIA_MARCA + " TEXT NOT NULL,"
            + KEY_GUIA_MODELO + " TEXT NOT NULL,"
            + KEY_GUIA_TIPO_ARMA + " INTEGER NOT NULL,"
            + KEY_GUIA_CALIBRE1 + " TEXT NOT NULL,"
            + KEY_GUIA_CALIBRE2 + " TEXT,"
            + KEY_GUIA_NUM_GUIA + " INTEGER NOT NULL,"
            + KEY_GUIA_NUM_ARMA + " INTEGER NOT NULL,"
            + KEY_GUIA_IMAGEN + " BLOB,"
            + KEY_GUIA_CUPO + " INTEGER NOT NULL,"
            + KEY_GUIA_GASTADO + " INTEGER NOT NULL,"
            + " FOREIGN KEY (" + KEY_GUIA_ID_COMPRA + ") REFERENCES " + TABLE_COMPRAS + "(" + KEY_ID + ")"
            + " FOREIGN KEY (" + KEY_GUIA_ID_LICENCIA + ") REFERENCES " + TABLE_LICENCIAS + "(" + KEY_ID + ")"
            + ")";

    // Compras table create statement
    private static final String CREATE_TABLE_COMPRA = "CREATE TABLE " + TABLE_COMPRAS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
            + KEY_COMPRA_CALIBRE1 + " TEXT NOT NULL, "
            + KEY_COMPRA_CALIBRE2 + " TEXT,"
            + KEY_COMPRA_MUNICION_PROPIA + " TEXT,"
            + KEY_COMPRA_UNIDADES + " INTEGER NOT NULL,"
            + KEY_COMPRA_PRECIO + " REAL NOT NULL,"
            + KEY_COMPRA_FECHA + " TEXT NOT NULL,"
            + KEY_COMPRA_TIPO + " TEXT,"
            + KEY_COMPRA_PESO + " TEXT,"
            + KEY_COMPRA_MARCA + " TEXT,"
            + KEY_COMPRA_MODELO + " TEXT,"
            + KEY_COMPRA_TIENDA + " TEXT,"
            + KEY_COMPRA_VALORACION + " INTEGER"
            + ")";

    // Licencias table create statement
    private static final String CREATE_TABLE_LICENCIAS = "CREATE TABLE " + TABLE_LICENCIAS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
            + KEY_LICENCIAS_TIPO + " TEXT NOT NULL,"
            + KEY_LICENCIAS_NUM_LICENCIA + " INTEGER NOT NULL,"
            + KEY_LICENCIAS_FECHA_EXPEDICION + " TEXT NOT NULL,"
            + KEY_LICENCIAS_FECHA_CADUCIDAD + " TEXT NOT NULL"
            + ")";

    public DataBaseSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Metodo para insertar Guias de prueba en la BBDD
     *
     * @param db
     */
    public static void addGuias(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + TABLE_GUIAS + " (" +
                KEY_GUIA_ID_COMPRA + ", " +
                KEY_GUIA_ID_LICENCIA + ", " +
                KEY_GUIA_APODO + ", " +
                KEY_GUIA_MARCA + ", " +
                KEY_GUIA_MODELO + ", " +
                KEY_GUIA_TIPO_ARMA + ", " +
                KEY_GUIA_CALIBRE1 + ", " +
                KEY_GUIA_NUM_GUIA + ", " +
                KEY_GUIA_NUM_ARMA + ", " +
                KEY_GUIA_CUPO + ", " +
                KEY_GUIA_GASTADO +
                ") VALUES (" +
                "'1' , " +
                "'1' , " +
                "'Mi Pipa' , " +
                "'Norinco' , " +
                "'R2D2' , " +
                "'1' , " +
                "'Calibre 45' , " +
                "'12345' , " +
                "'98765' , " +
                "'1000' , " +
                "'500'" +
                ");");
        db.execSQL("INSERT INTO " + TABLE_GUIAS + " (" +
                KEY_GUIA_ID_COMPRA + ", " +
                KEY_GUIA_ID_LICENCIA + ", " +
                KEY_GUIA_APODO + ", " +
                KEY_GUIA_MARCA + ", " +
                KEY_GUIA_MODELO + ", " +
                KEY_GUIA_TIPO_ARMA + ", " +
                KEY_GUIA_CALIBRE1 + ", " +
                KEY_GUIA_NUM_GUIA + ", " +
                KEY_GUIA_NUM_ARMA + ", " +
                KEY_GUIA_CUPO + ", " +
                KEY_GUIA_GASTADO +
                ") VALUES (" +
                "'2' , " +
                "'2' , " +
                "'Rifle' , " +
                "'Norinco' , " +
                "'C3PO' , " +
                "'2' , " +
                "'Calibre 50' , " +
                "'987654' , " +
                "'98765' , " +
                "'100' , " +
                "'20'" +
                ");");
    }

    /**
     * Metodo para insertar Conmpras de prueba en la BBDD
     *
     * @param db
     */
    public static void addCompras(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + TABLE_COMPRAS + " (" +
                KEY_COMPRA_CALIBRE1 + ", " +
                KEY_COMPRA_UNIDADES + ", " +
                KEY_COMPRA_PRECIO + ", " +
                KEY_COMPRA_FECHA +
                ") VALUES (" +
                "'Calibre 45' , " +
                "'500' , " +
                "'25.50' , " +
                "'18/05/2016'" +
                ");");
        db.execSQL("INSERT INTO " + TABLE_COMPRAS + " (" +
                KEY_COMPRA_CALIBRE1 + ", " +
                KEY_COMPRA_UNIDADES + ", " +
                KEY_COMPRA_PRECIO + ", " +
                KEY_COMPRA_FECHA +
                ") VALUES (" +
                "'Calibre 50' , " +
                "'50' , " +
                "'40' , " +
                "'18/05/2016'" +
                ");");
    }

    /**
     * Metodo para insertar Licencias de prueba en la BBDD
     *
     * @param db
     */
    public static void addLicencias(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + TABLE_LICENCIAS + " (" +
                KEY_LICENCIAS_TIPO + ", " +
                KEY_LICENCIAS_NUM_LICENCIA + ", " +
                KEY_LICENCIAS_FECHA_EXPEDICION + ", " +
                KEY_LICENCIAS_FECHA_CADUCIDAD +
                ") VALUES (" +
                "'A' , " +
                "'192834' , " +
                "'18/05/2015' , " +
                "'18/05/2017'" +
                ");");
    }

    public static Cursor getGuias(SQLiteDatabase db) {
        return db.query(
                DataBaseSQLiteHelper.TABLE_GUIAS,  //Nombre de la tabla
                null,  //Lista de Columnas a consultar
                null,  //Columnas para la clausula WHERE
                null,  //Valores a comparar con las columnas del WHERE
                null,  //Agrupar con GROUP BY
                null,  //Condición HAVING para GROUP BY
                null  //Clausula ORDER BY
        );
    }

    public static Cursor getCompras(SQLiteDatabase db) {
        return db.query(
                DataBaseSQLiteHelper.TABLE_COMPRAS,  //Nombre de la tabla
                null,  //Lista de Columnas a consultar
                null,  //Columnas para la clausula WHERE
                null,  //Valores a comparar con las columnas del WHERE
                null,  //Agrupar con GROUP BY
                null,  //Condición HAVING para GROUP BY
                null  //Clausula ORDER BY
        );
    }

    public static Cursor getLicencias(SQLiteDatabase db) {
        return db.query(
                DataBaseSQLiteHelper.TABLE_LICENCIAS,  //Nombre de la tabla
                null,  //Lista de Columnas a consultar
                null,  //Columnas para la clausula WHERE
                null,  //Valores a comparar con las columnas del WHERE
                null,  //Agrupar con GROUP BY
                null,  //Condición HAVING para GROUP BY
                null  //Clausula ORDER BY
        );
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Creating required tables
        db.execSQL(CREATE_TABLE_GUIA);
        db.execSQL(CREATE_TABLE_COMPRA);
        db.execSQL(CREATE_TABLE_LICENCIAS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // On upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GUIAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPRAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LICENCIAS);

        // Create new tables
        onCreate(db);
    }
}

//      android adb, retrieve database using run-as
//      http://www.hermosaprogramacion.com/2014/10/android-sqlite-bases-de-datos/
//      https://www.sqlite.org/datatype3.html
//      http://www.androidhive.info/2013/09/android-sqlite-database-with-multiple-tables/