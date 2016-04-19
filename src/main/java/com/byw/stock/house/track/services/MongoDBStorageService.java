package com.byw.stock.house.track.services;

import java.util.ArrayList;
import java.util.List;

import com.byw.web.platform.log.PlatformLogger;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindFluent;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;


/**
 * 
 * MongoDB存储服务.
 * 
 * .
 * 
 * @title MongoDBStorageService
 * @package com.byw.stock.house.track.services
 * @author baiyanwei
 * @version
 * @date Feb 16, 2016
 * 
 */
public class MongoDBStorageService {

    /**
     * logger
     */
    final private static PlatformLogger _logger = PlatformLogger.getLogger(MongoDBStorageService.class);

    private ArrayList<String> _mongoDBConnections = new ArrayList<String>();

    private String _databaseInstaceName = "STOCK";

    private MongoClient _mongoClient = null;

    private MongoDatabase _databaseInstance = null;

    public MongoDBStorageService(String connection, String dbInstanceName) {

        _mongoDBConnections.add(connection);
        this._databaseInstaceName = dbInstanceName;
    }

    public void start() throws Exception {

        initStorage();
        _logger.info("service started");
    }

    public void stop() throws Exception {

        destoryStorage();
        _logger.info("service stopped");
    }

    public void initStorage() throws Exception {

        try {
            ArrayList<ServerAddress> connectionList = new ArrayList<ServerAddress>();
            for (int i = 0; i < this._mongoDBConnections.size(); i++) {
                String[] info = this._mongoDBConnections.get(i).split(":");
                if (info.length != 2) {
                    continue;
                }
                connectionList.add(new ServerAddress(info[0], Integer.parseInt(info[1])));
            }

            this._mongoClient = new MongoClient(connectionList);
            this._databaseInstance = this._mongoClient.getDatabase(this._databaseInstaceName);
            _logger.info("create MongoDBClient instance name:" + _databaseInstaceName + ",connections:" + connectionList.toString());
        } catch (Exception e) {
            _logger.exception(e);
        }

    }

    public void destoryStorage() throws Exception {

        this._databaseInstance = null;
        this._mongoClient.close();
        this._mongoClient = null;
        _logger.info("destory MongoDB Client for instance:" + this._databaseInstaceName);

    }

    /**
     * Your Methods description is in here.
     * 
     * @param args
     */
    public void write(String collectionName, List<Document> saveList) {

        if (collectionName == null || collectionName.length() == 0 || saveList == null || saveList.isEmpty() == true) {
            return;
        }
        this._databaseInstance.getCollection(collectionName).insertMany(saveList);

    }

    public void write(String collectionName, Document doc) {

        if (collectionName == null || collectionName.length() == 0 || doc == null) {
            return;
        }
        this._databaseInstance.getCollection(collectionName).insertOne(doc);

    }

    /**
     * Your Methods description is in here.
     * 
     * @param args
     */
    public List<Document> read(String collectionName, Document queryDocument, Document sortDocument, int pageNo, int pageSize) {

        FindFluent<Document> findFluent = this._databaseInstance.getCollection(collectionName).find(queryDocument);
        findFluent.sort(sortDocument);
        findFluent.batchSize(pageNo);
        findFluent.limit(pageSize);
        //findFluent.;
        MongoCursor<Document> cursor = findFluent.iterator();
        ArrayList<Document> dataList = new ArrayList<Document>();
        try {
            while (cursor.hasNext()) {
                dataList.add(cursor.next());
            }
        } finally {
            cursor.close();
        }
        dataList.trimToSize();
        return dataList;
    }

    public MongoCollection<Document> getStockCollection(String collectionName) {

        return this._databaseInstance.getCollection(collectionName);
    }

    public static void main(String[] args) {

        MongoDBStorageService mss = new MongoDBStorageService("192.168.72.36:27017", "STOCK");
        try {
            mss.start();
            //            List<Document> saveList = new ArrayList<Document>();
            //            Document doc = new Document();
            //            doc.put("name", "baiyanwei");
            //            saveList.add(doc);
            //            mss.write("A_C", saveList);
            //            List<Document> resultList = mss.read("A_C", new Document(), new Document(), 0, 10);
            //            for (Document rd : resultList) {
            //                System.out.println(rd);
            //            }
            DBObject updateCondition = new BasicDBObject();
            //where name='fox'
            updateCondition.put("_id", new ObjectId("56ab0f92c36f4f1e76f841a1"));

            DBObject updatedValue = new BasicDBObject();
            updatedValue.put("age", 150);
            updatedValue.put("sex12", "fame");
            updatedValue.put("sex1", "fame1");

            DBObject updateSetValue = new BasicDBObject("$set", updatedValue);

            UpdateResult up = mss._databaseInstance.getCollection("A_C").updateOne(updateCondition, updateSetValue);
            System.out.println(up.getMatchedCount());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
