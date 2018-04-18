/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.connection;

import com.mongodb.client.MongoCollection;

/**
 *
 * @author Sonia
 */
public abstract class MongoConnectionPOJO<T> extends MongoConnection{
    public abstract MongoCollection<T> getCollectionDependClass();
}
