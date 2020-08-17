package al.ahgitdevelopment.municion.sandbox;

/**
 * Created by Alberto on 12/04/2016.
 */
public class DataBaseSQLiteHelper { //extends SQLiteOpenHelper {
//
//    // Table Create Statements
//    // Guias table create statement
//    private static final String CREATE_TABLE_GUIA = "CREATE TABLE " + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.TABLE_GUIAS + "("
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_ID_COMPRA + " INTEGER,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_ID_LICENCIA + " INTEGER NOT NULL,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_APODO + " TEXT NOT NULL,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_MARCA + " TEXT NOT NULL,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_MODELO + " TEXT NOT NULL,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_TIPO_ARMA + " INTEGER NOT NULL,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_CALIBRE1 + " TEXT NOT NULL,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_CALIBRE2 + " TEXT,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_NUM_GUIA + " TEXT NOT NULL,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_NUM_ARMA + " TEXT NOT NULL,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_IMAGEN + " TEXT,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_CUPO + " INTEGER NOT NULL,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_GASTADO + " INTEGER NOT NULL,"
//            + " FOREIGN KEY (" + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_ID_COMPRA + ") REFERENCES " + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.TABLE_COMPRAS + "(" + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_ID + "),"
//            + " FOREIGN KEY (" + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_ID_LICENCIA + ") REFERENCES " + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.TABLE_LICENCIAS + "(" + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_ID + ")"
//            + ")";
//    // Compras table create statement
//    private static final String CREATE_TABLE_COMPRA = "CREATE TABLE " + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.TABLE_COMPRAS + "("
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_ID_POS_GUIA + " INTEGER NOT NULL, "
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_CALIBRE1 + " TEXT NOT NULL, "
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_CALIBRE2 + " TEXT,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_UNIDADES + " INTEGER NOT NULL,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_PRECIO + " REAL NOT NULL,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_FECHA + " TEXT NOT NULL,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_TIPO + " TEXT,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_PESO + " TEXT,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_MARCA + " TEXT,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_TIENDA + " TEXT,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_IMAGEN + " TEXT,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_VALORACION + " REAL"
//            + ")";
//    // Licencias table create statement
//    private static final String CREATE_TABLE_LICENCIAS = "CREATE TABLE " + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.TABLE_LICENCIAS + "("
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_TIPO + " INTEGER NOT NULL,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_NOMBRE + " TEXT,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_TIPO_PERMISO_CONDUCCION + " INTEGER,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_EDAD + " INTEGER,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_FECHA_EXPEDICION + " TEXT NOT NULL,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_FECHA_CADUCIDAD + " TEXT NOT NULL,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_NUM_LICENCIA + " TEXT NOT NULL,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_NUM_ABONADO + " INTEGER,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_NUM_SEGURO + " TEXT,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_AUTONOMIA + " INTEGER,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_ESCALA + " INTEGER,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_CATEGORIA + " INTEGER"
//            + ")";
//    // Tiradas table create statement
//    private static final String CREATE_TABLE_TIRADAS = "CREATE TABLE " + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.TABLE_TIRADAS + "("
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_TIRADAS_DESCRIPCION + " TEXT,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_TIRADAS_RANGO + " TEXT,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_TIRADAS_FECHA + " TEXT,"
//            + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_TIRADAS_PUNTUACION + " INTEGER"
//            + ")";
//    private final String TAG = "DataBaseSQLLiteHelper";
//    public Context context;
//
//    public DataBaseSQLiteHelper(Context context) {
//        super(context, al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.DATABASE_NAME, null, al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.DATABASE_VERSION);
//        this.context = context;
//    }
//
//    @Override
//    public void onCreate(SQLiteDatabase db) {
//        // Creating required tables
//        db.execSQL(CREATE_TABLE_GUIA);
//        db.execSQL(CREATE_TABLE_COMPRA);
//        db.execSQL(CREATE_TABLE_LICENCIAS);
//        db.execSQL(CREATE_TABLE_TIRADAS);
//    }
//
//    @Override
//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//
//        ArrayList<Guia> guias = new ArrayList<>();
//        ArrayList<Compra> compras = new ArrayList<>();
//        ArrayList<Licencia> licencias = new ArrayList<>();
//        ArrayList<Tirada> tiradas = new ArrayList<>();
//
//        // Guardado previo de la BBDD
//        try {
//            guias = getListGuias(db);
//        } catch (Exception ex) {
//            Log.w( TAG, "Fallo obteniendo guias. No existe la tabla");
//        }
//        try {
//            compras = getListCompras(db);
//        } catch (Exception ex) {
//            Log.w( TAG, "Fallo obteniendo compras. No existe la tabla");
//        }
//        try {
//            licencias = getListLicencias(db);
//        } catch (Exception ex) {
//            Log.w( TAG, "Fallo obteniendo licencias. No existe la tabla");
//        }
//        try {
//            tiradas = getListTiradas(db);
//        } catch (Exception ex) {
//            Log.w( TAG, "Fallo obteniendo tiradas. No existe la tabla");
//        }
//
//        // On upgrade drop older tables
//        db.execSQL("DROP TABLE IF EXISTS " + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.TABLE_GUIAS);
//        db.execSQL("DROP TABLE IF EXISTS " + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.TABLE_COMPRAS);
//        db.execSQL("DROP TABLE IF EXISTS " + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.TABLE_LICENCIAS);
//        db.execSQL("DROP TABLE IF EXISTS " + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.TABLE_TIRADAS);
//
//        // Create new tables
//        onCreate(db);
//
//        // Load older data
//        saveListGuias(db, guias);
//        saveListCompras(db, compras);
//        saveListLicencias(db, licencias);
//        saveListTiradas(db, tiradas);
//
//        Log.i(TAG, "BBDD Upgrade Done");
//    }
//
//    /**
//     * Metodo para insertar Guias de prueba en la BBDD
//     */
//    public void addDummyGuias() {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.execSQL("INSERT INTO " + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.TABLE_GUIAS + " (" +
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_ID_COMPRA + ", " +
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_ID_LICENCIA + ", " +
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_APODO + ", " +
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_MARCA + ", " +
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_MODELO + ", " +
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_TIPO_ARMA + ", " +
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_CALIBRE1 + ", " +
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_NUM_GUIA + ", " +
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_NUM_ARMA + ", " +
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_CUPO + ", " +
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_GASTADO +
//                ") VALUES (" +
//                "'1' , " +
//                "'1' , " +
//                "'Mi Pipa' , " +
//                "'Norinco' , " +
//                "'R2D2' , " +
//                "'1' , " +
//                "'Calibre 45' , " +
//                "'12345' , " +
//                "'98765' , " +
//                "'1000' , " +
//                "'500'" +
//                ");");
//        db.execSQL("INSERT INTO " + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.TABLE_GUIAS + " (" +
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_ID_COMPRA + ", " +
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_ID_LICENCIA + ", " +
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_MARCA + ", " +
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_MODELO + ", " +
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_APODO + ", " +
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_TIPO_ARMA + ", " +
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_CALIBRE1 + ", " +
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_NUM_GUIA + ", " +
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_NUM_ARMA + ", " +
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_CUPO + ", " +
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_GASTADO +
//                ") VALUES (" +
//                "'2' , " +
//                "'2' , " +
//                "'Rifle' , " +
//                "'Norinco' , " +
//                "'C3PO' , " +
//                "'2' , " +
//                "'Calibre 50' , " +
//                "'987654' , " +
//                "'98765' , " +
//                "'100' , " +
//                "'20'" +
//                ");");
//    }
//
//    /**
//     * Metodo para insertar Conmpras de prueba en la BBDD
//     */
//    public void addDummyCompras() {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.execSQL("INSERT INTO " + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.TABLE_COMPRAS + " (" +
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_CALIBRE1 + ", " +
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_UNIDADES + ", " +
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_PRECIO + ", " +
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_FECHA +
//                ") VALUES (" +
//                "'Calibre 45' , " +
//                "'500' , " +
//                "'25.50' , " +
//                "'18/05/2016'" +
//                ");");
//        db.execSQL("INSERT INTO " + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.TABLE_COMPRAS + " (" +
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_CALIBRE1 + ", " +
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_UNIDADES + ", " +
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_PRECIO + ", " +
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_FECHA +
//                ") VALUES (" +
//                "'Calibre 50' , " +
//                "'50' , " +
//                "'40' , " +
//                "'18/05/2016'" +
//                ");");
//    }
//
//    private Cursor getCursorGuias(SQLiteDatabase db) {
//        if (db == null)
//            db = this.getWritableDatabase();
//        return db.query(
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.TABLE_GUIAS,  //Nombre de la tabla
//                null,  //Lista de Columnas a consultar
//                null,  //Columnas para la clausula WHERE
//                null,  //Valores a comparar con las columnas del WHERE
//                null,  //Agrupar con GROUP BY
//                null,  //Condición HAVING para GROUP BY
//                null  //Clausula ORDER BY
//        );
//    }
//
//    private Cursor getCursorCompras(SQLiteDatabase db) {
//        if (db == null)
//            db = this.getWritableDatabase();
//        return db.query(
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.TABLE_COMPRAS,  //Nombre de la tabla
//                null,  //Lista de Columnas a consultar
//                null,  //Columnas para la clausula WHERE
//                null,  //Valores a comparar con las columnas del WHERE
//                null,  //Agrupar con GROUP BY
//                null,  //Condición HAVING para GROUP BY
//                null  //Clausula ORDER BY
//        );
//    }
//
//    private Cursor getCursorLicencias(SQLiteDatabase db) {
//        if (db == null)
//            db = this.getWritableDatabase();
//        return db.query(
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.TABLE_LICENCIAS,  //Nombre de la tabla
//                null,  //Lista de Columnas a consultar
//                null,  //Columnas para la clausula WHERE
//                null,  //Valores a comparar con las columnas del WHERE
//                null,  //Agrupar con GROUP BY
//                null,  //Condición HAVING para GROUP BY
//                null  //Clausula ORDER BY
//        );
//    }
//
//    private Cursor getCursorTiradas(SQLiteDatabase db) {
//        if (db == null)
//            db = this.getWritableDatabase();
//        return db.query(
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.TABLE_TIRADAS,  //Nombre de la tabla
//                null,  //Lista de Columnas a consultar
//                null,  //Columnas para la clausula WHERE
//                null,  //Valores a comparar con las columnas del WHERE
//                null,  //Agrupar con GROUP BY
//                null,  //Condición HAVING para GROUP BY
//                null  //Clausula ORDER BY
//        );
//    }
//
//    private Cursor getCursorMaxGuiasLicenciaE(SQLiteDatabase db) {
//        String selection = al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_ID_LICENCIA + " = ?";
//        String[] selectionArgs = {"4"};
//        if (db == null)
//            db = this.getWritableDatabase();
//        return db.query(
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.TABLE_GUIAS,  //Nombre de la tabla
//                null,  //Lista de Columnas a consultar
//                selection,  //Columnas para la clausula WHERE
//                selectionArgs,  //Valores a comparar con las columnas del WHERE
//                null,  //Agrupar con GROUP BY
//                null,  //Condición HAVING para GROUP BY
//                null  //Clausula ORDER BY
//        );
//    }
//
//    private Cursor getCursorMaxGuiasEscopeta(SQLiteDatabase db) {
//        String selection = al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_ID_LICENCIA + " = ?" + " AND " + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_TIPO_ARMA + " = ?";
//        String[] selectionArgs = {"4", "0"};
//        if (db == null)
//            db = this.getWritableDatabase();
//        return db.query(
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.TABLE_GUIAS,  //Nombre de la tabla
//                null,  //Lista de Columnas a consultar
//                selection,  //Columnas para la clausula WHERE
//                selectionArgs,  //Valores a comparar con las columnas del WHERE
//                null,  //Agrupar con GROUP BY
//                null,  //Condición HAVING para GROUP BY
//                null  //Clausula ORDER BY
//        );
//    }
//
//    private Cursor getCursorMaxGuiasRifle(SQLiteDatabase db) {
//        String selection = al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_ID_LICENCIA + " = ?" + " AND " + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_TIPO_ARMA + " = ?";
//        String[] selectionArgs = {"4", "1"};
//        if (db == null)
//            db = this.getWritableDatabase();
//        return db.query(
//                al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.TABLE_GUIAS,  //Nombre de la tabla
//                null,  //Lista de Columnas a consultar
//                selection,  //Columnas para la clausula WHERE
//                selectionArgs,  //Valores a comparar con las columnas del WHERE
//                null,  //Agrupar con GROUP BY
//                null,  //Condición HAVING para GROUP BY
//                null  //Clausula ORDER BY
//        );
//    }
//
//    public ArrayList<Guia> getListGuias(SQLiteDatabase db) {
//        if (db == null)
//            db = this.getWritableDatabase();
//        ArrayList<Guia> guias = new ArrayList<>();
//        Cursor cursor = getCursorGuias(db);
//
//        // Looping through all rows and adding to list
//        if (cursor != null && cursor.getCount() >= 0 && cursor.moveToFirst()) {
//            do {
//                Guia guia = new Guia(
//                        cursor.getInt(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_ID)),
//                        cursor.getInt(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_ID_COMPRA)),
//                        cursor.getInt(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_ID_LICENCIA)),
//                        cursor.getString(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_APODO)),
//                        cursor.getString(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_MARCA)),
//                        cursor.getString(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_MODELO)),
//                        cursor.getInt(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_TIPO_ARMA)),
//                        cursor.getString(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_CALIBRE1)),
//                        cursor.getString(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_CALIBRE2)),
//                        cursor.getString(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_NUM_GUIA)),
//                        cursor.getString(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_NUM_ARMA)),
//                        cursor.getString(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_IMAGEN)),
//                        cursor.getInt(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_CUPO)),
//                        cursor.getInt(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_GASTADO))
//                );
//
//                // Adding contact to list
//                guias.add(guia);
//            } while (cursor.moveToNext());
//        }
//
//        // return contact list
//        return guias;
//    }
//
//    private ArrayList<Compra> getListCompras(SQLiteDatabase db) {
//        if (db == null)
//            db = this.getWritableDatabase();
//        ArrayList<Compra> compras = new ArrayList<>();
//        Cursor cursor = getCursorCompras(db);
//
//        // Looping through all rows and adding to list
//        try {
//            if (cursor != null && cursor.getCount() >= 0 && cursor.moveToFirst()) {
//                do {
//                    Compra compra = new Compra(
//                            cursor.getInt(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_ID)),
//                            cursor.getInt(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_ID_POS_GUIA)),
//                            cursor.getString(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_CALIBRE1)),
//                            cursor.getString(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_CALIBRE2)),
//                            cursor.getInt(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_UNIDADES)),
//                            cursor.getDouble(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_PRECIO)),
//                            cursor.getString(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_FECHA)),
//                            cursor.getString(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_TIPO)),
//                            cursor.getInt(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_PESO)),
//                            cursor.getString(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_MARCA)),
//                            cursor.getString(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_TIENDA)),
//                            cursor.getInt(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_VALORACION)),
//                            cursor.getString(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_IMAGEN))
//                    );
//                    // Adding contact to list
//                    compras.add(compra);
//                } while (cursor.moveToNext());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        // return contact list
//        return compras;
//    }
//
//    public ArrayList<Licencia> getListLicencias(SQLiteDatabase db) {
//        if (db == null)
//            db = this.getWritableDatabase();
//        ArrayList<Licencia> licencias = new ArrayList<>();
//        Cursor cursor = getCursorLicencias(db);
//
//        // Looping through all rows and adding to list
//        try {
//            if (cursor != null && cursor.getCount() >= 0 && cursor.moveToFirst()) {
//                do {
//                    Licencia licencia = new Licencia(
//                            cursor.getInt(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_ID)),
//                            cursor.getInt(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_TIPO)),
//                            cursor.getString(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_NOMBRE)),
//                            cursor.getInt(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_TIPO_PERMISO_CONDUCCION)),
//                            cursor.getInt(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_EDAD)),
//                            cursor.getString(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_FECHA_EXPEDICION)),
//                            cursor.getString(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_FECHA_CADUCIDAD)),
//                            cursor.getString(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_NUM_LICENCIA)),
//                            cursor.getInt(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_NUM_ABONADO)),
//                            cursor.getString(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_NUM_SEGURO)),
//                            cursor.getInt(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_AUTONOMIA)),
//                            cursor.getInt(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_ESCALA)),
//                            cursor.getInt(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_CATEGORIA))
//                    );
//                    // Adding contact to list
//                    licencias.add(licencia);
//
//                    if (licencia.getFechaExpedicion().equals(licencia.getFechaCaducidad()))
//                        Log.wtf(context.getPackageName(), "Error de fechas");
//
//
//                } while (cursor.moveToNext());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        // return contact list
//        return licencias;
//    }
//
//    public ArrayList<Tirada> getListTiradas(SQLiteDatabase db) {
//        if (db == null)
//            db = this.getWritableDatabase();
//        ArrayList<Tirada> tiradas = new ArrayList<>();
//        Cursor cursor = getCursorTiradas(db);
//
//        // Looping through all rows and adding to list
//        try {
//            if (cursor != null && cursor.getCount() >= 0 && cursor.moveToFirst()) {
//                do {
////                    Tirada tirada = new Tirada(
////                            cursor.getString(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_TIRADAS_DESCRIPCION)),
////                            cursor.getString(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_TIRADAS_RANGO)),
////                            cursor.getString(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_TIRADAS_FECHA)),
////                            cursor.getInt(cursor.getColumnIndex(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_TIRADAS_PUNTUACION))
////                    );
////                    // Adding to list
////                    tiradas.add(tirada);
//                } while (cursor.moveToNext());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return tiradas;
//    }
//
//    public void saveListGuias(SQLiteDatabase db, ArrayList<Guia> guias) {
//        if (db == null)
//            db = this.getWritableDatabase();
//        db.delete(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.TABLE_GUIAS, null, null); // No elimina la tabla, solo elimina las filas
//        if (guias.size() > 0) {
//            for (Guia guia : guias) {
//                db.execSQL("INSERT INTO " + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.TABLE_GUIAS + " (" +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_ID_COMPRA + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_ID_LICENCIA + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_APODO + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_MARCA + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_MODELO + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_TIPO_ARMA + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_CALIBRE1 + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_CALIBRE2 + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_NUM_GUIA + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_NUM_ARMA + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_IMAGEN + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_CUPO + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_GUIA_GASTADO +
//                        ") VALUES (" +
//                        "'" + guia.getId() + "' , " +
//                        "'" + guia.getTipoLicencia() + "' , " +
//                        "'" + guia.getApodo() + "' , " +
//                        "'" + guia.getMarca() + "' , " +
//                        "'" + guia.getModelo() + "' , " +
//                        "'" + guia.getTipoArma() + "' , " +
//                        "'" + guia.getCalibre1() + "' , " +
//                        "'" + guia.getCalibre2() + "' , " +
//                        "'" + guia.getNumGuia() + "' , " +
//                        "'" + guia.getNumArma() + "' , " +
//                        "'" + guia.getImagePath() + "' , " +
//                        "'" + guia.getCupo() + "' , " +
//                        "'" + guia.getGastado() + "'" +
//                        ");");
//            }
//        }
//        Log.d(context.getPackageName(), "Guia actualizada en BBDD");
//    }
//
//    public void saveListCompras(SQLiteDatabase db, ArrayList<Compra> compras) {
//        if (db == null)
//            db = this.getWritableDatabase();
//        db.delete(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.TABLE_COMPRAS, null, null); // No elimina la tabla, solo elimina las filas
//        if (compras.size() > 0) {
//            for (Compra compra : compras) {
//                db.execSQL("INSERT INTO " + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.TABLE_COMPRAS + " (" +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_ID_POS_GUIA + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_CALIBRE1 + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_CALIBRE2 + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_UNIDADES + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_PRECIO + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_FECHA + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_TIPO + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_PESO + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_MARCA + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_TIENDA + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_IMAGEN + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_COMPRA_VALORACION +
//                        ") VALUES (" +
//                        "'" + compra.getIdPosGuia() + "' , " +
//                        "'" + compra.getCalibre1() + "' , " +
//                        "'" + compra.getCalibre2() + "' , " +
//                        "'" + compra.getUnidades() + "' , " +
//                        "'" + compra.getPrecio() + "' , " +
//                        "'" + compra.getFecha() + "' , " +
//                        "'" + compra.getTipo() + "' , " +
//                        "'" + compra.getPeso() + "' , " +
//                        "'" + compra.getMarca() + "' , " +
//                        "'" + compra.getTienda() + "' , " +
//                        "'" + compra.getImagePath() + "' , " +
//                        "'" + compra.getValoracion() + "'" +
//                        ");");
//            }
//        }
//        Log.d(context.getPackageName(), "Compra actualizada en BBDD");
//    }
//
//    public void saveListLicencias(SQLiteDatabase db, ArrayList<Licencia> licencias) {
//        if (db == null)
//            db = this.getWritableDatabase();
//        db.delete(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.TABLE_LICENCIAS, null, null); // No elimina la tabla, solo elimina las filas
//        if (licencias.size() > 0) {
//            for (Licencia licencia : licencias) {
//                db.execSQL("INSERT INTO " + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.TABLE_LICENCIAS + " (" +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_TIPO + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_NOMBRE + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_TIPO_PERMISO_CONDUCCION + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_EDAD + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_NUM_LICENCIA + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_FECHA_EXPEDICION + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_FECHA_CADUCIDAD + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_NUM_ABONADO + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_NUM_SEGURO + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_AUTONOMIA + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_ESCALA + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_CATEGORIA +
//                        ") VALUES (" +
//                        "'" + licencia.getTipo() + "' , " +
//                        "'" + licencia.getNombre() + "' , " +
//                        "'" + licencia.getTipoPermisoConduccion() + "' , " +
//                        "'" + licencia.getEdad() + "' , " +
//                        "'" + licencia.getNumLicencia() + "' , " +
//                        "'" + licencia.getFechaExpedicion() + "' , " +
//                        "'" + licencia.getFechaCaducidad() + "' , " +
//                        "'" + licencia.getNumAbonado() + "' , " +
//                        "'" + licencia.getNumSeguro() + "' , " +
//                        "'" + licencia.getAutonomia() + "' , " +
//                        "'" + licencia.getEscala() + "' , " +
//                        "'" + licencia.getCategoria() + "'" +
//                        ");");
//            }
//        }
//        Log.d(context.getPackageName(), "Licencia actualizada en BBDD");
//    }
//
//    public void saveListTiradas(SQLiteDatabase db, ArrayList<Tirada> tiradas) {
//        if (db == null)
//            db = this.getWritableDatabase();
//        db.delete(al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.TABLE_TIRADAS, null, null); // No elimina la tabla, solo elimina las filas
//        if (tiradas.size() > 0) {
//            for (Tirada tirada : tiradas) {
//                db.execSQL("INSERT INTO " + al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.TABLE_TIRADAS + " (" +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_TIRADAS_DESCRIPCION + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_TIRADAS_RANGO + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_TIRADAS_FECHA + ", " +
//                        al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_TIRADAS_PUNTUACION +
//                        ") VALUES (" +
//                        "'" + tirada.getDescripcion() + "' , " +
//                        "'" + tirada.getRango() + "' , " +
//                        "'" + tirada.getFecha() + "' , " +
//                        "'" + tirada.getPuntuacion() + "'" +
//                        ");");
//            }
//        }
//        Log.d(context.getPackageName(), "Tirada actualizada en BBDD");
//    }
//
//    public int getNumLicenciasTipoE() {
//        SQLiteDatabase db = this.getReadableDatabase();
//        return getCursorMaxGuiasLicenciaE(db).getCount();
//    }
//
//    public int getNumGuiasLicenciaTipoEscopeta() {
//        SQLiteDatabase db = this.getReadableDatabase();
//        return getCursorMaxGuiasEscopeta(db).getCount();
//    }
//
//    public int getNumGuiasLicenciaTipoRifle() {
//        SQLiteDatabase db = this.getReadableDatabase();
//        return getCursorMaxGuiasRifle(db).getCount();
//    }
//
}

//      android adb, retrieve database using run-as
//      http://www.hermosaprogramacion.com/2014/10/android-sqlite-bases-de-datos/
//      https://www.sqlite.org/datatype3.html
//      http://www.androidhive.info/2013/09/android-sqlite-database-with-multiple-tables/