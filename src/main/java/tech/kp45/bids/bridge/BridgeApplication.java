package tech.kp45.bids.bridge;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import tech.kp45.bids.bridge.dataset.storage.BidsStorageRegister;

@MapperScan("tech.kp45.bids.bridge")
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(BidsStorageRegister.class)
public class BridgeApplication {

	public static void main(String[] args) {
		SpringApplication.run(BridgeApplication.class, args);
	}

}
