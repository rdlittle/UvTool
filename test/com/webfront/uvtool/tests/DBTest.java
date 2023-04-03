/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.tests;

import com.couchbase.client.java.*;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author rlittle
 */
public class DBTest {
    
    public DBTest() {
    }
 


    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    @Test
    public void connect() {
        System.out.println("DBTest.connect");
        Cluster cluster = CouchbaseCluster.create("http://corvette");
        cluster.authenticate("release", "R31ea$E_@)!(");
        Bucket bucket = cluster.openBucket("deployBackup");
        assertTrue(bucket != null);
        bucket.close();
        assertTrue(bucket.isClosed());
        
    }
}
