package com.logreposit.vedirectreaderservice.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "logreposit")
class LogrepositConfiguration {
    var apiBaseUrl: String? = null
    var deviceToken: String? = null
    var includeLegacyFields: Boolean? = null
    var scrapeIntervalInMillis: Long? = null
}
