package tech.kp45.bids.bridge.dataset.storage;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.opendal.Entry;
import org.apache.opendal.Operator;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import cn.hutool.core.io.FileUtil;
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

    public List<String> listBidsPath() {
        List<String> paths = new ArrayList<>();
        getOperator().list("/").forEach(ob -> {
            if (ob.metadata.isDir()) {
                if (isBids(ob.path)) {
                    paths.add(ob.path);
                } else {
                    log.warn("The path {} is not a BIDS dataset", ob.path);
                }
            }
        });

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
     * List file and dir in the current path
     * 
     * @param files container to store the file paths
     * @param path  the path to list files
     */
    public List<String> listPath(String path) {
        if (!isBids(path)) {
            throw new BasicRuntimeException("The path is not a BIDS dataset");
        }
        List<String> paths = new ArrayList<>();
        getOperator().list(path).forEach(ob -> {
            paths.add(ob.path);
        });
        return paths;
    }

    /**
     * Recursively scan the path and store the file paths in the filesContainer
     * 
     * @param path
     * @param filesContainer
     */
    public void scanFiles(String path, List<String> filesContainer) {
        getOperator().list(path).forEach(ob -> {
            if (ob.metadata.isDir()) {
                scanFiles(ob.path, filesContainer);
            } else {
                filesContainer.add(ob.path);
            }
        });
    }

    public List<String> listSub(String path) {
        List<String> subs = new ArrayList<>();
        getOperator().list(path).forEach(ob -> {
            if (ob.metadata.isDir() && getCurrentInPath(ob.path).startsWith("sub-")) {
                subs.add(ob.path);
            }
        });
        return subs;
    }

    public boolean derived(String path) {
        boolean derived = false;
        List<Entry> obs = getOperator().list(path);
        for (Entry ob : obs) {
            String pathName = getCurrentInPath(ob.path);
            if (ob.metadata.isDir() && "derivatives".equals(pathName)) {
                derived = true;
                break;
            }
        }
        return derived;
    }

    public List<String> listDerivatives(String path) {
        List<String> derivatives = new ArrayList<>();
        getOperator().list(path + "/derivatives/").forEach(ob -> {
            if (ob.metadata.isDir()) {
                derivatives.add(ob.path);
            }
        });
        return derivatives;
    }

    public File getDescriptorFile(String path) {
        BidsDescription bidsDescription = getBidsDescription(path);
        String tmpFilePath = System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString()
                + File.separator + path + bidsDescriptionFileName;
        File file = new File(tmpFilePath);
        FileUtil.writeString(bidsDescription.getContent(), file, StandardCharsets.UTF_8);
        log.info("BIDS dataset description file is saved to {}", file.getAbsolutePath());
        return file;
    }

    public File getParticipantFile(String path) {
        String tmpFilePath = System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString()
                + File.separator + path + "participants.tsv";
        File file = new File(tmpFilePath);
        byte[] bytes = getOperator().read(path + "participants.tsv");
        FileUtil.writeBytes(bytes, file);
        log.info("BIDS dataset participants file is saved to {}", file.getAbsolutePath());
        return file;
    }

    /**
     * sample ds005616/derivatives -> derivatives
     * ds005616/sub-01 -> sub-01
     * ds005616/dataset_description.json -> dataset_description.json
     * 
     * @param path
     * @return the current file or dir name in path
     */
    private String getCurrentInPath(String path) {
        String[] pathArr = path.split("/");
        if (path.endsWith("/")) {
            return pathArr[pathArr.length - 1];
        } else {
            return pathArr[pathArr.length - 2];
        }
    }

    public static void main(String[] args) {
        String testPath = "ds005616/";
        BidsStorageService service = new BidsStorageService();

        service.listSub(testPath).forEach(subPath -> {
            System.out.println(service.getCurrentInPath(subPath));
        });

        service.listDerivatives(testPath).forEach(derivative -> {
            System.out.println(derivative);
        });

        boolean derived = service.derived(testPath);
        System.out.println("Derived: " + derived);
    }
}
