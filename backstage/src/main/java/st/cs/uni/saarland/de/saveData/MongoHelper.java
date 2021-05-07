package st.cs.uni.saarland.de.saveData;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kuznetsov on 01/08/16.
 */
public class MongoHelper {
    public static final String DB_HOST = "134.96.235.101";//TODO: put into config file
    public static final int DB_PORT = 27017;//TODO: put into config file

    public static final String DB_NAME = "google_play";
    public static final String TOP_LIST_NAME = "top_list";
    public static final String FS_UI = "fs_ui";
    public static final String FS_API = "fs_api";

    private static MongoHelper instance;
    private final MongoCollection<Document> topList;
    private GridFSBucket gridFSapi;
    private GridFSBucket gridFSui;

    private MongoHelper() {
        MongoClient client = new MongoClient(DB_HOST, DB_PORT);
        gridFSapi = GridFSBuckets.create(client.getDatabase(DB_NAME), FS_API);
        gridFSui = GridFSBuckets.create(client.getDatabase(DB_NAME), FS_UI);
        MongoDatabase db = client.getDatabase(DB_NAME);
        topList = db.getCollection(TOP_LIST_NAME);
    }

    public static MongoHelper getInstance() {
        if (instance == null)
            instance = new MongoHelper();
        return instance;
    }

    private String stripPkgName(String value) {
        if (value.endsWith(".apk")) {
            return value.substring(0, value.length() - 4);
        }
        return value;
    }

    private void saveResult(String fileName, InputStream stream, String version, GridFSBucket fs) {
        Map<String, Object> meta = new HashMap<>();

        meta.put("version", version);
        meta.put("pkg_name", stripPkgName(fileName));
        GridFSUploadOptions options = new GridFSUploadOptions().chunkSizeBytes(1024).metadata(new Document(meta));
        ObjectId fileId = fs.uploadFromStream(fileName, stream, options);

    }

    public void saveAPIResult(String fileName, InputStream stream, String version) {
        saveResult(fileName, stream, version, gridFSapi);
    }

    public void saveUIResult(String fileName, InputStream stream, String version) {
        saveResult(fileName, stream, version, gridFSui);
    }

    public void saveUIFile(Path file, String version) {
        String fullFileName = file.getFileName().toString();
        String fileName = fullFileName;//TODO
        try (InputStream stream = Files.newInputStream(file)) {
            saveUIResult(fileName, stream, version);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveAPIFile(Path file, String version) {
        String fullFileName = file.getFileName().toString();
        String fileName = fullFileName;//TODO
        try (InputStream stream = Files.newInputStream(file)) {
            saveAPIResult(fileName, stream, version);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getFile(String fileName) {
        //        while (gridFSapi.find().iterator().hasNext()) {
        //            gridFSapi.find().iterator().next();
        //        }
    }

    public void getToFile(String fileName, Path filePath, GridFSBucket fs) throws IOException {
        FileOutputStream streamToDownloadTo = new FileOutputStream(filePath.toString());
        fs.downloadToStream(fileName, streamToDownloadTo);
        streamToDownloadTo.close();
    }

    public InputStream getToStream(String fileName, GridFSBucket fs) throws IOException {
        return fs.openDownloadStream(fileName);
    }

    public void updateStatus(String pkgName, String status) {
        pkgName = stripPkgName(pkgName);
        topList.updateOne(new Document("pkg_name", pkgName), new Document("$set", new Document("status", status)));
    }
}
