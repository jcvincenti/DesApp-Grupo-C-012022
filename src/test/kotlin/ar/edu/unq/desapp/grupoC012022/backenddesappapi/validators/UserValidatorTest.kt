package ar.edu.unq.desapp.grupoC012022.backenddesappapi.validators

import ar.edu.unq.desapp.grupoC012022.backenddesappapi.builders.UserBuilder
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.models.User
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.repositories.UserRepository
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.exceptions.UserAlreadyExistsException
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.services.validators.UserValidator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class UserValidatorTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @InjectMocks
    private lateinit var subject: UserValidator

    private lateinit var user: User
    private val userBuilder = UserBuilder()

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        user = userBuilder
            .createUserWithValues()
            .id(912)
            .firstName("Gonzalo")
            .lastName("Fernandez")
            .email("someawesomeemail@testing.com")
            .build()
        `when`(userRepository.findByEmail("someawesomeemail@testing.com")).thenReturn(user)
    }

    @Test
    fun testExceptionIsThrownIfUserEmailAlreadyExists() {
        val otherUser = userBuilder
            .createUserWithValues()
            .id(158)
            .firstName("Pepe")
            .lastName("Rodriguez")
            .email("someawesomeemail@testing.com")
            .build()
        assertThrows<UserAlreadyExistsException> { subject.validateUser(otherUser) }
    }

    @Test
    fun testExceptionIsNotThrownIfUserEmailAlreadyExistsButIsSameId() {
        assertDoesNotThrow { subject.validateUser(user) }
    }

    @Test
    fun testExceptionIsNotThrownIfUserEmailDoesNotExists() {
        `when`(userRepository.findByEmail("someawesomeemail@testing.com")).thenReturn(null)
        assertDoesNotThrow { subject.validateUser(user) }
    }
}