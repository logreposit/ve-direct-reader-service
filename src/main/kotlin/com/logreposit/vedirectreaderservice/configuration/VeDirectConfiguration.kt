package com.logreposit.vedirectreaderservice.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "vedirect")
class VeDirectConfiguration {
    var comPort: String? = null
}
