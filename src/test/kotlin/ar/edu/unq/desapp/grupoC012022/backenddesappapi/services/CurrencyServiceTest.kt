package ar.edu.unq.desapp.grupoC012022.backenddesappapi.services

import ar.edu.unq.desapp.grupoC012022.backenddesappapi.apis.BinanceApi
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.builders.BinanceApiMockBuilder
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.builders.CurrencyBuilder
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.helpers.MockitoHelper
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.repositories.CurrencyRepository
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.exceptions.CurrencyNotSupportedException
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.models.Currency
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations

class CurrencyServiceTest {

	@Mock
	private lateinit var binanceApiMock: BinanceApi
	@Mock
	private lateinit var currencyRepositoryMock: CurrencyRepository
	@Mock
	private lateinit var redisTemplateMock: RedisTemplate<String, Currency>
	@Mock
	private lateinit var valueOperationsMocks: ValueOperations<String, Currency>

	@InjectMocks
	private lateinit var subject : CurrencyService
	private val currencyBuilder = CurrencyBuilder()

	@BeforeEach
	fun setUp() {
		MockitoAnnotations.openMocks(this)
		val binanceApiMockBuilder = BinanceApiMockBuilder(this.binanceApiMock)
		binanceApiMockBuilder
			.mockCurrency("BNB", 1.01)
			.mockCurrency("BTC", 40000.1254)
			.prepareMock()
		val bnbCurrency = currencyBuilder.createCurrencyWithValues().ticker("BNBUSDT").usdPrice(1.01).build()
		val btcCurrency = currencyBuilder.createCurrencyWithValues().ticker("BTCUSDT").usdPrice(40000.1254).build()
		val btcCurrency2 = currencyBuilder.createCurrencyWithValues().ticker("BTCUSDT").usdPrice(30000.1254).build()
		`when`(currencyRepositoryMock.findByTickerAndTimestampGreaterThanOrderByTickerAscTimestampDesc(MockitoHelper.anyObject(), MockitoHelper.anyObject())).thenReturn(
			listOf(btcCurrency, btcCurrency2)
		)
		`when`(currencyRepositoryMock.findFirstByTickerOrderByTimestampDesc("BNBUSDT")).thenReturn(bnbCurrency)
		`when`(currencyRepositoryMock.findFirstByTickerOrderByTimestampDesc("BTCUSDT")).thenReturn(btcCurrency)
		`when`(redisTemplateMock.opsForValue()).thenReturn(valueOperationsMocks)
		`when`(valueOperationsMocks.get(any())).thenReturn(null)
	}

	@Test
	fun getCurrenciesTest() {
		val criptos = this.subject.getCurrencies()
		assertEquals(2, criptos.size)
	}

	@Test
	fun getCurrencyTest() {
		val cripto = this.subject.getOrUpdateCurrency("BNB")
		assertEquals("BNBUSDT", cripto.ticker)
		assertEquals(1.01, cripto.usdPrice)
	}

	@Test
	fun updateCurrencyTest() {
		val cripto = this.subject.updateCurrency("BNB")
		assertEquals("BNBUSDT", cripto.ticker)
		assertEquals(1.01, cripto.usdPrice)
	}

	@Test
	fun updateCurrencyNotSupportedTest() {
		assertThrows<CurrencyNotSupportedException> {
			this.subject.updateCurrency("SOMECRIPTO")
		}
	}

	@Test
	fun getPricesTest() {
		val criptos = this.subject.getPrices("BTCUSDT")
		assertEquals(2, criptos.size)
		assertEquals("BTCUSDT", criptos.first().ticker)
		assertEquals(40000.1254, criptos.first().usdPrice)
		assertNotNull(criptos.first().timestamp)
		assertEquals("BTCUSDT", criptos.last().ticker)
		assertEquals(30000.1254, criptos.last().usdPrice)
		assertNotNull(criptos.last().timestamp)
	}
}
