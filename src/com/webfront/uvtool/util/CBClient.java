/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.util;

import com.couchbase.client.java.*;
import com.couchbase.client.java.query.*;

/**
 *
 * @author rlittle
 */
public class CBClient {

    private final NetworkTopography platforms = NetworkTopography.getInstance();
//    private final String cbHost = "http://corvette";
    private final String cbHost = platforms.getCbHost();
    private final String cbUser = "release";
    private final String cbPassword = "R31ea$E_@)!(";

    public CBClient() {

    }

    public Bucket connect(String bucketName) {
        Cluster cluster = CouchbaseCluster.create(cbHost);
        cluster.authenticate(cbUser, cbPassword);
        Bucket bucket = cluster.openBucket(bucketName);
        return bucket;
    }

    public void disconnect(Bucket bucket) {
        bucket.close();
    }

    public void testQuery(Bucket bucket) {
        String query = "select name from " + bucket.name() + " where name like \"getAopIbvVendorInfo.uvs%\"";
        SimpleN1qlQuery q = N1qlQuery.simple(query);
        N1qlQueryResult result = bucket.query(q);

        // Print each found Row
        for (N1qlQueryRow row : result) {
            System.out.println(row);
        }
    }
    
    public N1qlQueryResult doQuery(Bucket bucket, String query) {
        SimpleN1qlQuery q = N1qlQuery.simple(query);
        N1qlQueryResult result = bucket.query(q);
        return result;
    }
}
