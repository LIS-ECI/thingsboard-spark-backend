/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.connection.dao;

import java.util.List;

/**
 *
 * @author Sonia
 */
public interface Dao<T> {

    List<T> find();

    T findById(String id);

    T save(T t);

    boolean removeById(String id);

}
