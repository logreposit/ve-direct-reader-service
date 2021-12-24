package com.logreposit.vedirectreaderservice

import com.logreposit.vedirectreaderservice.communication.vedirect.VeDirectSerialClient
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(properties = ["spring.profiles.active=test"])
class ApplicationTests {
	// We don't have the serial port available at test invocation time
	@MockBean private lateinit var veDirectSerialClient: VeDirectSerialClient

	@Test
	fun contextLoads() {
	}
}
