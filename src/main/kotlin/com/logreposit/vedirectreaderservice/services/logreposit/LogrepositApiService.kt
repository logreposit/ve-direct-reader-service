package com.logreposit.vedirectreaderservice.services.logreposit

import com.logreposit.vedirectreaderservice.communication.vedirect.VeDirectField
import com.logreposit.vedirectreaderservice.services.logreposit.dtos.ingress.IngressDefinition
import com.logreposit.vedirectreaderservice.services.logreposit.mappers.LogrepositIngressDataMapper
import com.logreposit.vedirectreaderservice.communication.vedirect.VeDirectReading
import com.logreposit.vedirectreaderservice.communication.vedirect.VeDirectValueType
import com.logreposit.vedirectreaderservice.configuration.LogrepositConfiguration
import com.logreposit.vedirectreaderservice.logger
import com.logreposit.vedirectreaderservice.services.logreposit.dtos.ingress.DataType
import com.logreposit.vedirectreaderservice.services.logreposit.dtos.ingress.FieldDefinition
import com.logreposit.vedirectreaderservice.services.logreposit.dtos.ingress.MeasurementDefinition
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class LogrepositApiService(
        restTemplateBuilder: RestTemplateBuilder,
        private val logrepositConfiguration: LogrepositConfiguration
) {
    private val logger = logger()
    private val deviceDefinition = buildDefinition()

    private val restTemplate: RestTemplate = restTemplateBuilder
            .setConnectTimeout(Duration.of(10, ChronoUnit.SECONDS))
            .setReadTimeout(Duration.of(10, ChronoUnit.SECONDS))
            .build()

    @Retryable(
            value = [RestClientException::class],
            exclude = [HttpClientErrorException.UnprocessableEntity::class],
            maxAttempts = 5,
            backoff = Backoff(delay = 500)
    )
    fun pushData(receivedAt: Instant, veDirectData: List<VeDirectReading<Any>>) {
        val data = LogrepositIngressDataMapper.toLogrepositIngressDto(
            date = receivedAt,
            data = veDirectData,
            address = "1" // Address is currently hardcoded, in future maybe configurable
        )

        val url = logrepositConfiguration.apiBaseUrl + "/v2/ingress/data"

        logger.info("Sending data to Logreposit API ({}): {}", url, data)

        // TODO DoM: comment in again
        // val response = restTemplate.postForObject(url, HttpEntity(data, createHeaders(logrepositConfiguration.deviceToken)), String::class.java)
        // logger.info("Response from Logreposit API: {}", response)
    }

    @Recover
    fun recoverUnprocessableEntity(e: HttpClientErrorException.UnprocessableEntity, veDirectData: List<VeDirectReading<Any>>) {
        logger.warn("Error while sending data to Logreposit API. Got unprocessable entity. Most likely a device definition validation error.", veDirectData, e)
        logger.warn("Updating device ingress definition ...")

        val url = logrepositConfiguration.apiBaseUrl + "/v2/ingress/definition"

        restTemplate.put(url, HttpEntity(deviceDefinition, createHeaders(logrepositConfiguration.deviceToken)))
    }

    @Recover
    fun recoverThrowable(e: Throwable, veDirectData: List<VeDirectReading<Any>>) {
        logger.error("Could not send data to Logreposit API: {}", veDirectData, e)

        throw e
    }

    private fun buildDefinition() = IngressDefinition(
        measurements = listOf(
            MeasurementDefinition(
                name = "data",
                tags = setOf("device_address"),
                fields = VeDirectField.values().map {
                    FieldDefinition(
                        name = it.logrepositName,
                        datatype = mapDataType(it.valueType),
                        description = it.logrepositDescription
                    )
                })
            )
        )

    private fun mapDataType(veDirectValueType: VeDirectValueType) = when (veDirectValueType) {
        VeDirectValueType.NUMBER -> DataType.INTEGER
        VeDirectValueType.ON_OFF -> DataType.INTEGER
        VeDirectValueType.TEXT -> DataType.STRING
        VeDirectValueType.HEX -> DataType.INTEGER
    }

    private fun createHeaders(deviceToken: String?): HttpHeaders {
        val httpHeaders = HttpHeaders()

        httpHeaders["x-device-token"] = deviceToken

        return httpHeaders
    }
}