package org.nsu.fit.tests.api.auth;


import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.nsu.fit.services.rest.RestClient;
import org.nsu.fit.services.rest.data.AccountTokenPojo;
import org.nsu.fit.services.rest.data.CustomerPojo;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

public class CustomerTest {
    private RestClient restClient;

    private AccountTokenPojo adminToken;
    private AccountTokenPojo customerToken;
    private CustomerPojo customerPojo;

    @BeforeClass
    public void beforeClass() {
        restClient = new RestClient();
    }

    @Test(description = "Authenticate as admin")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Authentication as customer feature")
    public void authAsAdminTest() {
        adminToken = restClient.authenticate("admin", "setup");
        assertNotNull(adminToken);
    }

    @Test(description = "Create new customer from admin", dependsOnMethods = "authAsAdminTest")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Authentication as customer feature")
    public void authAsCustomerTest() {
        customerPojo = new RestClient().createAutoGeneratedCustomer(adminToken);
        customerToken = restClient.authenticate(customerPojo.login, customerPojo.pass);
    }

    @AfterClass
    public void afterClass() {
        restClient.deleteCustomer(customerPojo, adminToken);
    }

}
