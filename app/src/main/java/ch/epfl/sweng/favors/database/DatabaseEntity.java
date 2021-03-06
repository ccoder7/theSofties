package ch.epfl.sweng.favors.database;

import android.databinding.Observable;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.epfl.sweng.favors.database.fields.DatabaseBooleanField;
import ch.epfl.sweng.favors.database.fields.DatabaseField;
import ch.epfl.sweng.favors.database.fields.DatabaseLongField;
import ch.epfl.sweng.favors.database.fields.DatabaseObjectField;
import ch.epfl.sweng.favors.database.fields.DatabaseStringField;


/**
 * Database entity defines the capabilities
 *
 * The data in the entity is stored in the form of a map
 * indicating the type of data as key
 * and the data as value
 *
 * There are currently following datatypes (illustrated with examples)
 * String data - names and other personal information
 * long data - amount of tokens
 * boolean data - permissions, notification settings
 * Object data - location
 */
public abstract class DatabaseEntity implements Observable {

    protected static Database db = Database.getInstance();

    protected Map<DatabaseStringField, ObservableField<String>> stringData;
    protected Map<DatabaseLongField, ObservableField<Long>> longData;
    protected Map<DatabaseBooleanField, ObservableField<Boolean>> booleanData;
    protected Map<DatabaseObjectField, ObservableField<Object>> objectData;

    protected final String collection;
    protected String documentID;

    /**
     * The type of update
     * DATA update is issued when there have been local changes
     * FROM_DB update is issued there have been remote changes that should be pushed to the user's app
     * FROM_REQUEST if a dedicated set has been invoked to change something specific in this entity
     */
    public enum UpdateType{
        DATA,
        FROM_DB,
        FROM_REQUEST
    }

    private final static String TAG = "Favors_DatabaseHandler";

    public void setId(String documentId){
        this.documentID = documentId;
    }

    /**
     * @return documentID of this database entity
     */
    public String getId() {return documentID;}

    /**
     * Init a database object with all fields that are possible for the instanced collection in the database
     *
     * @param stringFieldsValues The possibles names of string objects in the database
     * @param longFieldsValues The possibles names of int objects in the database
     * @param booleanFieldsValues The possibles names of boolean objects in the database
     * @param objectFieldsValues The possibles names of generic objects in the database that must be test
     *                           later, often tables
     * @param collection The collection in the database
     * @param documentID If so, the doccumentId in the database
     */
    public DatabaseEntity(DatabaseStringField stringFieldsValues[], DatabaseLongField longFieldsValues[],
                          DatabaseBooleanField booleanFieldsValues[], DatabaseObjectField objectFieldsValues[],
                          String collection, String documentID){

        assert(collection != null);

        stringData = initMap(stringFieldsValues);
        longData = initMap(longFieldsValues);
        booleanData = initMap(booleanFieldsValues);
        objectData = initMap(objectFieldsValues);


        this.collection = collection;
        this.documentID = documentID;
    }

    /**
     * Init the map with a null value for every possible object of a specific type
     *
     * @param possibleValues An array containing the possible names of this kind of object
     * @param <T> The field enum type
     * @param <V> The type of objects
     * @return An initialised map
     */
    private <T extends DatabaseField, V> Map<T, ObservableField<V>>  initMap(final T[] possibleValues){
        if(possibleValues == null || possibleValues.length == 0){return null;}
        return new HashMap<T, ObservableField<V>>(){
            {
                for(T field : possibleValues){
                    this.put(field, new ObservableField<V>());
                }
            }

        };
    }

    /**
     * Return a uniform map with all data to send
     *
     * @return The maps with objects names and values
     */
    protected Map<String, Object> getEncapsulatedObjectOfMaps(){
        Map<String, Object> toSend = new HashMap<>();

        convertTypedMapToObjectMap(stringData, toSend);
        convertTypedMapToObjectMap(booleanData, toSend);
        convertTypedMapToObjectMap(longData, toSend);
        convertTypedMapToObjectMap(objectData, toSend);

        return toSend;
    }

    /**
     * Update local data with a generic content with Objects
     *
     * @param incomingData The map with object content and object value
     * @return True is successful
     */
    protected void updateLocalData(Map<String, Object> incomingData){
        if(incomingData == null){
            return;
        }
        convertObjectMapToTypedMap(incomingData, stringData, String.class);
        convertObjectMapToTypedMap(incomingData, booleanData, Boolean.class);
        convertObjectMapToTypedMap(incomingData, objectData, Object.class);
        convertObjectMapToTypedMap(incomingData, longData, Long.class);

        for (OnPropertyChangedCallback callback : callbacks){
            callback.onPropertyChanged(this, UpdateType.FROM_DB.ordinal());
        }

        notifyContentChange();
    }

    /**
     * Convert a string / object map to local typed object map
     *
     * @param from  The received map to convert
     * @param to    The map where to add typed object
     * @param <T>   The enum of map content
     * @param <V>   The ObservableField content type
     * @param <U>   The ObservableField
     */
    private <T extends DatabaseField, V , U extends ObservableField<V>> void convertObjectMapToTypedMap(Map<String, Object> from, Map<T, U> to, Class<V> clazz) {
        if(from == null || to == null){return;}
        for (Map.Entry<T, U> entry : to.entrySet()){
            T fieldName = entry.getKey();
            Object object = from.get(fieldName.toString());
            if(object != null){
                try {
                    to.get(fieldName).set(clazz.cast(object));
                } catch(ClassCastException e) {
                    Log.e(TAG, "Error while casting incomming data");
                }
            }
        }
    }

    /**
     * Convert the map containing some parameters in an String / Object map
     *
     * @param from  The original map to convert
     * @param to    The map where to add objects
     * @param <T>   The enum of map content
     * @param <V>   The ObservableField content type
     * @param <U>   The ObservableField
     */
    private <T extends DatabaseField, V, U extends ObservableField<V>> void convertTypedMapToObjectMap(Map<T, U> from, Map<String, Object> to) {
        if(from == null || to == null){return;}
        for (Map.Entry<T, U> entry : from.entrySet()){
            V value = (V) entry.getValue().get();
            if(value != null) to.put(entry.getKey().toString(), value);
        }
    }

    /**
     * resets the data of this database entity to the default value null
     * this clears all of the information that had previously been in this
     * database field
     */
    public void reset(){
        if(stringData != null)
            resetMap(stringData, null);
        if(booleanData != null)
            resetMap(booleanData, null);
        if(objectData != null)
            resetMap(objectData, null);
        if(longData != null)
            resetMap(longData,null);
        notifyContentChange();
    }

    /**
     * Clears a map of all the data that is contained in it and replaces it with the default value passed in.
     *
     * @param map Map that needs to be cleared of information
     * @param defaultValue What value should be placed in the map when being cleared
     * @param <K> Key type of the map
     * @param <V> Value contained in the observableField of the map
     */
    protected  <K,V> void resetMap(@NonNull Map<K, ObservableField<V>> map, V defaultValue) {
        for(K key : map.keySet()){
            map.get(key).set(defaultValue);
        }
    }

    List<OnPropertyChangedCallback> callbacks = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void addOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        assert(callback != null);
        callbacks.add(callback);
    }

    @Override
    public void removeOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        callbacks.remove(callback);
    }

    /**
     * Callback that indicates a DATA update
     */
    private void notifyContentChange() {
        for (OnPropertyChangedCallback callback : callbacks){
            callback.onPropertyChanged(this, UpdateType.DATA.ordinal());
        }
    }

    /**
     * the database set method updates local content and
     * issues a callback indicating a FROM_REQUEST update
     *
     * @param id documentID of this database entity
     * @param content to be set (changed)
     */
    public void set(String id, Map<String, Object> content) {
        this.documentID = id;
        this.updateLocalData(content);
        for (OnPropertyChangedCallback callback : callbacks) {
            callback.onPropertyChanged(this, UpdateType.FROM_REQUEST.ordinal());
        }
    }

    /**
     * Get / set methods for the different types of data
     * @param field
     * @return
     */
    public String get(DatabaseStringField field) {
        if(stringData.get(field) != null)
            return stringData.get(field).get();
        else
            return null;
    }

    /**
     * Sets a new String in the database and issues a DATA update
     *
     * @param field in the database
     * @param value string to be put there
     */
    public void set(DatabaseStringField field, String value) {
        stringData.get(field).set(value);
        notifyContentChange();
    }

    public ObservableField<String> getObservableObject(DatabaseStringField field) {
        return stringData.get(field);
    }

    /**
     * Gets an Object like location from the database
     * TODO the if is redundant since objectData.get is nullable and will return without an exception
     * Although we agree it is good to explicitly show this, it would perhaps be better
     * to return an Optional Object
     *
     * @param field in the database representing an object
     * @return the Object from the database
     */
    @Nullable
    public Object get(DatabaseObjectField field) {
        if(objectData.get(field) != null)
            return objectData.get(field).get();
        else
            return null;
    }

    /**
     * Sets a new Object in the database and issues a DATA update
     *
     * @param field in the database
     * @param value Object to be put there
     */
    public void set(DatabaseObjectField field, Object value){
        objectData.get(field).set(value);
        notifyContentChange();
    }

    public ObservableField<Object> getObservableObject(DatabaseObjectField field) {
        return objectData.get(field);
    }

    /**
     * Gets a Long value from the database
     * TODO the if is redundant since longData.get is nullable and will return without an exception
     * Although we agree it is good to explicitly show this, it would perhaps be better
     * to return an Optional value
     *
     * @param field in the database representing a Long
     * @return the Long from the database
     */
    public Long get(DatabaseLongField field) {
        if(longData.get(field) != null)
            return longData.get(field).get();
        else
            return null;
    }

    /**
     * Sets a new long in the database and issues a DATA update
     *
     * @param field in the database
     * @param value long to be put in database
     */
    public void set(DatabaseLongField field, Long value) {
        longData.get(field).set(value);
        notifyContentChange();
    }

    public ObservableField<Long> getObservableObject(DatabaseLongField field) {
        return longData.get(field);
    }

    /**
     * Gets a Boolean value from the database
     * TODO the if is redundant since booleanData.get is nullable and will return without an exception
     * Although we agree it is good to explicitly show this, it would perhaps be better
     * to return an Optional value
     *
     * @param field in the database representing a Boolean
     * @return the Boolean from the database
     */
    public Boolean get(DatabaseBooleanField field) {
        if(booleanData.get(field) != null)
            return booleanData.get(field).get();
        else
            return null;
    }

    /**
     * Sets a new Boolean in the database and issues a DATA update
     *
     * @param field in the database
     * @param value Boolean to be put in database
     */
    public void set(DatabaseBooleanField field, Boolean value) {
        booleanData.get(field).set(value);
        notifyContentChange();
    }

    public ObservableField<Boolean> getObservableObject(DatabaseBooleanField field) {
        return booleanData.get(field);
    }

    /**
     * @return database entity for class extending this entity (user / favor)
     */
    public abstract DatabaseEntity copy();

}
