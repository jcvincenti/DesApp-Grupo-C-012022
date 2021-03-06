package ar.edu.unq.desapp.grupoC012022.backenddesappapi.models

import ar.edu.unq.desapp.grupoC012022.backenddesappapi.dtos.DeserializableUser
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.exceptions.InvalidPropertyException
import com.fasterxml.jackson.annotation.JsonFilter
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.swagger.v3.oas.annotations.media.Schema
import javax.persistence.*

@Entity
@Table(name = "users")
@JsonIgnoreProperties(value = ["id", "reputation", "operationsAmount"], allowGetters = true)
@JsonFilter("userFilter")
open class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @field:Schema(hidden = true) var id: Int? = 0,
    @Column(nullable = false) var firstName: String,
    @Column(nullable = false) var lastName: String,
    @Column(nullable = false, unique = true) var email: String,
    @Column(nullable = false) var homeAddress: String,
    @Column(nullable = false) var password: String,
    @Column(nullable = false, name = "mercado_pago_cvu") var mercadoPagoCVU: String,
    @Column(nullable = false) var walletAddress: String,
    @Schema(hidden = true) @Column(nullable = false) var operationsAmount: Int? = 0,
    @Schema(hidden = true) @Column(nullable = false) var reputation: Int? = 0
) {
    private val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
    private val passRegex = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@\$ %^&*-]).{8,}\$"

    fun validate() {
        validateFirstName()
        validateLastName()
        validateEmail()
        validateHomeAddres()
        validatePass()
        validateMPCVU()
        validateWalletAddress()
    }

    fun increaseReputationBy(amount: Int) {
        reputation = reputation?.plus(amount)
    }

    fun increaseOperationsAmount() {
        operationsAmount = operationsAmount?.plus(1)
    }

    fun decreaseReputationBy(amount: Int) {
        reputation = reputation?.minus(amount)
    }

    fun toDeserializableUser(): DeserializableUser {
        return DeserializableUser(id!!, firstName, lastName, email, homeAddress, mercadoPagoCVU, walletAddress, operationsAmount, reputation,password)
    }

    private fun validateFirstName() {
        if (firstName.length < 3)
            throw InvalidPropertyException()
        if (firstName.length > 30)
            throw InvalidPropertyException()
    }

    private fun validateLastName() {
        if (lastName.length < 3)
            throw InvalidPropertyException()
        if (lastName.length > 30)
            throw InvalidPropertyException()
    }

    private fun validateEmail() {
        if (email.isEmpty())
            throw InvalidPropertyException()
        if (!isEmailValid(email))
            throw InvalidPropertyException()
    }

    private fun validateHomeAddres() {
        if (homeAddress.length < 10)
            throw InvalidPropertyException()
        if (homeAddress.length > 30)
            throw InvalidPropertyException()
    }

    private fun validatePass() {
        if (password.isEmpty())
            throw InvalidPropertyException()
        if (!isPassStrong(password))
            throw InvalidPropertyException()
    }

    private fun validateMPCVU() {
        if (mercadoPagoCVU.length != 22)
            throw InvalidPropertyException()
    }

    private fun validateWalletAddress() {
        if (walletAddress.length != 8)
            throw InvalidPropertyException()
    }

    private fun isEmailValid(email: String): Boolean {
        return emailRegex.toRegex().matches(email)
    }

    private fun isPassStrong(email: String): Boolean {
        return passRegex.toRegex().matches(email)
    }
}