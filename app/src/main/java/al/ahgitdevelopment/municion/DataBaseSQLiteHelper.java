package al.ahgitdevelopment.municion;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import al.ahgitdevelopment.municion.DataModel.Compra;
import al.ahgitdevelopment.municion.DataModel.Guia;
import al.ahgitdevelopment.municion.DataModel.Licencia;

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
    public static final String KEY_GUIA_IMAGEN = "imagen_uri";
    public static final String KEY_GUIA_CUPO = "cupo";
    public static final String KEY_GUIA_GASTADO = "gastado";
    // Table COMPRAS  - column names
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

    // Logcat tag
    private static final String LOG = "DatabaseHelper";
    // Database Version
    private static final int DATABASE_VERSION = 18;
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
            + KEY_GUIA_IMAGEN + " TEXT,"
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
            + KEY_COMPRA_UNIDADES + " INTEGER NOT NULL,"
            + KEY_COMPRA_PRECIO + " REAL NOT NULL,"
            + KEY_COMPRA_FECHA + " TEXT NOT NULL,"
            + KEY_COMPRA_TIPO + " TEXT,"
            + KEY_COMPRA_PESO + " TEXT,"
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
            + KEY_LICENCIAS_NUM_LICENCIA + " INTEGER NOT NULL,"
            + KEY_LICENCIAS_NUM_ABONADO + " INTEGER,"
            + KEY_LICENCIAS_NUM_SEGURO + " TEXT,"
            + KEY_LICENCIAS_AUTONOMIA + " INTEGER,"
            + KEY_LICENCIAS_ESCALA + " INTEGER"
            + ")";

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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Guardado previo de la BBDD
        ArrayList<Guia> guias = getListGuias(db);
        ArrayList<Compra> compras = getListCompras(db);
        ArrayList<Licencia> licencias = getListLicencias(db);

        // On upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GUIAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPRAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LICENCIAS);

        // Create new tables
        onCreate(db);

        // Load older data
        saveListGuias(db, guias);
        saveListCompras(db, compras);
        saveListLicencias(db, licencias);
        Log.i(context.getPackageName(), "Upgrade Done");
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
                KEY_LICENCIAS_FECHA_EXPEDICION + ", "  +
                KEY_LICENCIAS_FECHA_CADUCIDAD + ", "  +
                KEY_LICENCIAS_NUM_ABONADO + ", "  +
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

    public Cursor getCursorGuias(SQLiteDatabase db) {
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

    public Cursor getCursorCompras(SQLiteDatabase db) {
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

    public Cursor getCursorLicencias(SQLiteDatabase db) {
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

    public Cursor getCursorGuiasQueries(SQLiteDatabase db) {
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

    public ArrayList<Guia> getListGuias(SQLiteDatabase db) {
        if (db == null)
            db = this.getWritableDatabase();
        ArrayList<Guia> guias = new ArrayList<Guia>();
        Cursor cursor = getCursorGuias(db);

        // Looping through all rows and adding to list
        if (cursor.moveToFirst()) {
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
                guia.setNumGuia(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_GUIA_NUM_GUIA)));
                guia.setNumArma(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_GUIA_NUM_ARMA)));
                guia.setImagePath(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_GUIA_IMAGEN)));
                guia.setCupo(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_GUIA_CUPO)));
                guia.setGastado(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_GUIA_GASTADO)));

                // Adding contact to list
                guias.add(guia);
            } while (cursor.moveToNext());
        }

        // return contact list
        return guias;
    }

    public ArrayList<Compra> getListCompras(SQLiteDatabase db) {
        if (db == null)
            db = this.getWritableDatabase();
        ArrayList<Compra> compras = new ArrayList<Compra>();
        Cursor cursor = getCursorCompras(db);

        // Looping through all rows and adding to list
        try {
            if (cursor.moveToFirst()) {
                do {
                    Compra compra = new Compra();
                    compra.setId(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_ID)));
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
                    compra.setValoracion(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_COMPRA_VALORACION)));

                    // Adding contact to list
                    compras.add(compra);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // return contact list
        return compras;
    }

    public ArrayList<Licencia> getListLicencias(SQLiteDatabase db) {
        if (db == null)
            db = this.getWritableDatabase();
        ArrayList<Licencia> licencias = new ArrayList<Licencia>();
        Cursor cursor = getCursorLicencias(db);

        // Looping through all rows and adding to list
        try {
            if (cursor.moveToFirst()) {
                do {
                    Licencia licencia = new Licencia();
                    licencia.setId(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_ID)));
                    licencia.setTipo(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_TIPO)));
                    licencia.setNombre(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_NOMBRE)));
                    licencia.setTipoPermisoConduccion(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_TIPO_PERMISO_CONDUCCION)));
                    licencia.setEdad(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_EDAD)));
                    licencia.setFechaExpedicion(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_FECHA_EXPEDICION)));
                    licencia.setFechaCaducidad(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_FECHA_CADUCIDAD)));
                    licencia.setNumLicencia(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_NUM_LICENCIA)));
                    licencia.setNumAbonado(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_NUM_ABONADO)));
                    licencia.setNumSeguro(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_NUM_SEGURO)));
                    licencia.setAutonomia(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_AUTONOMIA)));
                    licencia.setEscala(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_ESCALA)));

                    // Adding contact to list
                    licencias.add(licencia);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // return contact list
        return licencias;
    }

    public void saveListGuias(SQLiteDatabase db, ArrayList<Guia> guias) {
        if (db == null)
            db = this.getWritableDatabase();
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
        Log.d(context.getPackageName(), "Guia actualizada en BBDD");
    }

    public void saveListCompras(SQLiteDatabase db, ArrayList<Compra> compras) {
        if (db == null)
            db = this.getWritableDatabase();
        db.delete(TABLE_COMPRAS, null, null); // No elimina la tabla, solo elimina las filas
        if (compras.size() > 0) {
            for (Compra compra : compras) {
                db.execSQL("INSERT INTO " + TABLE_COMPRAS + " (" +
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
        Log.d(context.getPackageName(), "Compra actualizada en BBDD");
    }

    public void saveListLicencias(SQLiteDatabase db, ArrayList<Licencia> licencias) {
        if (db == null)
            db = this.getWritableDatabase();
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
                        KEY_LICENCIAS_ESCALA +
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
                        "'" + licencia.getEscala() + "'" +
                        ");");
            }
        }
        Log.d(context.getPackageName(), "Licencia actualizada en BBDD");
    }

    public int getNumLicenciasTipoE() {
        SQLiteDatabase db = this.getReadableDatabase();
        int result = getCursorGuiasQueries(db).getCount();
        return result;
    }
}

//      android adb, retrieve database using run-as
//      http://www.hermosaprogramacion.com/2014/10/android-sqlite-bases-de-datos/
//      https://www.sqlite.org/datatype3.html
//      http://www.androidhive.info/2013/09/android-sqlite-database-with-multiple-tables/