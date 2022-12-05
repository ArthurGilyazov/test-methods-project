package org.nsu.fit.tm_backend.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.nsu.fit.tm_backend.repository.data.CustomerPojo;
import org.nsu.fit.tm_backend.service.CustomerService;
import org.nsu.fit.tm_backend.service.StatisticService;
import org.nsu.fit.tm_backend.service.SubscriptionService;
import org.nsu.fit.tm_backend.service.data.StatisticBO;
import org.nsu.fit.tm_backend.service.data.StatisticPerCustomerBO;
import org.nsu.fit.tm_backend.service.impl.StatisticServiceImpl;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

// Лабораторная 2: покрыть unit тестами класс StatisticServiceImpl на 100%.
// Чтобы протестировать метод calculate() используйте Mockito.spy(statisticService) и переопределите метод
// calculate(UUID customerId) чтобы использовать стратегию "разделяй и властвуй".
public class StatisticServiceImplTest {

    @Mock
    private CustomerService customerService;

    @Mock
    private SubscriptionService subscriptionService;

    @Spy
    @InjectMocks
    private StatisticService statisticService = new StatisticServiceImpl();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Check success calculate")
    void successCalculate() {

        Set<UUID> customerIds = new HashSet<>();
        UUID firstCustomerId = UUID.randomUUID();
        UUID secondCustomerId = UUID.randomUUID();
        UUID thirdCustomerId = UUID.randomUUID();

        customerIds.add(firstCustomerId);
        customerIds.add(secondCustomerId);
        customerIds.add(thirdCustomerId);

        doReturn(customerIds).when(customerService).getCustomerIds();

        StatisticPerCustomerBO firstCustomerStatistic = new StatisticPerCustomerBO();
        firstCustomerStatistic.setCustomerId(firstCustomerId);
        firstCustomerStatistic.setOverallBalance(100);
        firstCustomerStatistic.setOverallFee(10);

        StatisticPerCustomerBO thirdCustomerStatistic = new StatisticPerCustomerBO();
        thirdCustomerStatistic.setCustomerId(firstCustomerId);
        thirdCustomerStatistic.setOverallBalance(300);
        thirdCustomerStatistic.setOverallFee(30);

        doReturn(firstCustomerStatistic).when(statisticService).calculate(eq(firstCustomerId));
        doReturn(thirdCustomerStatistic).when(statisticService).calculate(eq(thirdCustomerId));

        StatisticBO statistic = statisticService.calculate();

        StatisticBO targetStatistic = StatisticBO.builder()
            .customers(Set.of(firstCustomerStatistic, thirdCustomerStatistic))
            .overallBalance(400)
            .overallFee(40)
            .build();

        Assertions.assertEquals(statistic, targetStatistic);
    }

    @Test
    void successCalculateUUID() {
        CustomerPojo customer1 = new CustomerPojo();
        customer1.id = UUID.randomUUID();
        customer1.firstName = "John";
        customer1.lastName = "Wick";
        customer1.login = "john_wick@example.com";
        customer1.pass = "Baba_Jaga";
        customer1.balance = 100;

        UUID subscriptionUUID = UUID.randomUUID();

        CustomerPojo customer2 = new CustomerPojo();
        customer2.id = UUID.randomUUID();
        customer2.firstName = "Ryan";
        customer2.lastName = "Gosling";
        customer2.login = "ryan_gosling@example.com";
        customer2.pass = "Kojey_Bessmertnii";
        customer2.balance = 250;

        UUID subscription2UUID = UUID.randomUUID();

        UUID randomUUID = UUID.randomUUID();

        HashSet<UUID> customerIds = new HashSet<>();
        customerIds.add(customer1.id);
        customerIds.add(customer2.id);
        customerIds.add(randomUUID);

        doReturn(customerIds).when(customerService).getCustomerIds();

        StatisticPerCustomerBO statistic1 = new StatisticPerCustomerBO();
        statistic1.setOverallBalance(customer1.balance);
        statistic1.setOverallFee(399);
        HashSet<UUID> expectedIds1 = new HashSet<>();
        expectedIds1.add(subscriptionUUID);
        statistic1.setSubscriptionIds(expectedIds1);

        StatisticPerCustomerBO statistic2 = new StatisticPerCustomerBO();
        statistic2.setOverallBalance(customer2.balance);
        statistic2.setOverallFee(499);
        HashSet<UUID> expectedIds2 = new HashSet<>();
        expectedIds2.add(subscription2UUID);
        statistic2.setSubscriptionIds(expectedIds2);

        doReturn(statistic1).when(statisticService).calculate(customer1.id);
        doReturn(statistic2).when(statisticService).calculate(customer2.id);
        doReturn(null).when(statisticService).calculate(randomUUID);

        HashSet<StatisticPerCustomerBO> customers = new HashSet<>();
        customers.add(statistic1);
        customers.add(statistic2);

        StatisticBO statistics = statisticService.calculate();

        StatisticBO expectedStatistics = new StatisticBO();
        expectedStatistics.setOverallBalance(350);
        expectedStatistics.setOverallFee(898);
        expectedStatistics.setCustomers(customers);

        Assertions.assertEquals(statistics, expectedStatistics);
    }

}
