package ar.edu.unq.desapp.grupoC012022.backenddesappapi.models

import ar.edu.unq.desapp.grupoC012022.backenddesappapi.builders.PriceBuilder
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.exceptions.InvalidPropertyException
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PriceTest {
	private val priceBuilder = PriceBuilder()

	@Test
	fun priceCreation() {
		val price = priceBuilder.createPriceWithValues().build()
		assertNotNull(price.bidCurrency)
		assertNotNull(price.sellingPrice)
		assertNotNull(price.askCurrency)
		assertNotNull(price.timestamp)
	}

	@Test
	fun priceCreationWithNegativeSellingPriceThrowsException() {
		assertThrows<InvalidPropertyException> {priceBuilder.createPriceWithValues().sellingPrice(-5).build()}
	}

	@Test
	fun priceCorrectlyCreationButSettingAfterWithNegativeSellingPriceThrowsException() {
		assertThrows<InvalidPropertyException> {
			val price = priceBuilder.createPriceWithValues().sellingPrice(5).build()
			price.sellingPrice = -5
		}
	}
}
