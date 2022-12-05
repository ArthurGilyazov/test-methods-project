package org.nsu.fit.tm_backend.impl;

import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.nsu.fit.tm_backend.repository.Repository;
import org.nsu.fit.tm_backend.repository.data.ContactPojo;
import org.nsu.fit.tm_backend.repository.data.CustomerPojo;
import org.nsu.fit.tm_backend.service.impl.CustomerServiceImpl;
import org.nsu.fit.tm_backend.service.impl.auth.data.AuthenticatedUserDetails;
import org.nsu.fit.tm_backend.shared.Authority;
import org.nsu.fit.tm_backend.shared.Globals;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Лабораторная 2: покрыть unit тестами класс CustomerServiceImpl на 100%.
@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {
    @Mock
    private Repository repository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    @DisplayName("Correct Date")
    void testCreateCustomer() {
        // arrange: готовим входные аргументы и настраиваем mock'и.
        CustomerPojo createCustomerInput = new CustomerPojo();
        createCustomerInput.firstName = "John";
        createCustomerInput.lastName = "Wick";
        createCustomerInput.login = "john_wick@example.com";
        createCustomerInput.pass = "Baba_Jaga";
        createCustomerInput.balance = 0;

        CustomerPojo createCustomerOutput = new CustomerPojo();
        createCustomerOutput.id = UUID.randomUUID();
        createCustomerOutput.firstName = "John";
        createCustomerOutput.lastName = "Wick";
        createCustomerOutput.login = "john_wick@example.com";
        createCustomerOutput.pass = "Baba_Jaga";
        createCustomerOutput.balance = 0;

        when(repository.createCustomer(createCustomerInput)).thenReturn(createCustomerOutput);

        // act: вызываем метод, который хотим протестировать.
        CustomerPojo customer = customerService.createCustomer(createCustomerInput);

        // assert: проверяем результат выполнения метода.
        assertEquals(customer.id, createCustomerOutput.id);

        // Проверяем, что метод по созданию Customer был вызван ровно 1 раз с определенными аргументами
        verify(repository, times(1)).createCustomer(createCustomerInput);

        // Проверяем, что другие методы не вызывались...
        verify(repository, times(1)).getCustomers();
    }

    @Test
    @DisplayName("Test on create customer with null field")
    void testCreateCustomerWithNullArgument_Wrong() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> customerService.createCustomer(null),
                "Argument 'customer' is null.");
    }


    //    Тесты на создание кастомера(Пароль)
    @Test
    @DisplayName("Test on empty password")
    void testCreateCustomerWithEmptyPassword() {
        // arrange
        CustomerPojo createCustomerInput = new CustomerPojo();
        createCustomerInput.firstName = "John";
        createCustomerInput.lastName = "Wick";
        createCustomerInput.login = "john_wick@example.com";
        createCustomerInput.pass = null;
        createCustomerInput.balance = 0;

        // act-assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> customerService.createCustomer(createCustomerInput),
                "Field 'customer.pass' is null.");
    }

    @Test
    @DisplayName("Test on easy password")
    void testCreateCustomerWithEasyPassword() {
        // arrange
        CustomerPojo createCustomerInput = new CustomerPojo();
        createCustomerInput.firstName = "John";
        createCustomerInput.lastName = "Wick";
        createCustomerInput.login = "john_wick@example.com";
        createCustomerInput.pass = "123qwe";
        createCustomerInput.balance = 0;

        // act-assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> customerService.createCustomer(createCustomerInput),
                "Password is very easy.");
    }

    @Test
    @DisplayName("Test on a short password")
    void testCreateCustomerWithShortPassword() {
        // arrange
        CustomerPojo createCustomerInput = new CustomerPojo();
        createCustomerInput.firstName = "John";
        createCustomerInput.lastName = "Wick";
        createCustomerInput.login = "john_wick@example.com";
        createCustomerInput.pass = "123";
        createCustomerInput.balance = 0;

        // act-assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> customerService.createCustomer(createCustomerInput),
                "Password's length should be more or equal 6 symbols and less or equal 12 symbols.");
    }

    @Test
    @DisplayName("Test on long password")
    void testCreateCustomerWithLongPassword() {
        // arrange
        CustomerPojo createCustomerInput = new CustomerPojo();
        createCustomerInput.firstName = "John";
        createCustomerInput.lastName = "Wick";
        createCustomerInput.login = "john_wick@example.com";
        createCustomerInput.pass = "123qwerty123qwe";
        createCustomerInput.balance = 0;

        // act-assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> customerService.createCustomer(createCustomerInput),
                "Password's length should be more or equal 6 symbols and less or equal 12 symbols.");
    }

    //    Тесты на создание кастомера(Имя)
    @Test
    @DisplayName("Test on create 2 equal customer")
    void testCreateCustomer_LoginAlreadyUsed() {
        // Arrange
        CustomerPojo existingCustomer = new CustomerPojo();
        existingCustomer.firstName = "John";
        existingCustomer.lastName = "Wick";
        existingCustomer.login = "john_wick@example.com";
        existingCustomer.pass = "Baba_Jaga";
        existingCustomer.balance = 0;

        CustomerPojo createCustomerInput = new CustomerPojo();
        createCustomerInput.firstName = "Ulrich";
        createCustomerInput.lastName = "Tomsen";
        createCustomerInput.login = "john_wick@example.com";
        createCustomerInput.pass = "Baba_Jaga";
        createCustomerInput.balance = 0;

        when(repository.getCustomers()).thenReturn(Stream.of(existingCustomer).collect(Collectors.toSet()));

        // Assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> customerService.createCustomer(createCustomerInput),
                "User with provided login already exists.");
    }

    @Test
    @DisplayName("Test on short firstName")
    void testCreateCustomerWithShortFirstName() {
        // arrange
        CustomerPojo createCustomerInput = new CustomerPojo();
        createCustomerInput.firstName = "J";
        createCustomerInput.lastName = "Wick";
        createCustomerInput.login = "john_wick@example.com";
        createCustomerInput.pass = "Strong";
        createCustomerInput.balance = 0;

        // act-assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> customerService.createCustomer(createCustomerInput),
                "First Name's length should be more or equal 2 symbols and less or equal 12 symbols.");
    }

    @Test
    @DisplayName("Test in long firstName")
    void testCreateCustomerWithLongFirstName() {
        // arrange
        CustomerPojo createCustomerInput = new CustomerPojo();
        createCustomerInput.firstName = "Johnjohnjohna";
        createCustomerInput.lastName = "Wick";
        createCustomerInput.login = "john_wick@example.com";
        createCustomerInput.pass = "Strong";
        createCustomerInput.balance = 0;

        // act-assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> customerService.createCustomer(createCustomerInput),
                "First Name's length should be more or equal 2 symbols and less or equal 12 symbols.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"john", "JOHN", "J1hn", "Jo hn", "J$hn", " John "})
    @DisplayName("Testing format firstName")
    void testCreateCustomer_FirstNameBadFormat(String firstName) {
        // Arrange
        CustomerPojo createCustomerInput = new CustomerPojo();
        createCustomerInput.firstName = firstName;
        createCustomerInput.lastName = "Wick";
        createCustomerInput.login = "john_wick@example.com";
        createCustomerInput.pass = "123qwerty";
        createCustomerInput.balance = 0;

        // Assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> customerService.createCustomer(createCustomerInput),
                "First name format is invalid.");
    }

    //    Testing getter customers
    @Test
    @DisplayName("Testing getter customer")
    void testGetCustomers() {
        // Arrange
        Set<CustomerPojo> customers = Stream
                .of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                .map(uuid -> {
                    CustomerPojo customer = new CustomerPojo();
                    customer.id = uuid;
                    return customer;
                })
                .collect(Collectors.toSet());

        when(repository.getCustomers()).thenReturn(customers);

        // Act
        Set<CustomerPojo> retrievedCustomers = customerService.getCustomers();

        // Assert
        assertEquals(retrievedCustomers, customers);
    }

    //Testing getters customer IDs
    @Test
    @DisplayName("Testing getter customer IDs")
    void testGetCustomerIds() {
        // Arrange
        Set<UUID> ids = Stream
                .of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                .collect(Collectors.toSet());

        when(repository.getCustomerIds()).thenReturn(ids);

        // Act
        Set<UUID> retrievedIds = customerService.getCustomerIds();

        // Assert
        assertEquals(retrievedIds, ids);
//        assertThat(retrievedIds).isEqualTo(ids); TODO remove
    }

    //    Testing getCustomer
    @Test
    @DisplayName("Testing get success customer!")
    void testGetCustomer_Success() {
        // Arrange
        UUID getCustomerInput = UUID.randomUUID();
        CustomerPojo customer = new CustomerPojo();
        customer.id = getCustomerInput;
        customer.firstName = "John";
        customer.lastName = "Wick";
        customer.login = "john_wick@example.com";
        customer.pass = "Baba_Jaga";
        customer.balance = 0;

        when(repository.getCustomer(getCustomerInput)).thenReturn(customer);

        // Act
        CustomerPojo retrievedCustomer = customerService.getCustomer(getCustomerInput);

        // Assert
        assertEquals(retrievedCustomer, customer);
    }

    @Test
    @DisplayName("Testing get not existing customer")
    void testGetCustomer_CustomerDoesNotExist() {
        // Act
        CustomerPojo retrievedCustomer = customerService.getCustomer(UUID.randomUUID());

        // Assert
        assertNull(retrievedCustomer);
    }

// Testing look up
    @Test
    @DisplayName("Testing success look up customer by id")
    void testLookupCustomerById_Success() {
        // Arrange
        UUID lookupCustomerInput = UUID.randomUUID();
        CustomerPojo customer = new CustomerPojo();
        customer.id = lookupCustomerInput;
        customer.firstName = "John";
        customer.lastName = "Wick";
        customer.login = "john_wick@example.com";
        customer.pass = "Baba_Jaga";
        customer.balance = 0;

        when(repository.getCustomers()).thenReturn(Stream.of(customer).collect(Collectors.toSet()));

        // Act
        CustomerPojo retrievedCustomer = customerService.lookupCustomer(lookupCustomerInput);

        // Assert
        assertEquals(retrievedCustomer, customer);
    }

    @Test
    @DisplayName("Testing dont exist Customer")
    void testLookupCustomerById_CustomerDoesNotExist() {
        // Act
        CustomerPojo retrievedCustomer = customerService.lookupCustomer(UUID.randomUUID());

        // Assert
        Assertions.assertNull(retrievedCustomer);
//        assertThat(retrievedCustomer).isNull();
    }


//    Testing look up by login
    @Test
    @DisplayName("Testing success lookUp customer by login")
    void testLookupCustomerByLogin_Success() {
        // Arrange
        String lookupCustomerInput = "john_wick@example.com";
        CustomerPojo customer = new CustomerPojo();
        customer.firstName = "John";
        customer.lastName = "Wick";
        customer.login = lookupCustomerInput;
        customer.pass = "Baba_Jaga";
        customer.balance = 0;

        when(repository.getCustomers()).thenReturn(Stream.of(customer).collect(Collectors.toSet()));

        // Act
        CustomerPojo retrievedCustomer = customerService.lookupCustomer(lookupCustomerInput);

        // Assert
        assertEquals(retrievedCustomer, customer);
    }

    @Test
    @DisplayName("Dont Exist customers")
    void testLookupCustomerByLogin_CustomerDoesNotExist() {
        // Act
        CustomerPojo retrievedCustomer = customerService.lookupCustomer("john_wick@example.com");

        // Assert
        Assertions.assertNull(retrievedCustomer);
    }

//    Testing me admin
    @Test
    @DisplayName("Testing me admin")
    void testMe_Admin() {
        // Arrange
        AuthenticatedUserDetails meInput = new AuthenticatedUserDetails(
                "",
                "",
                Stream.of(Authority.ADMIN_ROLE).collect(Collectors.toSet())
        );
        ContactPojo adminContact = new ContactPojo();
        adminContact.login = Globals.ADMIN_LOGIN;

        // Act
        ContactPojo retrievedContact = customerService.me(meInput);

        // Assert
        assertThat(retrievedContact)
                .usingRecursiveComparison()
                .isEqualTo(adminContact);
    }

//    Testing me user
    @Test
    @DisplayName("Testing me user")
    void testMe_RegularUser() {
        // Arrange
        String login = "john_wick@example.com";
        AuthenticatedUserDetails meInput = new AuthenticatedUserDetails(
                "",
                login,
                Collections.emptySet()
        );
        ContactPojo userContact = new ContactPojo();
        userContact.login = login;

        CustomerPojo customer = new CustomerPojo();
        customer.firstName = "John";
        customer.lastName = "Wick";
        customer.login = "john_wick@example.com";
        customer.pass = "Baba_Jaga";
        customer.balance = 0;

        when(repository.getCustomerByLogin(login)).thenReturn(customer);

        // Act
        ContactPojo retrievedContact = customerService.me(meInput);

        // Assert
        assertThat(retrievedContact)
                .usingRecursiveComparison()
                .isEqualTo(userContact);
    }
//    Testing me not existing user

    @Test
    @DisplayName("Testing Me not existing user")
    void testMe_NonExistentUser() {
        // Act
        ContactPojo retrievedContact = customerService.me(
                new AuthenticatedUserDetails("", "", Collections.emptySet())
        );

        // Assert
        assertThat(retrievedContact).isNull();
    }

//    Testing delete customer
    @Test
    @DisplayName("Testing delete customer")
    void testDeleteCustomer() {
        // Arrange
        UUID deleteCustomerInput = UUID.randomUUID();

        // Act
        customerService.deleteCustomer(deleteCustomerInput);

        // Assert
        verify(repository, times(1)).deleteCustomer(deleteCustomerInput);
    }




    //    Testing method topUpBalance
    @Test
    @DisplayName("Check method top up balance on correct data")
    void testTopUpBalanceOnCorrectData() {
        // Arrange
        UUID topUpBalanceInput = UUID.randomUUID();
        int initialBalance = new Random().nextInt(1000);
        int addedBalance = 50;
        CustomerPojo customer = new CustomerPojo();
        customer.id = topUpBalanceInput;
        customer.balance = initialBalance;

        when(repository.getCustomer(topUpBalanceInput)).thenReturn(customer);

        // Act
        CustomerPojo editedCustomer = customerService.topUpBalance(topUpBalanceInput, addedBalance);

        // Assert
        customer.balance = initialBalance + addedBalance;
        assertThat(editedCustomer).isEqualTo(customer);
        verify(repository, times(1)).editCustomer(customer);
    }

    @Test
    @DisplayName("Check method top up balance on minus balance")
    void testTopUpBalance_NegativeMoney() {
        // Arrange
        UUID topUpBalanceInput = UUID.randomUUID();

        // Act/assert
        assertThatThrownBy(() -> customerService.topUpBalance(topUpBalanceInput, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Money amount must be strictly positive.");

        verify(repository, times(0)).editCustomer(any());
    }

    @Test
    @DisplayName("Check method top up balance on not existing customer")
    void testTopUpBalance_CustomerDoesNotExist() {
        // Act
        CustomerPojo customer = customerService.topUpBalance(UUID.randomUUID(), 1);

        // Assert
        assertThat(customer).isNull();
        verify(repository, times(0)).editCustomer(any());
    }
}
