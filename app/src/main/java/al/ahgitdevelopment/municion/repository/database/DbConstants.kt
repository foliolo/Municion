package al.ahgitdevelopment.municion.repository.database

// Table Names
const val TABLE_PROPERTIES = "properties"
const val TABLE_PURCHASES = "purchases"
const val TABLE_LICENSES = "licenses"
const val TABLE_COMPETITION = "competitions"

// Common column names
const val KEY_ID = "id"

// Table PROPERTY - column names
const val KEY_PROPERTY_NICKNAME = "nickname"
const val KEY_PROPERTY_BRAND = "brand"
const val KEY_PROPERTY_MODEL = "model"
const val KEY_PROPERTY_BORE1 = "bore1"
const val KEY_PROPERTY_BORE2 = "bore2"
const val KEY_PROPERTY_NUM_ID = "num_id"
const val KEY_PROPERTY_IMAGE = "image"

// Table PURCHASE - column names
const val KEY_PURCHASE_BORE1 = "bore1"
const val KEY_PURCHASE_UNITS = "units"
const val KEY_PURCHASE_PRICE = "price"
const val KEY_PURCHASE_DATE = "date"
const val KEY_PURCHASE_WEIGHT = "weight"
const val KEY_PURCHASE_BRAND = "brand"
const val KEY_PURCHASE_STORE = "store"
const val KEY_PURCHASE_RATING = "rating"
const val KEY_PURCHASE_IMAGE = "image"

// Table LICENSE - column names
const val KEY_LICENSE_NAME = "name"
const val KEY_LICENSE_DATE_ISSUE = "date_issue"
const val KEY_LICENSE_DATE_EXPIRY = "date_expiry"
const val KEY_LICENSE_NUMBER = "number"
const val KEY_LICENSE_INSURANCE_NUMBER = "insurance_number"

// Table TIRADAS - column names
const val KEY_COMPETITION_DESCRIPTION = "description"
const val KEY_COMPETITION_RANKING = "ranking"
const val KEY_COMPETITION_DATE = "date"
const val KEY_COMPETITION_POINTS = "points"
const val KEY_COMPETITION_PLACE = "place"

// Database Version
const val DATABASE_VERSION = 2

// Database Name
const val DATABASE_NAME = "Weapons.db"
