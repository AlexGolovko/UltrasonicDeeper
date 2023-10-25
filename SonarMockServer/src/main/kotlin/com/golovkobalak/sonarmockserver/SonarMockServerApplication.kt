package com.golovkobalak.sonarmockserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@SpringBootApplication
class SonarMockServerApplication

fun main(args: Array<String>) {
	runApplication<SonarMockServerApplication>(*args)
}


@Configuration
@EnableWebSocket
class WebSocketConfig : WebSocketConfigurer {
	override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
		registry.addHandler(myHandler(), "/sonar").setAllowedOrigins("*")
	}

	@Bean
	fun myHandler(): TextWebSocketHandler {
		return MyWebSocketHandler()
	}
}

class MyWebSocketHandler : TextWebSocketHandler() {
	private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

	override fun afterConnectionEstablished(session: WebSocketSession) {
		executor.scheduleAtFixedRate({
			val depth = (3..15).random()
			val data = """{
  "status": 200,
  "depth": $depth,
  "battery": 3.57,
  "temperature": -273
}
"""
			session.sendMessage(TextMessage(data))
		}, 0, 100, TimeUnit.MILLISECONDS)
	}
}