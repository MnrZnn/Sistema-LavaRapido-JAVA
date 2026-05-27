package com.estetica.utils;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Configuracoes adicionais do MongoDB.
 * A URI de conexao deve ser definida em application.properties.
 */
@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.estetica.repository")
public class MongoConfig {
    // Adicione aqui customizacoes de converters ou auditing se necessario
}
