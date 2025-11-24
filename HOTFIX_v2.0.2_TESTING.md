# Testing Plan for Hotfix v2.0.2

## Critical Bug Fixed: Database Deletion on Purchase Creation

### What Was Fixed

**Root Cause**: Database schema type mismatch
- Field `peso` was defined as TEXT in database schema but treated as INTEGER in code
- This caused SQLite exceptions during data insertion
- Since `saveListCompras()` deleted all data BEFORE inserting, any insertion failure resulted in complete data loss

**Changes Made**:
1. Fixed schema: Changed `peso` from TEXT to INTEGER
2. Fixed `valoracion` reading from `getInt()` to `getFloat()`
3. Added SQL transactions to prevent data loss
4. Added cursor closing to prevent memory leaks
5. Added number parsing error handling with Spanish locale support
6. Incremented database version from 22 to 23

---

## Testing Checklist

### Phase 1: Database Migration Test

✓ **Test 1: Existing Data Preservation**
1. Install current production version (2.0.1) if you have test data
2. Add some test guias, compras, licencias if needed
3. Note down the data you have
4. Install the new version (2.0.2)
5. Open the app
6. **Expected**: App should upgrade database from v22 to v23 automatically
7. **Verify**: All your existing data is still there (guias, compras, licencias, tiradas)

### Phase 2: Purchase Creation Tests

✓ **Test 2: Create Purchase with Valid Integer Peso**
1. Open app → Navigate to Compras tab
2. Click FAB (+) button
3. Select a guia
4. Fill form:
   - Calibre 1: "9mm"
   - Unidades: "50"
   - Precio: "25.50" or "25,50" (both should work)
   - Peso: "115" ← **This is the critical field**
   - Marca: "Fiocchi"
   - Tienda: "Armeria Test"
5. Save
6. **Expected**: Purchase saved successfully
7. Close app completely
8. Reopen app
9. **Verify**: Purchase is still there (database not deleted)
10. **Verify**: Guia's cupo decreased correctly

✓ **Test 3: Create Purchase with Zero Peso**
1. Create another purchase
2. Leave Peso field empty (defaults to "0")
3. Save
4. **Expected**: Purchase saved successfully
5. **Verify**: No data loss

✓ **Test 4: Create Purchase with Large Peso Value**
1. Create another purchase
2. Peso: "250"
3. Save
4. **Expected**: Purchase saved successfully
5. **Verify**: Database not deleted
6. **Verify**: All previous purchases still visible

### Phase 3: Number Parsing Error Handling

✓ **Test 5: Invalid Number in Unidades**
1. Try to create purchase
2. Unidades: "abc" (invalid)
3. Save
4. **Expected**: Toast message "Error: Unidades debe ser un número válido"
5. **Expected**: Error shown on field
6. **Expected**: Form NOT saved
7. **Expected**: App does NOT crash

✓ **Test 6: Invalid Number in Precio**
1. Try to create purchase
2. Precio: "abc€" (invalid)
3. Save
4. **Expected**: Toast message about invalid precio
5. **Expected**: App does NOT crash

✓ **Test 7: Spanish Decimal Separator**
1. Create purchase
2. Precio: "12,50€" (comma as decimal separator)
3. **Expected**: Should be parsed correctly as 12.50
4. **Expected**: Purchase saved successfully

✓ **Test 8: Dot Decimal Separator**
1. Create purchase
2. Precio: "12.50€" (dot as decimal separator)
3. **Expected**: Should be parsed correctly as 12.50
4. **Expected**: Purchase saved successfully

✓ **Test 9: Invalid Peso**
1. Try to create purchase
2. Peso: "abc" (invalid)
3. Save
4. **Expected**: Toast message about invalid peso
5. **Expected**: App does NOT crash

### Phase 4: Memory Leak Verification

✓ **Test 10: Navigate Between Tabs Multiple Times**
1. Open app
2. Navigate: Guias → Compras → Licencias → Tiradas
3. Repeat 20 times
4. Create purchases, guias, licencias
5. Navigate between tabs after each creation
6. **Expected**: App remains responsive
7. **Expected**: No ANR (App Not Responding)
8. **Expected**: No Out of Memory errors

✓ **Test 11: Database Operations**
1. Create 10 purchases rapidly
2. Delete a purchase
3. Edit a purchase
4. Navigate to other tabs
5. **Expected**: No memory issues
6. **Expected**: All operations complete successfully

### Phase 5: Crashlytics Verification

✓ **Test 12: Check Crashlytics Dashboard**
1. After testing for 24 hours
2. Open Firebase Crashlytics dashboard
3. **Verify**: No new "database deletion" crashes
4. **Verify**: NumberFormatException crashes are 0 (or handled gracefully)
5. **Verify**: No SQLite exception crashes

### Phase 6: Edge Cases

✓ **Test 13: Network Offline/Online**
1. Turn off WiFi and mobile data
2. Create purchases (should save to SQLite)
3. Turn on network
4. App should sync to Firebase
5. **Verify**: No data loss in either state

✓ **Test 14: App Kill During Save**
1. Create a purchase
2. Click Save
3. Immediately force-kill app (swipe from recents)
4. Reopen app
5. **Expected**: Either purchase is saved completely OR not saved at all
6. **Expected**: Database NOT corrupted or deleted

✓ **Test 15: Database Transaction Rollback**
To simulate this, you'd need to:
1. Temporarily break one field insertion (requires code change)
2. Try to save
3. **Expected**: Transaction should rollback
4. **Expected**: Previous data still intact

---

## Regression Testing

### Verify Other Features Still Work

✓ **Test 16: Guias**
1. Create, edit, delete guias
2. **Verify**: All operations work
3. **Verify**: Images still work

✓ **Test 17: Licencias**
1. Create, edit, delete licencias
2. **Verify**: Calendar events created correctly
3. **Verify**: Notifications work

✓ **Test 18: Tiradas**
1. Create, edit, delete tiradas
2. **Verify**: Date calculations work
3. **Verify**: Sorting works

✓ **Test 19: Quota Calculation**
1. Create guia with cupo 100
2. Create compra with 50 unidades
3. **Verify**: Guia shows gastado = 50
4. Create another compra with 30 unidades
5. **Verify**: Guia shows gastado = 80
6. Delete first compra
7. **Verify**: Guia shows gastado = 30

✓ **Test 20: Firebase Sync**
1. Create data on device A
2. Login with same account on device B
3. **Verify**: Data syncs correctly
4. **Verify**: No duplicate data
5. **Verify**: No data loss

---

## Performance Testing

✓ **Test 21: Database Performance**
1. Create 100 purchases
2. Navigate to Compras tab
3. **Measure**: Load time should be < 1 second
4. Scroll through list
5. **Verify**: Smooth scrolling

✓ **Test 22: Memory Usage**
1. Open Android Studio Profiler
2. Run app
3. Create 20 purchases
4. Navigate between tabs
5. **Monitor**: Memory usage
6. **Expected**: Memory should not continuously increase
7. **Expected**: Garbage collector should reclaim memory

---

## Acceptance Criteria

**The hotfix is successful if:**

✅ No database deletion occurs when creating purchases with any peso value
✅ All existing data is preserved after upgrade
✅ Invalid number input shows error instead of crashing
✅ Spanish decimal separators work correctly
✅ No memory leaks detected
✅ All regression tests pass
✅ Crashlytics shows 0 database-related crashes after 48 hours in production

**If ANY test fails, DO NOT RELEASE. Report the failure immediately.**

---

## Rollback Plan

If critical issues are found in production:

1. Revert to v2.0.1 immediately in Play Store
2. Notify users via in-app message or push notification
3. Analyze Crashlytics logs
4. Fix issue in develop branch
5. Test again before re-releasing

---

## Post-Release Monitoring

**First 24 Hours:**
- Monitor Crashlytics every 2 hours
- Check user reviews
- Monitor server logs for sync issues

**First Week:**
- Daily Crashlytics review
- User feedback analysis
- Performance metrics review

**Success Metrics:**
- Crash-free rate > 99.5%
- Database deletion incidents = 0
- User retention unchanged or improved
- Negative reviews due to data loss = 0
