package ar.edu.unq.desapp.grupoC012022.backenddesappapi.services.transaction

import ar.edu.unq.desapp.grupoC012022.backenddesappapi.models.*
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.repositories.TransactionRepository
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.services.CurrencyService
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.services.OrderService
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.services.UserService
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.exceptions.CancelOrderDuePriceDifferenceException
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.exceptions.CantConfirmReceptionOnSellOrders
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * The user from the order wants to buy.
 * The order must have a BUY type.
 * Since the user from the order is the buyer, the money transfer will be done FROM his
 * mercado pago CVU TO the executing user mercado pago CVU.
 * The cripto exchange will be done FROM the executing user wallet to the user from the order
 * wallet.
 */

@Component
class TransactionConfirmReception @Autowired constructor(
    userService: UserService,
    currencyService: CurrencyService,
    mercadoPagoApi: MercadoPagoApi,
    criptoExchanger: CriptoExchanger,
    transactionRepository: TransactionRepository,
    orderService: OrderService
) : TransactionConfirmBase(
    userService,
    currencyService,
    mercadoPagoApi,
    criptoExchanger,
    transactionRepository,
    orderService
) {
    override fun doProcess(order: Order, executingUser: User): Transaction {
        val transaction = saveTransaction(order, Status.APPROVED)
        transferCriptoCurrency(order.quantity, order.price.askCurrency.ticker, executingUser.walletAddress, order.user.walletAddress)
        return transaction
    }

    override fun checkBidCurrencyVariation(order: Order) {
        // Si al momento de concretar una intención de compra,
        // la cotización de sistema está por encima del precio manifestado por el usuario,
        // la misma debe cancelarse.
        val currency = currencyService.getOrUpdateCurrency(order.price.bidCurrency.ticker)
        // Si la diferencia es mayor a un 5%, se elimina la orden
        if (currency.usdPrice > order.price.bidCurrency.usdPrice * 1.05) {
            throw CancelOrderDuePriceDifferenceException()
        }
    }

    @Throws(CantConfirmReceptionOnSellOrders::class)
    override fun checkActionAgainstOrderAction(order: Order) {
        if (order.operation == Operation.SELL) {
            throw CantConfirmReceptionOnSellOrders()
        }
    }

    override fun doGetOppositeTransactionConfirm(
        userService: UserService,
        currencyService: CurrencyService,
        mercadoPagoApi: MercadoPagoApi,
        criptoExchanger: CriptoExchanger,
        transactionRepository: TransactionRepository,
        orderService: OrderService
    ): TransactionConfirmBase {
        return TransactionConfirmTransfer(userService, currencyService, mercadoPagoApi, criptoExchanger, transactionRepository, orderService)
    }
}