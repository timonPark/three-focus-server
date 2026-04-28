package com.threefocus.global.config

import org.jooq.SQLDialect
import org.jooq.impl.DataSourceConnectionProvider
import org.jooq.impl.DefaultConfiguration
import org.jooq.impl.DefaultDSLContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class JooqConfig(private val dataSource: DataSource) {

    @Bean
    fun dslContext() = DefaultDSLContext(jooqConfiguration())

    private fun jooqConfiguration() = DefaultConfiguration().apply {
        set(DataSourceConnectionProvider(dataSource))
        set(SQLDialect.POSTGRES)
    }
}
