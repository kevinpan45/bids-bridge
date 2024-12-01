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

    private String datasetDescriptionFileName = "dataset_description.json";

    private Operator getOperator() {
        final Map<String, String> conf = new HashMap<>();
        conf.put("endpoint", "http://192.168.31.124:9000");
        conf.put("region", "us-east-1");
        conf.put("bucket", "bids");
        String accessKey = System.getenv("LOCAL_MINIO_ACCESS_KEY");
        String secretKey = System.getenv("LOCAL_MINIO_SECRET_KEY");
        if (StringUtils.hasText(secretKey) && StringUtils.hasText(secretKey)) {
            log.info("access {} and secret {}", accessKey, secretKey);
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

    public BidsDescription getDatasetDescription(String path) {
        Operator op = getOperator();
        byte[] contentBytes = op.read(path + datasetDescriptionFileName);
        String content = new String(contentBytes);
        return new BidsDescription(content);
    }

    public BidsDataset initialize(String path) {
        BidsDescription bidsDescription = getDatasetDescription(path);
        return bidsDescription.toBidsDataset();
    }

    public static void main(String[] args) {
        BidsStorageService service = new BidsStorageService();
        List<String> paths = service.listPath();
        paths.forEach(path -> {
            log.info(path);
        });
        BidsDescription description = service.getDatasetDescription(paths.get(0));
        log.info(description.getContent());
    }
}
