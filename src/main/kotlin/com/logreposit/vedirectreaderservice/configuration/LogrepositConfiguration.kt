package com.logreposit.vedirectreaderservice.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "logreposit")
class LogrepositConfiguration {
    var apiBaseUrl: String? = null
    var deviceToken: String? = null
    var includeLegacyFields: Boolean? = null
    var ignoredFields: List<String>? = null
    var minimumUpdateIntervalInMillis: Long? = null
    var address: String? = null
}
