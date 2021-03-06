package ar.edu.unq.desapp.grupoC012022.backenddesappapi.services

import ar.edu.unq.desapp.grupoC012022.backenddesappapi.builders.OrderBuilder
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.builders.PriceBuilder
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.builders.TransactionBuilder
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.builders.UserBuilder
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.models.*
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.repositories.TransactionRepository
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.services.transaction.CriptoExchanger
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.services.transaction.MercadoPagoApi
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.services.transaction.TransactionConfirmTransfer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.*
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime

@SpringBootTest
class TransactionConfirmTransferTest {

    @Mock
    private lateinit var userService: UserService
    @Mock
    private lateinit var transactionRepository: TransactionRepository
    @Mock
    private lateinit var currencyService: CurrencyService
    @Mock
    private lateinit var orderService: OrderService
    @Spy
    private lateinit var mercadoPagoApiMock: MercadoPagoApi
    @Spy
    private lateinit var criptoExchangerMock: CriptoExchanger
    @Autowired @InjectMocks
    private lateinit var subject: TransactionConfirmTransfer

    private lateinit var userFromOrder: User
    private lateinit var executingUser: User
    private lateinit var order: Order
    private lateinit var transactionsCaptor: ArgumentCaptor<Transaction>

    private val orderBuilder = OrderBuilder()
    private val userBuilder = UserBuilder()
    private val priceBuilder = PriceBuilder()
    private val transactionBuilder = TransactionBuilder()

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        userFromOrder = userBuilder.createUserWithValues().id(1).reputation(10).build()
        executingUser = userBuilder.createUserWithValues().id(2).reputation(10).build()
        `when`(userService.save(userFromOrder)).thenReturn(userFromOrder)
        `when`(userService.save(executingUser)).thenReturn(executingUser)
        `when`(transactionRepository.save(any())).thenReturn(transactionBuilder.createTransactionWithValues().build())
        transactionsCaptor = ArgumentCaptor.forClass(Transaction::class.java)
    }

    @Test
    fun processedOrderWithLessThan30MinutesAdds10PointsOfReputationTest() {
        order = orderBuilder
            .createOrderWithValues()
            .user(userFromOrder)
            .price(
                priceBuilder
                    .createPriceWithValues()
                    .timestamp(
                        LocalDateTime.now().minusMinutes(20)
                    ).build()
            )
            .operation(Operation.SELL)
            .build()
        `when`(currencyService.getOrUpdateCurrency("BTC")).thenReturn(Currency(ticker = "BTC", usdPrice = 50000.0))
        subject.process(order, executingUser)
        assert(userFromOrder.reputation == 20)
        assert(executingUser.reputation == 20)
        verify(transactionRepository, times(2)).save(transactionsCaptor.capture())
        assert(Status.APPROVED == transactionsCaptor.allValues[0].status)
        assert(executingUser.id == transactionsCaptor.allValues[0].user.id)
        assert(Status.APPROVED == transactionsCaptor.allValues[1].status)
        assert(order.user.id == transactionsCaptor.allValues[1].user.id)
    }

    @Test
    fun processedOrderWithMoreThan30MinutesAdds5PointsOfReputationTest() {
        order = orderBuilder
            .createOrderWithValues()
            .user(userFromOrder)
            .price(
                priceBuilder
                    .createPriceWithValues()
                    .timestamp(
                        LocalDateTime.now().minusMinutes(35)
                    ).build()
            )
            .operation(Operation.SELL)
            .build()
        `when`(currencyService.getOrUpdateCurrency("BTC")).thenReturn(Currency(ticker = "BTC", usdPrice = 50000.0))
        subject.process(order, executingUser)
        assert(userFromOrder.reputation == 15)
        assert(executingUser.reputation == 15)
        verify(transactionRepository, times(2)).save(transactionsCaptor.capture())
        assert(Status.APPROVED == transactionsCaptor.value.status)
    }

    @Test
    fun processedOrderWithMoreThanFivePercentOfDiffIsCanceledTest() {
        order = orderBuilder
            .createOrderWithValues()
            .user(userFromOrder)
            .price(
                priceBuilder
                    .createPriceWithValues()
                    .timestamp(
                        LocalDateTime.now().minusMinutes(35)
                    )
                    .bidCurrency(Currency(ticker = "BTC", usdPrice = 50000.0))
                    .build()
            )
            .operation(Operation.SELL)
            .build()
        `when`(currencyService.getOrUpdateCurrency("BTC")).thenReturn(Currency(ticker = "BTC", usdPrice = 47000.0))
        `when`(orderService.save(order)).thenReturn(order)
        subject.process(order, executingUser)
        assert(userFromOrder.reputation == 10)
        assert(executingUser.reputation == 10)
        verify(transactionRepository, times(1)).save(transactionsCaptor.capture())
        assert(Status.CANCELED == transactionsCaptor.value.status)
    }
}