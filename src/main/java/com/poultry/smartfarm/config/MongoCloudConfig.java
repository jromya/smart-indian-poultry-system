package com.poultry.smartfarm.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import java.util.concurrent.TimeUnit;

/**
 * Cloud-resilient MongoDB configuration for Render, Railway & MongoDB Atlas.
 * - Automatically strips accidental surrounding quotes/spaces in MONGODB_URI
 * - Automatically defaults database name to 'smart_indian_poultry_db' even if omitted in mongodb+srv://.../
 */
@Configuration
public class MongoCloudConfig {

    private static final Logger log = LoggerFactory.getLogger(MongoCloudConfig.class);
    private static final String DEFAULT_DB_NAME = "smart_indian_poultry_db";

    @Value("${spring.data.mongodb.uri:mongodb://localhost:27017/smart_indian_poultry_db}")
    private String rawMongoUri;

    @Bean
    public MongoClient mongoClient() {
        String cleanUri = sanitizeUri(rawMongoUri);
        ConnectionString connectionString = new ConnectionString(cleanUri);

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .applyToSocketSettings(socket -> socket.connectTimeout(20, TimeUnit.SECONDS))
                .applyToClusterSettings(cluster -> cluster.serverSelectionTimeout(30, TimeUnit.SECONDS))
                .build();

        log.info("Initializing MongoDB Client for hosts={} (SRV={})",
                connectionString.getHosts(), connectionString.isSrvProtocol());
        return MongoClients.create(settings);
    }

    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient) {
        String cleanUri = sanitizeUri(rawMongoUri);
        ConnectionString connectionString = new ConnectionString(cleanUri);
        String dbName = connectionString.getDatabase();
        if (dbName == null || dbName.isBlank()) {
            dbName = DEFAULT_DB_NAME;
        }
        log.info("Using MongoDB database '{}'", dbName);
        return new SimpleMongoClientDatabaseFactory(mongoClient, dbName);
    }

    static String sanitizeUri(String input) {
        if (input == null || input.isBlank()) {
            return "mongodb://localhost:27017/" + DEFAULT_DB_NAME;
        }
        String trimmed = input.trim();
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\""))
                || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }
        return trimmed;
    }
}
