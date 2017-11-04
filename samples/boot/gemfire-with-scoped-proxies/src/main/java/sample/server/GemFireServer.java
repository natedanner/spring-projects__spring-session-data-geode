/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sample.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.CacheServerConfigurer;
import org.springframework.data.gemfire.config.annotation.EnableManager;
import org.springframework.session.data.gemfire.config.annotation.web.http.EnableGemFireHttpSession;

/**
 * A Spring Boot application bootstrapping a GemFire Cache Server JVM process.
 *
 * @author John Blum
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.session.data.gemfire.config.annotation.web.http.EnableGemFireHttpSession
 * @see org.apache.geode.cache.Cache
 * @since 1.2.1
 */
@SuppressWarnings("unused")
// tag::class[]
@SpringBootApplication
@CacheServerApplication(name = "SpringSessionDataGeodeServerWithScopedProxiesBootSample", logLevel = "config")
@EnableGemFireHttpSession(maxInactiveIntervalInSeconds = 10)
@EnableManager(start = true)
public class GemFireServer {

	public static void main(String[] args) {

		new SpringApplicationBuilder(GemFireServer.class)
			.web(WebApplicationType.NONE)
			.build()
			.run(args);
	}

	@Bean
	static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	CacheServerConfigurer cacheServerPortConfigurer(
			@Value("${spring.session.data.geode.cache.server.port:40404}") int port) {

		return (beanName, cacheServerFactoryBean) ->
			cacheServerFactoryBean.setPort(port);
	}
}
// end::class[]
