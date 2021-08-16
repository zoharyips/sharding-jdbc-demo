package com.example.shardingjdbcdemo;

import com.example.shardingjdbcdemo.model.Order;

/**
 * <h3></h3>
 *
 * @author zohar
 * @version 1.0
 * 2021/8/16 15:34:46
 */
public class ClassUtil {

    @SuppressWarnings("rawtypes")
    public static boolean hasField(Class clazz, String field) {
        try {
            clazz.getDeclaredField(field);
            return true;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println(hasField(Order.class, "id"));
    }
}
