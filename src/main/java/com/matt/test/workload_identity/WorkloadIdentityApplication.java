package com.matt.test.workload_identity;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WorkloadIdentityApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkloadIdentityApplication.class, args);
	}


	@Bean
	public RedissonClient redissonClient(){
		DefaultAzureCredential build = new DefaultAzureCredentialBuilder().build();
		String token = build.getToken(new TokenRequestContext().addScopes("https://redis.azure.com/.default")).block().getToken();
		Config config = new Config();
		config.useSingleServer()
				.setAddress("rediss://m01redis.redis.cache.windows.net:6380")
				.setUsername("c8c406a9-bfdd-45d9-84af-df9dc55be555")
				.setPassword(token);
		RedissonClient redissonClient = Redisson.create(config);
		return redissonClient;
	}
}
