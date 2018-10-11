package me.zeroeightsix.kami.setting;

import me.zeroeightsix.kami.util.Bind;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 086 on 25/08/2017.
 */
public class SettingsClass {

    ArrayList<StaticSetting> settings = new ArrayList<>();

    public void initSettings() {
        settings.clear();

        Class parent = getClass();
        while (SettingsClass.class.isAssignableFrom(parent)) {
            for (Field field : parent.getDeclaredFields()) {
                if (!field.isAnnotationPresent(Setting.class)) continue;
                field.setAccessible(true);
                Setting annot = field.getAnnotation(Setting.class);
                String fname;
                fname = getClass().getCanonicalName() + "~" + field.getName();

                StaticSetting setting;
                try {
                    Object val = field.get(this);
                    setting = new StaticSetting(fname, annot.name(), this, field, val);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    setting = new StaticSetting(fname, annot.name(), this, field, null);
                }
                settings.add(setting);
            }
            parent = parent.getSuperclass();
        }

        SettingsPool.flushClass(this);
    }

    public ArrayList<StaticSetting> getSettings() {
        return settings;
    }

    public StaticSetting getSettingByDisplayName(String displayName) {
        return settings.stream().filter(staticSetting -> staticSetting.getDisplayName().equalsIgnoreCase(displayName)).findFirst().orElse(null);
    }

    public StaticSetting getSettingByFullName(String fullName) {
        return settings.stream().filter(staticSetting -> staticSetting.getFullName().equalsIgnoreCase(fullName)).findFirst().orElse(null);
    }

    public StaticSetting getSettingByFieldName(String fieldName) {
        return settings.stream().filter(staticSetting -> staticSetting.getFieldName().equalsIgnoreCase(fieldName)).findFirst().orElse(null);
    }

    public static class StaticSetting {
        String fullName;
        String displayName;
        SettingsClass holder;
        Field field;
        Object defaultValue;

        public StaticSetting(String fullName, String displayName, SettingsClass holder, Field field, Object defaultValue) {
            this.fullName = fullName;
            this.displayName = displayName;
            this.holder = holder;
            this.field = field;
            this.defaultValue = defaultValue;
        }

        public Field getField() {
            return field;
        }

        public String getFieldName() {
            return getField().getName();
        }

        public Object getValue() {
            try {
                return getField().get(holder);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getFullName() {
            return fullName;
        }

        public void setValue(Object value) {
            Class fieldType = field.getType();

            if (Enum.class.isAssignableFrom(fieldType)) {
                if (fieldType.isAssignableFrom(value.getClass())) {
                    try {
                        field.set(holder, value);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }else{
                    try {
                        field.set(holder, valueOfIgnoreCase(fieldType, value.toString()));
                    }catch (IllegalArgumentException e) {
                        throw new RuntimeException("'" + value + "' doesn't belong in enum " + fieldType.getSimpleName());
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                return;
            }

            if (value.getClass() == String.class) {
                Class boxedType = (Class) getKeyFromValue(typeMap, fieldType);
                String name = capitalizeFirstLetter(fieldType.getName());

                try {
                    value = boxedType.getMethod("parse" + name, String.class).invoke(null, value);
                    Setting annot = field.getAnnotation(Setting.class);
                    double v = Double.parseDouble(value.toString());
                    if (annot.max() != -1 && v > annot.max()) throw new RuntimeException(v + " exceeds max value of " + annot.max());
                    if (annot.min() != -1 && v < annot.min()) throw new RuntimeException(v + " exceeds min value of " + annot.min());
                    field.set(holder, value);
                    return;
                } catch (Exception e) {
                    if (e instanceof InvocationTargetException)
                        e = (Exception) e.getCause(); // Unwrap
                    if (e instanceof NumberFormatException) {
                        try{
                            Float.parseFloat(value.toString().trim());
                            throw new RuntimeException("Value can't be a floating point number.");
                        }catch (NumberFormatException e1) {}
                        throw new RuntimeException("Value must be numerical!");
                    }
                    e.printStackTrace();
                    throw new RuntimeException(e.getMessage());
                }
            }

            if (value.getClass() == ArrayList.class || value.getClass() == Bind.class) {
                try {
                    field.set(holder, value);
                    return;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            Class primitiveType = typeMap.get(value.getClass());
            if (primitiveType == null)
                throw new RuntimeException("Unsupported value: " + value.getClass().getSimpleName());
            try {
                value = value.getClass().getDeclaredMethod(fieldType.getName() + "Value", null).invoke(value);

                field.set(holder, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static Map<Class,Class> typeMap = new HashMap<>();{
        typeMap.put(Integer.class, int.class);
        typeMap.put(Long.class, long.class);
        typeMap.put(Double.class, double.class);
        typeMap.put(Float.class, float.class);
        typeMap.put(Boolean.class, boolean.class);
        typeMap.put(Character.class, char.class);
        typeMap.put(Byte.class, byte.class);
        typeMap.put(Short.class, short.class);
    }

    private static String capitalizeFirstLetter(String original) {
        if (original == null || original.length() == 0) {
            return original;
        }
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }

    private static Object getKeyFromValue(Map hm, Object value) {
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return o;
            }
        }
        return null;
    }

    /**
     * Finds the value of the given enumeration by name, case-insensitive.
     * Throws an IllegalArgumentException if no match is found.
     **/
    public static <T extends Enum<T>> T valueOfIgnoreCase(
            Class<T> enumeration, String name) {

        for (T enumValue : enumeration.getEnumConstants()) {
            if (enumValue.name().equalsIgnoreCase(name)) {
                return enumValue;
            }
        }

        throw new IllegalArgumentException("There is no value with name '" + name + "' in Enum " + enumeration.getName());
    }

}
