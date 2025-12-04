package al.ahgitdevelopment.municion.databases;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;

import al.ahgitdevelopment.municion.datamodel.Compra;
import al.ahgitdevelopment.municion.datamodel.Guia;
import al.ahgitdevelopment.municion.datamodel.Licencia;
import al.ahgitdevelopment.municion.datamodel.Tirada;

/**
 * Created by Alberto on 12/04/2016.
 */
public class DataBaseSQLiteHelper extends SQLiteOpenHelper {

    public FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();

    // Table Names
    public static final String TABLE_GUIAS = "guias";
    public static final String TABLE_COMPRAS = "compras";
    public static final String TABLE_LICENCIAS = "licencias";
    public static final String TABLE_TIRADAS = "tiradas";
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
    public static final String KEY_GUIA_IMAGEN = "imagen_uri";
    public static final String KEY_GUIA_CUPO = "cupo";
    public static final String KEY_GUIA_GASTADO = "gastado";
    // Table COMPRAS  - column names
    public static final String KEY_COMPRA_ID_POS_GUIA = "idPosGuia";
    public static final String KEY_COMPRA_CALIBRE1 = "calibre1";
    public static final String KEY_COMPRA_CALIBRE2 = "calibre2";
    public static final String KEY_COMPRA_UNIDADES = "unidades";
    public static final String KEY_COMPRA_PRECIO = "precio";
    public static final String KEY_COMPRA_FECHA = "fecha";
    public static final String KEY_COMPRA_TIPO = "tipo";
    public static final String KEY_COMPRA_PESO = "peso";
    public static final String KEY_COMPRA_MARCA = "marca";
    public static final String KEY_COMPRA_TIENDA = "tienda";
    public static final String KEY_COMPRA_IMAGEN = "imagen_uri";
    public static final String KEY_COMPRA_VALORACION = "valoracion";
    // Table LICENCIAS  - column names
    public static final String KEY_LICENCIAS_TIPO = "tipo";
    public static final String KEY_LICENCIAS_NOMBRE = "nombre";
    public static final String KEY_LICENCIAS_TIPO_PERMISO_CONDUCCION = "tipo_permiso_conduccion";
    public static final String KEY_LICENCIAS_EDAD = "edad";
    public static final String KEY_LICENCIAS_FECHA_EXPEDICION = "fecha_expedicion";
    public static final String KEY_LICENCIAS_FECHA_CADUCIDAD = "fecha_caducidad";
    public static final String KEY_LICENCIAS_NUM_LICENCIA = "num_licencia";
    public static final String KEY_LICENCIAS_NUM_ABONADO = "num_abonado";
    public static final String KEY_LICENCIAS_NUM_SEGURO = "num_seguro";
    public static final String KEY_LICENCIAS_AUTONOMIA = "autonomia";
    public static final String KEY_LICENCIAS_ESCALA = "escala";
    public static final String KEY_LICENCIAS_CATEGORIA = "categoria";
    // Table TIRADAS  - column names
    public static final String KEY_TIRADAS_DESCRIPCION = "descripcion";
    public static final String KEY_TIRADAS_RANGO = "rango";
    public static final String KEY_TIRADAS_FECHA = "fecha";
    public static final String KEY_TIRADAS_PUNTUACION = "puntuacion";
    // Database Version
    private static final int DATABASE_VERSION = 23;
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
            + KEY_GUIA_NUM_GUIA + " TEXT NOT NULL,"
            + KEY_GUIA_NUM_ARMA + " TEXT NOT NULL,"
            + KEY_GUIA_IMAGEN + " TEXT,"
            + KEY_GUIA_CUPO + " INTEGER NOT NULL,"
            + KEY_GUIA_GASTADO + " INTEGER NOT NULL,"
            + " FOREIGN KEY (" + KEY_GUIA_ID_COMPRA + ") REFERENCES " + TABLE_COMPRAS + "(" + KEY_ID + ")"
            + " FOREIGN KEY (" + KEY_GUIA_ID_LICENCIA + ") REFERENCES " + TABLE_LICENCIAS + "(" + KEY_ID + ")"
            + ")";
    // Compras table create statement
    private static final String CREATE_TABLE_COMPRA = "CREATE TABLE " + TABLE_COMPRAS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
            + KEY_COMPRA_ID_POS_GUIA + " INTEGER NOT NULL, "
            + KEY_COMPRA_CALIBRE1 + " TEXT NOT NULL, "
            + KEY_COMPRA_CALIBRE2 + " TEXT,"
            + KEY_COMPRA_UNIDADES + " INTEGER NOT NULL,"
            + KEY_COMPRA_PRECIO + " REAL NOT NULL,"
            + KEY_COMPRA_FECHA + " TEXT NOT NULL,"
            + KEY_COMPRA_TIPO + " TEXT,"
            + KEY_COMPRA_PESO + " INTEGER,"
            + KEY_COMPRA_MARCA + " TEXT,"
            + KEY_COMPRA_TIENDA + " TEXT,"
            + KEY_COMPRA_IMAGEN + " TEXT,"
            + KEY_COMPRA_VALORACION + " REAL"
            + ")";
    // Licencias table create statement
    private static final String CREATE_TABLE_LICENCIAS = "CREATE TABLE " + TABLE_LICENCIAS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
            + KEY_LICENCIAS_TIPO + " INTEGER NOT NULL,"
            + KEY_LICENCIAS_NOMBRE + " TEXT,"
            + KEY_LICENCIAS_TIPO_PERMISO_CONDUCCION + " INTEGER,"
            + KEY_LICENCIAS_EDAD + " INTEGER,"
            + KEY_LICENCIAS_FECHA_EXPEDICION + " TEXT NOT NULL,"
            + KEY_LICENCIAS_FECHA_CADUCIDAD + " TEXT NOT NULL,"
            + KEY_LICENCIAS_NUM_LICENCIA + " TEXT NOT NULL,"
            + KEY_LICENCIAS_NUM_ABONADO + " INTEGER,"
            + KEY_LICENCIAS_NUM_SEGURO + " TEXT,"
            + KEY_LICENCIAS_AUTONOMIA + " INTEGER,"
            + KEY_LICENCIAS_ESCALA + " INTEGER,"
            + KEY_LICENCIAS_CATEGORIA + " INTEGER"
            + ")";
    // Tiradas table create statement
    private static final String CREATE_TABLE_TIRADAS = "CREATE TABLE " + TABLE_TIRADAS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
            + KEY_TIRADAS_DESCRIPCION + " TEXT,"
            + KEY_TIRADAS_RANGO + " TEXT,"
            + KEY_TIRADAS_FECHA + " TEXT,"
            + KEY_TIRADAS_PUNTUACION + " INTEGER"
            + ")";
    private final String TAG = "DataBaseSQLLiteHelper";
    public Context context;

    public DataBaseSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Creating required tables
        db.execSQL(CREATE_TABLE_GUIA);
        db.execSQL(CREATE_TABLE_COMPRA);
        db.execSQL(CREATE_TABLE_LICENCIAS);
        db.execSQL(CREATE_TABLE_TIRADAS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        ArrayList<Guia> guias = new ArrayList<>();
        ArrayList<Compra> compras = new ArrayList<>();
        ArrayList<Licencia> licencias = new ArrayList<>();
        ArrayList<Tirada> tiradas = new ArrayList<>();

        // Guardado previo de la BBDD
        try {
            guias = getListGuias(db);
        } catch (Exception ex) {
            crashlytics.log("Fallo obteniendo guias. No existe la tabla");
        }
        try {
            // CRITICAL FIX v2.0.4: Special handling for v22→v23 migration (peso TEXT→INTEGER)
            if (oldVersion <= 22) {
                Log.i(TAG, "Migrating from v22: Special handling for peso field (TEXT→INTEGER)");
                compras = getListComprasV22Migration(db);
            } else {
                compras = getListCompras(db);
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error during compras migration", ex);
            crashlytics.log("Fallo obteniendo compras. No existe la tabla");
            crashlytics.recordException(ex);
        }
        try {
            licencias = getListLicencias(db);
        } catch (Exception ex) {
            crashlytics.log("Fallo obteniendo licencias. No existe la tabla");
        }
        try {
            tiradas = getListTiradas(db);
        } catch (Exception ex) {
            crashlytics.log("Fallo obteniendo tiradas. No existe la tabla");
        }

        // On upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GUIAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPRAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LICENCIAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TIRADAS);

        // Create new tables
        onCreate(db);

        // Load older data
        saveListGuias(db, guias);
        saveListCompras(db, compras);
        saveListLicencias(db, licencias);
        saveListTiradas(db, tiradas);

        Log.i(TAG, "BBDD Upgrade Done from v" + oldVersion + " to v" + newVersion);
    }

    /**
     * CRITICAL FIX v2.0.4: Special method for migrating compras from v22
     * In v22, peso was TEXT. This method safely converts TEXT peso to INTEGER.
     */
    private ArrayList<Compra> getListComprasV22Migration(SQLiteDatabase db) {
        ArrayList<Compra> compras = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = getCursorCompras(db);
            if (cursor != null && cursor.getCount() >= 0 && cursor.moveToFirst()) {
                do {
                    Compra compra = new Compra();
                    compra.setId(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_ID)));
                    compra.setIdPosGuia(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_COMPRA_ID_POS_GUIA)));
                    compra.setCalibre1(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_COMPRA_CALIBRE1)));
                    compra.setCalibre2(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_COMPRA_CALIBRE2)));
                    compra.setUnidades(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_COMPRA_UNIDADES)));
                    compra.setPrecio(cursor.getDouble(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_COMPRA_PRECIO)));
                    compra.setFecha(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_COMPRA_FECHA)));
                    compra.setTipo(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_COMPRA_TIPO)));

                    // CRITICAL FIX: Read peso as STRING (it's TEXT in v22), then convert to INTEGER
                    String pesoStr = cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_COMPRA_PESO));
                    int peso = 0;
                    try {
                        if (pesoStr != null && !pesoStr.trim().isEmpty()) {
                            peso = Integer.parseInt(pesoStr.trim());
                        } else {
                            Log.w(TAG, "v22 Migration: Empty peso value, defaulting to 0");
                        }
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "v22 Migration: Invalid peso value '" + pesoStr + "', defaulting to 0", e);
                        crashlytics.log(
                                "v22 Migration: Invalid peso value '" + pesoStr + "' for compra marca=" + compra.getMarca()
                        );
                    }
                    compra.setPeso(peso);

                    compra.setMarca(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_COMPRA_MARCA)));
                    compra.setTienda(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_COMPRA_TIENDA)));
                    compra.setImagePath(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_COMPRA_IMAGEN)));
                    compra.setValoracion(cursor.getFloat(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_COMPRA_VALORACION)));

                    compras.add(compra);
                } while (cursor.moveToNext());
            }

            Log.i(TAG, "v22 Migration: Successfully migrated " + compras.size() + " compras");
        } catch (Exception e) {
            Log.e(TAG, "Error during v22 compras migration", e);
            if (context != null) {
                crashlytics.recordException(e);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return compras;
    }

    /**
     * Metodo para insertar Guias de prueba en la BBDD
     */
    public void addDummyGuias() {
        SQLiteDatabase db = this.getWritableDatabase();
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
                KEY_GUIA_MARCA + ", " +
                KEY_GUIA_MODELO + ", " +
                KEY_GUIA_APODO + ", " +
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
     */
    public void addDummyCompras() {
        SQLiteDatabase db = this.getWritableDatabase();
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
     */
    public void addDummyLicencias() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO " + TABLE_LICENCIAS + " (" +
                KEY_LICENCIAS_TIPO + ", " +
                KEY_LICENCIAS_NUM_LICENCIA + ", " +
                KEY_LICENCIAS_FECHA_EXPEDICION + ", " +
                KEY_LICENCIAS_FECHA_CADUCIDAD + ", " +
                KEY_LICENCIAS_NUM_ABONADO + ", " +
                KEY_LICENCIAS_AUTONOMIA +
                ") VALUES (" +
                "'A - Profesionales, agentes de la autoridad' , " +
                "'192834' , " +
                "'45632' , " +
                "'18/05/2015' , " +
                "'18/05/2020' , " +
                "'1111' , " +
                "'Madrid'" +
                ");");
    }

    private Cursor getCursorGuias(SQLiteDatabase db) {
        if (db == null)
            db = this.getWritableDatabase();
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

    private Cursor getCursorCompras(SQLiteDatabase db) {
        if (db == null)
            db = this.getWritableDatabase();
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

    private Cursor getCursorLicencias(SQLiteDatabase db) {
        if (db == null)
            db = this.getWritableDatabase();
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

    private Cursor getCursorTiradas(SQLiteDatabase db) {
        if (db == null)
            db = this.getWritableDatabase();
        return db.query(
                DataBaseSQLiteHelper.TABLE_TIRADAS,  //Nombre de la tabla
                null,  //Lista de Columnas a consultar
                null,  //Columnas para la clausula WHERE
                null,  //Valores a comparar con las columnas del WHERE
                null,  //Agrupar con GROUP BY
                null,  //Condición HAVING para GROUP BY
                null  //Clausula ORDER BY
        );
    }

    private Cursor getCursorMaxGuiasLicenciaE(SQLiteDatabase db) {
        String selection = KEY_GUIA_ID_LICENCIA + " = ?";
        String[] selectionArgs = {"4"};
        if (db == null)
            db = this.getWritableDatabase();
        return db.query(
                DataBaseSQLiteHelper.TABLE_GUIAS,  //Nombre de la tabla
                null,  //Lista de Columnas a consultar
                selection,  //Columnas para la clausula WHERE
                selectionArgs,  //Valores a comparar con las columnas del WHERE
                null,  //Agrupar con GROUP BY
                null,  //Condición HAVING para GROUP BY
                null  //Clausula ORDER BY
        );
    }

    private Cursor getCursorMaxGuiasEscopeta(SQLiteDatabase db) {
        String selection = KEY_GUIA_ID_LICENCIA + " = ?" + " AND " + KEY_GUIA_TIPO_ARMA + " = ?";
        String[] selectionArgs = {"4", "0"};
        if (db == null)
            db = this.getWritableDatabase();
        return db.query(
                DataBaseSQLiteHelper.TABLE_GUIAS,  //Nombre de la tabla
                null,  //Lista de Columnas a consultar
                selection,  //Columnas para la clausula WHERE
                selectionArgs,  //Valores a comparar con las columnas del WHERE
                null,  //Agrupar con GROUP BY
                null,  //Condición HAVING para GROUP BY
                null  //Clausula ORDER BY
        );
    }

    private Cursor getCursorMaxGuiasRifle(SQLiteDatabase db) {
        String selection = KEY_GUIA_ID_LICENCIA + " = ?" + " AND " + KEY_GUIA_TIPO_ARMA + " = ?";
        String[] selectionArgs = {"4", "1"};
        if (db == null)
            db = this.getWritableDatabase();
        return db.query(
                DataBaseSQLiteHelper.TABLE_GUIAS,  //Nombre de la tabla
                null,  //Lista de Columnas a consultar
                selection,  //Columnas para la clausula WHERE
                selectionArgs,  //Valores a comparar con las columnas del WHERE
                null,  //Agrupar con GROUP BY
                null,  //Condición HAVING para GROUP BY
                null  //Clausula ORDER BY
        );
    }

    public ArrayList<Guia> getListGuias(SQLiteDatabase db) {
        if (db == null)
            db = this.getWritableDatabase();
        ArrayList<Guia> guias = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = getCursorGuias(db);

            // Looping through all rows and adding to list
            if (cursor != null && cursor.getCount() >= 0 && cursor.moveToFirst()) {
                do {
                    Guia guia = new Guia();
                    guia.setId(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_ID)));
                    guia.setIdCompra(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_GUIA_ID_COMPRA)));
                    guia.setTipoLicencia(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_GUIA_ID_LICENCIA)));
                    guia.setApodo(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_GUIA_APODO)));
                    guia.setMarca(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_GUIA_MARCA)));
                    guia.setModelo(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_GUIA_MODELO)));
                    guia.setTipoArma(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_GUIA_TIPO_ARMA)));
                    guia.setCalibre1(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_GUIA_CALIBRE1)));
                    guia.setCalibre2(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_GUIA_CALIBRE2)));
                    guia.setNumGuia(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_GUIA_NUM_GUIA)));
                    guia.setNumArma(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_GUIA_NUM_ARMA)));
                    guia.setImagePath(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_GUIA_IMAGEN)));
                    guia.setCupo(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_GUIA_CUPO)));
                    guia.setGastado(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_GUIA_GASTADO)));

                    // Adding contact to list
                    guias.add(guia);
                } while (cursor.moveToNext());
            }
        } finally {
            // Always close cursor to prevent memory leaks
            if (cursor != null) {
                cursor.close();
            }
        }

        // return contact list
        return guias;
    }

    public ArrayList<Compra> getListCompras(SQLiteDatabase db) {
        if (db == null)
            db = this.getWritableDatabase();
        ArrayList<Compra> compras = new ArrayList<>();
        Cursor cursor = null;

        // Looping through all rows and adding to list
        try {
            cursor = getCursorCompras(db);
            if (cursor != null && cursor.getCount() >= 0 && cursor.moveToFirst()) {
                do {
                    Compra compra = new Compra();
                    compra.setId(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_ID)));
                    compra.setIdPosGuia(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_COMPRA_ID_POS_GUIA)));
                    compra.setCalibre1(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_COMPRA_CALIBRE1)));
                    compra.setCalibre2(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_COMPRA_CALIBRE2)));
                    compra.setUnidades(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_COMPRA_UNIDADES)));
                    compra.setPrecio(cursor.getDouble(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_COMPRA_PRECIO)));
                    compra.setFecha(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_COMPRA_FECHA)));
                    compra.setTipo(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_COMPRA_TIPO)));
                    compra.setPeso(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_COMPRA_PESO)));
                    compra.setMarca(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_COMPRA_MARCA)));
                    compra.setTienda(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_COMPRA_TIENDA)));
                    compra.setImagePath(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_COMPRA_IMAGEN)));
                    compra.setValoracion(cursor.getFloat(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_COMPRA_VALORACION)));

                    // Adding contact to list
                    compras.add(compra);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading compras from database", e);
            if (context != null) {
                crashlytics.recordException(e);
            }
        } finally {
            // Always close cursor to prevent memory leaks
            if (cursor != null) {
                cursor.close();
            }
        }

        // return contact list
        return compras;
    }

    public ArrayList<Licencia> getListLicencias(SQLiteDatabase db) {
        if (db == null)
            db = this.getWritableDatabase();
        ArrayList<Licencia> licencias = new ArrayList<>();
        Cursor cursor = null;

        // Looping through all rows and adding to list
        try {
            cursor = getCursorLicencias(db);
            if (cursor != null && cursor.getCount() >= 0 && cursor.moveToFirst()) {
                do {
                    Licencia licencia = new Licencia();
                    licencia.setId(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_ID)));
                    licencia.setTipo(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_TIPO)));
                    licencia.setNombre(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_NOMBRE)));
                    licencia.setTipoPermisoConduccion(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_TIPO_PERMISO_CONDUCCION)));
                    licencia.setEdad(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_EDAD)));
                    licencia.setFechaExpedicion(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_FECHA_EXPEDICION)));
                    licencia.setFechaCaducidad(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_FECHA_CADUCIDAD)));
                    licencia.setNumLicencia(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_NUM_LICENCIA)));
                    licencia.setNumAbonado(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_NUM_ABONADO)));
                    licencia.setNumSeguro(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_NUM_SEGURO)));
                    licencia.setAutonomia(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_AUTONOMIA)));
                    licencia.setEscala(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_ESCALA)));
                    licencia.setCategoria(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_CATEGORIA)));
                    // Adding contact to list
                    licencias.add(licencia);

                    Log.d(context.getPackageName(), "F. Expedicion: " + licencia.getFechaExpedicion());
                    Log.d(context.getPackageName(), "F. Expedicion: " + licencia.getFechaExpedicion());
                    if (String.valueOf(licencia.getFechaExpedicion()).equals(String.valueOf(licencia.getFechaCaducidad())))
                        Log.wtf(context.getPackageName(), "Error de fechas");


                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading licencias from database", e);
            if (context != null) {
                crashlytics.recordException(e);
            }
        } finally {
            // Always close cursor to prevent memory leaks
            if (cursor != null) {
                cursor.close();
            }
        }

        // return contact list
        return licencias;
    }

    public ArrayList<Tirada> getListTiradas(SQLiteDatabase db) {
        if (db == null)
            db = this.getWritableDatabase();
        ArrayList<Tirada> tiradas = new ArrayList<>();
        Cursor cursor = null;

        // Looping through all rows and adding to list
        try {
            cursor = getCursorTiradas(db);
            if (cursor != null && cursor.getCount() >= 0 && cursor.moveToFirst()) {
                do {
                    Tirada tirada = new Tirada();
                    tirada.setDescripcion(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_TIRADAS_DESCRIPCION)));
                    tirada.setLocalizacion(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_TIRADAS_RANGO)));
                    tirada.setFecha(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_TIRADAS_FECHA)));
                    tirada.setPuntuacion(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_TIRADAS_PUNTUACION)));
                    // Adding to list
                    tiradas.add(tirada);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading tiradas from database", e);
            if (context != null ) {
                crashlytics.recordException(e);
            }
        } finally {
            // Always close cursor to prevent memory leaks
            if (cursor != null) {
                cursor.close();
            }
        }

        return tiradas;
    }

    public void saveListGuias(SQLiteDatabase db, ArrayList<Guia> guias) {
        if (db == null)
            db = this.getWritableDatabase();

        // Use transaction to prevent data loss if insert fails
        db.beginTransaction();
        try {
            db.delete(TABLE_GUIAS, null, null); // No elimina la tabla, solo elimina las filas
            if (guias.size() > 0) {
                for (Guia guia : guias) {
                    db.execSQL("INSERT INTO " + TABLE_GUIAS + " (" +
                            KEY_GUIA_ID_COMPRA + ", " +
                            KEY_GUIA_ID_LICENCIA + ", " +
                            KEY_GUIA_APODO + ", " +
                            KEY_GUIA_MARCA + ", " +
                            KEY_GUIA_MODELO + ", " +
                            KEY_GUIA_TIPO_ARMA + ", " +
                            KEY_GUIA_CALIBRE1 + ", " +
                            KEY_GUIA_CALIBRE2 + ", " +
                            KEY_GUIA_NUM_GUIA + ", " +
                            KEY_GUIA_NUM_ARMA + ", " +
                            KEY_GUIA_IMAGEN + ", " +
                            KEY_GUIA_CUPO + ", " +
                            KEY_GUIA_GASTADO +
                            ") VALUES (" +
                            "'" + guia.getIdCompra() + "' , " +
                            "'" + guia.getTipoLicencia() + "' , " +
                            "'" + guia.getApodo() + "' , " +
                            "'" + guia.getMarca() + "' , " +
                            "'" + guia.getModelo() + "' , " +
                            "'" + guia.getTipoArma() + "' , " +
                            "'" + guia.getCalibre1() + "' , " +
                            "'" + guia.getCalibre2() + "' , " +
                            "'" + guia.getNumGuia() + "' , " +
                            "'" + guia.getNumArma() + "' , " +
                            "'" + guia.getImagePath() + "' , " +
                            "'" + guia.getCupo() + "' , " +
                            "'" + guia.getGastado() + "'" +
                            ");");
                }
            }
            db.setTransactionSuccessful();
            Log.d(context.getPackageName(), "Guia actualizada en BBDD");
        } catch (Exception e) {
            Log.e(TAG, "Error saving guias to database", e);
            if (context != null ) {
                crashlytics.recordException(e);
            }
        } finally {
            db.endTransaction();
        }
    }

    public void saveListCompras(SQLiteDatabase db, ArrayList<Compra> compras) {
        if (db == null)
            db = this.getWritableDatabase();

        // Use transaction to prevent data loss if insert fails
        db.beginTransaction();
        try {
            db.delete(TABLE_COMPRAS, null, null); // No elimina la tabla, solo elimina las filas
            if (compras.size() > 0) {
                for (Compra compra : compras) {
                    db.execSQL("INSERT INTO " + TABLE_COMPRAS + " (" +
                            KEY_COMPRA_ID_POS_GUIA + ", " +
                            KEY_COMPRA_CALIBRE1 + ", " +
                            KEY_COMPRA_CALIBRE2 + ", " +
                            KEY_COMPRA_UNIDADES + ", " +
                            KEY_COMPRA_PRECIO + ", " +
                            KEY_COMPRA_FECHA + ", " +
                            KEY_COMPRA_TIPO + ", " +
                            KEY_COMPRA_PESO + ", " +
                            KEY_COMPRA_MARCA + ", " +
                            KEY_COMPRA_TIENDA + ", " +
                            KEY_COMPRA_IMAGEN + ", " +
                            KEY_COMPRA_VALORACION +
                            ") VALUES (" +
                            "'" + compra.getIdPosGuia() + "' , " +
                            "'" + compra.getCalibre1() + "' , " +
                            "'" + compra.getCalibre2() + "' , " +
                            "'" + compra.getUnidades() + "' , " +
                            "'" + compra.getPrecio() + "' , " +
                            "'" + compra.getFecha() + "' , " +
                            "'" + compra.getTipo() + "' , " +
                            "'" + compra.getPeso() + "' , " +
                            "'" + compra.getMarca() + "' , " +
                            "'" + compra.getTienda() + "' , " +
                            "'" + compra.getImagePath() + "' , " +
                            "'" + compra.getValoracion() + "'" +
                            ");");
                }
            }
            db.setTransactionSuccessful();
            Log.d(context.getPackageName(), "Compra actualizada en BBDD");
        } catch (Exception e) {
            Log.e(TAG, "Error saving compras to database", e);
            if (context != null ) {
                crashlytics.recordException(e);
            }
        } finally {
            db.endTransaction();
        }
    }

    public void saveListLicencias(SQLiteDatabase db, ArrayList<Licencia> licencias) {
        if (db == null)
            db = this.getWritableDatabase();

        // Use transaction to prevent data loss if insert fails
        db.beginTransaction();
        try {
            db.delete(TABLE_LICENCIAS, null, null); // No elimina la tabla, solo elimina las filas
            if (licencias.size() > 0) {
                for (Licencia licencia : licencias) {
                    db.execSQL("INSERT INTO " + TABLE_LICENCIAS + " (" +
                            KEY_LICENCIAS_TIPO + ", " +
                            KEY_LICENCIAS_NOMBRE + ", " +
                            KEY_LICENCIAS_TIPO_PERMISO_CONDUCCION + ", " +
                            KEY_LICENCIAS_EDAD + ", " +
                            KEY_LICENCIAS_NUM_LICENCIA + ", " +
                            KEY_LICENCIAS_FECHA_EXPEDICION + ", " +
                            KEY_LICENCIAS_FECHA_CADUCIDAD + ", " +
                            KEY_LICENCIAS_NUM_ABONADO + ", " +
                            KEY_LICENCIAS_NUM_SEGURO + ", " +
                            KEY_LICENCIAS_AUTONOMIA + ", " +
                            KEY_LICENCIAS_ESCALA + ", " +
                            KEY_LICENCIAS_CATEGORIA +
                            ") VALUES (" +
                            "'" + licencia.getTipo() + "' , " +
                            "'" + licencia.getNombre() + "' , " +
                            "'" + licencia.getTipoPermisoConduccion() + "' , " +
                            "'" + licencia.getEdad() + "' , " +
                            "'" + licencia.getNumLicencia() + "' , " +
                            "'" + licencia.getFechaExpedicion() + "' , " +
                            "'" + licencia.getFechaCaducidad() + "' , " +
                            "'" + licencia.getNumAbonado() + "' , " +
                            "'" + licencia.getNumSeguro() + "' , " +
                            "'" + licencia.getAutonomia() + "' , " +
                            "'" + licencia.getEscala() + "' , " +
                            "'" + licencia.getCategoria() + "'" +
                            ");");
                }
            }
            db.setTransactionSuccessful();
            Log.d(context.getPackageName(), "Licencia actualizada en BBDD");
        } catch (Exception e) {
            Log.e(TAG, "Error saving licencias to database", e);
            if (context != null ) {
                crashlytics.recordException(e);
            }
        } finally {
            db.endTransaction();
        }
    }

    public void saveListTiradas(SQLiteDatabase db, ArrayList<Tirada> tiradas) {
        if (db == null)
            db = this.getWritableDatabase();

        // Use transaction to prevent data loss if insert fails
        db.beginTransaction();
        try {
            db.delete(TABLE_TIRADAS, null, null); // No elimina la tabla, solo elimina las filas
            if (tiradas.size() > 0) {
                for (Tirada tirada : tiradas) {
                    db.execSQL("INSERT INTO " + TABLE_TIRADAS + " (" +
                            KEY_TIRADAS_DESCRIPCION + ", " +
                            KEY_TIRADAS_RANGO + ", " +
                            KEY_TIRADAS_FECHA + ", " +
                            KEY_TIRADAS_PUNTUACION +
                            ") VALUES (" +
                            "'" + tirada.getDescripcion() + "' , " +
                            "'" + tirada.getLocalizacion() + "' , " +
                            "'" + tirada.getFecha() + "' , " +
                            "'" + tirada.getPuntuacion() + "'" +
                            ");");
                }
            }
            db.setTransactionSuccessful();
            Log.d(context.getPackageName(), "Tirada actualizada en BBDD");
        } catch (Exception e) {
            Log.e(TAG, "Error saving tiradas to database", e);
            if (context != null ) {
                crashlytics.recordException(e);
            }
        } finally {
            db.endTransaction();
        }
    }

    public int getNumLicenciasTipoE() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = getCursorMaxGuiasLicenciaE(db);
            return cursor.getCount();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public int getNumGuiasLicenciaTipoEscopeta() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = getCursorMaxGuiasEscopeta(db);
            return cursor.getCount();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public int getNumGuiasLicenciaTipoRifle() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = getCursorMaxGuiasRifle(db);
            return cursor.getCount();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

}

//      android adb, retrieve database using run-as
//      http://www.hermosaprogramacion.com/2014/10/android-sqlite-bases-de-datos/
//      https://www.sqlite.org/datatype3.html
//      http://www.androidhive.info/2013/09/android-sqlite-database-with-multiple-tables/