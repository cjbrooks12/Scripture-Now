package com.caseybrooks.androidbibletools.data;

import com.caseybrooks.androidbibletools.basic.AbstractVerse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class Metadata {
	//test comment
    private HashMap<String, Object> items;

    public Metadata() {
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

    public void put(String key, Object value) {
        if(value instanceof Comparable) {
            items.put(key, value);
        }
        else {
            throw new IllegalArgumentException("Objects must implement " + Comparable.class.getName() + ". [" + value.getClass().getName() + "] does not name a Comparable type");
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

    /** Returns the integer value of @key from the map. If the object with key @key is not an
     * Integer, throw an exception. If the key does not exist, return the default value.
     * */
    public int getInt(String key, int defValue) {
        if(items.containsKey(key)) {
            Object item = items.get(key);
            if (item != null) {
                if (!items.get(key).getClass().equals(Integer.class)) {
                    throw new ClassCastException("Key [" + key + "] expected result of type [" + Integer.class.toString() + "], found [" + items.get(key).getClass().toString() + "]");
                }
                else {
                    return (int) item;
                }
            }
        }
        return defValue;
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public long getLong(String key, long defValue) {
        if(items.containsKey(key)) {
            Object item = items.get(key);
            if (item != null) {
                if (!items.get(key).getClass().equals(Long.class)) {
                    throw new ClassCastException("Key [" + key + "] expected result of type [" + Long.class.toString() + "], found [" + items.get(key).getClass().toString() + "]");
                }
                else {
                    return (long) item;
                }
            }
        }
        return defValue;
    }

    public long getLong(String key) {
        return getLong(key, 0l);
    }

    public boolean getBoolean(String key, boolean defValue) {
        if(items.containsKey(key)) {
            Object item = items.get(key);
            if (item != null) {
                if (!items.get(key).getClass().equals(Boolean.class)) {
                    throw new ClassCastException("Key [" + key + "] expected result of type [" + Boolean.class.toString() + "], found [" + items.get(key).getClass().toString() + "]");
                }
                else {
                    return (boolean) item;
                }
            }
        }
        return defValue;
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public String getString(String key, String defValue) {
        if(items.containsKey(key)) {
            Object item = items.get(key);
            if (item != null) {
                if (!items.get(key).getClass().equals(String.class)) {
                    throw new ClassCastException("Key [" + key + "] expected result of type [" + String.class.toString() + "], found [" + items.get(key).getClass().toString() + "]");
                }
                else {
                    return (String) item;
                }
            }
        }
        return defValue;
    }

    public String getString(String key) {
        return getString(key, "");
    }

    public static class Comparator implements java.util.Comparator<AbstractVerse> {
        public static String KEY_REFERENCE = "KEY_REF";
        public static String KEY_REFERENCE_ALPHABETICAL = "KEY_REFERENCE_ALPHABETICAL";
        String key;

        public Comparator(String key) {
            this.key = key;
        }

        //TODO: first check to see if Metadata objects contain the appropriate key, and throw exception if either does not
        @Override
        public int compare(AbstractVerse a, AbstractVerse b) {
            if(key.equals(KEY_REFERENCE)) {
                return a.getReference().compareTo(b.getReference());
            }
            else if(key.equals(KEY_REFERENCE_ALPHABETICAL)) {
                return a.getReference().toString().compareTo(b.getReference().toString());
            }
            else {
                Object lhs = a.getMetadata().get(key);
                Object rhs = b.getMetadata().get(key);
                if (lhs.getClass().equals(rhs.getClass())) {
                    return ((Comparable) lhs).compareTo((Comparable) rhs);
                } else {
                    throw new ClassCastException("Objects are not of the same Class: " + lhs.getClass().toString() + " " + rhs.getClass().toString());
                }
            }
        }
    }

	public static class MultiComparator implements java.util.Comparator<AbstractVerse> {
		ArrayList<Comparator> comparisonCriteria;

		public MultiComparator(ArrayList<Comparator> comparisonCriteria) {
			this.comparisonCriteria = comparisonCriteria;
		}

		@Override
		public int compare(AbstractVerse lhs, AbstractVerse rhs) {
			for (Comparator comparator : comparisonCriteria) {
				int comparison = comparator.compare(lhs, rhs);
				if (comparison != 0) return comparison;
			}
			return 0;
		}
	}
}
