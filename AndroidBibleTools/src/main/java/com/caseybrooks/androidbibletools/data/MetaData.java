package com.caseybrooks.androidbibletools.data;

import com.caseybrooks.androidbibletools.basic.AbstractVerse;

import java.util.HashMap;
import java.util.Set;

public class MetaData {
    private HashMap<String, Object> items;

    public MetaData() {
        items = new HashMap<>();
    }

    public boolean containsKey(String key) {
        return items.containsKey(key);
    }

    public Class checkType(String key) {
        return items.get(key).getClass();
    }

    public int size() {
        return items.size();
    }

    public Set<String> getKeys() {
        return items.keySet();
    }

    private void put(String key, Object value) {
        if(value instanceof Comparable) {
            items.put(key, value);
        }
    }

    public void putInt(String key, int value) {
        put(key, Integer.valueOf(value));
    }

    public void putLong(String key, long value) {
        put(key, Long.valueOf(value));
    }

    public void putBoolean(String key, boolean value) {
        put(key, Boolean.valueOf(value));
    }

    public void putString(String key, String value) {
        put(key, value);
    }

    private Object get(String key) {
        return items.get(key);
    }

    public int getInt(String key) {
        if(items.get(key).getClass().equals(Integer.class)) {
            return (int) items.get(key);
        }
        else {
            throw new ClassCastException("Key [" + key + "] expected result of type [" + Integer.class.toString() + "], found [" + items.get(key).getClass().toString() + "]");
        }
    }

    public Long getLong(String key) {
        if(items.containsKey(key)) {
            if (items.get(key).getClass().equals(Long.class)) {
                return (long) items.get(key);
            } else {
                throw new ClassCastException("Key [" + key + "] expected result of type [" + Long.class.toString() + "], found [" + items.get(key).getClass().toString() + "]");
            }
        }
        else return 0l; //default value of 0
    }

    public boolean getBoolean(String key) {
        if(items.containsKey(key)) {
            if (items.get(key).getClass().equals(Boolean.class)) {
                return (boolean) items.get(key);
            } else {
                throw new ClassCastException("Key [" + key + "] expected result of type [" + Boolean.class.toString() + "], found [" + items.get(key).getClass().toString() + "]");
            }
        }
        else return false; //default value of false
    }

    public int getString(String key) {
        if(items.get(key).getClass().equals(String.class)) {
            return (int) items.get(key);
        }
        else {
            throw new ClassCastException("Key [" + key + "] expected result of type [" + String.class.toString() + "], found [" + items.get(key).getClass().toString() + "]");
        }
    }

    public static class Comparator implements java.util.Comparator<AbstractVerse> {
        public static String KEY_REFERENCE = "KEY_REF";
        public static String KEY_REFERENCE_ALPHABETICAL = "KEY_REFERENCE_ALPHABETICAL";
        String key;

        public Comparator(String key) {
            this.key = key;
        }

        @Override
        public int compare(AbstractVerse a, AbstractVerse b) {
            if(key.equals(KEY_REFERENCE)) {
                return a.getReference().compareTo(b.getReference());
            }
            else if(key.equals(KEY_REFERENCE_ALPHABETICAL)) {
                return a.getReference().toString().compareTo(b.getReference().toString());
            }
            else {
                Object lhs = a.getMetaData().get(key);
                Object rhs = b.getMetaData().get(key);
                if (lhs.getClass().equals(rhs.getClass())) {
                    return ((Comparable) lhs).compareTo((Comparable) rhs);
                } else {
                    throw new ClassCastException("Objects are not of the same Class: " + lhs.getClass().toString() + " " + rhs.getClass().toString());
                }
            }
        }
    }

}
