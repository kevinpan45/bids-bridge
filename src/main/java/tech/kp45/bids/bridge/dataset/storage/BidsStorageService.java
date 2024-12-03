package tech.kp45.bids.bridge.dataset.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.opendal.Entry;
import org.apache.opendal.Operator;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.exception.BasicRuntimeException;

@Slf4j
@Service
public class BidsStorageService {

    private String bidsDescriptionFileName = "dataset_description.json";

    private Operator getOperator() {
        final Map<String, String> conf = new HashMap<>();
        conf.put("endpoint", "http://192.168.31.124:9000");
        conf.put("region", "us-east-1");
        conf.put("bucket", "bids");
        String accessKey = System.getenv("LOCAL_MINIO_ACCESS_KEY");
        String secretKey = System.getenv("LOCAL_MINIO_SECRET_KEY");
        if (StringUtils.hasText(secretKey) && StringUtils.hasText(secretKey)) {
            conf.put("access_key_id", accessKey);
            conf.put("secret_access_key", secretKey);

            Operator op = Operator.of("s3", conf);
            return op;
        } else {
            log.error("Access key or secret key is not set");
            throw new BasicRuntimeException("Access key or secret key is not set");
        }
    }

    public List<String> listPath() {
        List<String> paths = new ArrayList<>();
        Operator op = getOperator();
        List<Entry> obs = op.list("/");
        if (!obs.isEmpty()) {
            for (Entry ob : obs) {
                if (ob.metadata.isDir()) {
                    paths.add(ob.path);
                }
            }
        }
        return paths;
    }

    private BidsDescription getBidsDescription(String path) {
        Operator op = getOperator();
        byte[] contentBytes = op.read(path + bidsDescriptionFileName);
        String content = new String(contentBytes);
        return new BidsDescription(content);
    }

    private boolean isBids(String path) {
        try {
            return getBidsDescription(path) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public BidsDataset initialize(String path) {
        BidsDescription bidsDescription = getBidsDescription(path);
        return bidsDescription.toBidsDataset();
    }

    /**
     * List all files in the path
     * 
     * @param files container to store the file paths
     * @param path  the path to list files
     */
    public void listFiles(List<String> files, String path) {
        if (isBids(path)) {
            
        }
        Operator op = getOperator();
        List<Entry> obs = op.list(path);
        if (!obs.isEmpty()) {
            for (Entry ob : obs) {
                if (ob.metadata.isDir()) {
                    listFiles(files, ob.path);
                } else {
                    files.add(ob.path);
                }
            }
        }
    }

    public static void main(String[] args) {
        List<String> files = new ArrayList<>();
        new BidsStorageService().listFiles(files, "ds005616/");
        files.forEach(file -> {
            System.out.println(file);
        });
    }
}
