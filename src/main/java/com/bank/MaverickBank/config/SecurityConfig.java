package com.bank.MaverickBank.config;


import com.bank.MaverickBank.service.MyUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.hibernate.engine.jdbc.batch.spi.Batch;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final MyUserDetailsService myUserDetailsService ;
    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        //anyone can be a customer with proper details
                        .requestMatchers(HttpMethod.POST,"/api/customer/add").permitAll()
                        //validate the login credentials and gives jwt tokens
                        .requestMatchers(HttpMethod.GET,"/api/auth/login").authenticated()
                        //Gets the details of Logged in User for validation
                        .requestMatchers(HttpMethod.GET,"/api/auth/user-details").authenticated()
                        //this will be done in the backend once to add the admin and then will be closed no need of frontend for it
                        .requestMatchers(HttpMethod.POST,"/api/admin/add").permitAll()
                        //Only admin can add branches
                        .requestMatchers(HttpMethod.POST,"/api/branch/add").hasAuthority("ADMIN")
                          //No need for this one
//                        //Only admin can add multiple branches
//                        .requestMatchers(HttpMethod.POST,"/api/branch/batch/add").hasAuthority("ADMIN")
                          //Batch Processing not needed
//                        //Only admin can add Employees
//                        .requestMatchers(HttpMethod.POST,"/api/employee/batch/add").hasAuthority("ADMIN")
                        //Only Admin can modify Employee Details
                        .requestMatchers(HttpMethod.PUT,"/api/admin/employee/{employeeId}").hasAuthority("ADMIN")
                        //Only Admin can modify User Details
                        .requestMatchers(HttpMethod.PUT,"/api/admin/user/{userId}").hasAuthority("ADMIN")
                        //Only Admin can see stats of employees per branch
                        .requestMatchers(HttpMethod.GET,"/api/admin/employee/stat").hasAuthority("ADMIN")
                        //Only Admin can see stats of account per branch
                        .requestMatchers(HttpMethod.GET,"/api/admin/account/stat").hasAuthority("ADMIN")
                        //Only Admin can deactivate users
                        .requestMatchers(HttpMethod.PUT,"/api/admin/user/{userId}/deactivate").hasAuthority("ADMIN")
                        //Only Admin can filter through users
                        .requestMatchers(HttpMethod.GET,"/api/admin/user/filter").hasAuthority("ADMIN")
                        //Only Admin can filter through users
                        .requestMatchers(HttpMethod.POST,"/api/admin/user/add").hasAuthority("ADMIN")


                        //Both Admin and Employee can see all branches
                        .requestMatchers(HttpMethod.GET,"/api/account/filter").hasAnyAuthority("ADMIN","EMPLOYEE")
                        //Only Employee and ADMIN can filter all accounts
                        .requestMatchers(HttpMethod.GET,"/api/branch/filter").hasAnyAuthority("ADMIN","EMPLOYEE")
                        //Both Admin and Employee can see all Customers
                        .requestMatchers(HttpMethod.GET,"/api/customer/filter").hasAnyAuthority("ADMIN","EMPLOYEE")
                        //Both Admin and Employee can see all Employees
                        .requestMatchers(HttpMethod.GET,"/api/employee/filter").hasAnyAuthority("ADMIN","EMPLOYEE")
                        //Only Employee can manage Account requests
                        .requestMatchers(HttpMethod.PUT,"/api/account/request").hasAuthority("EMPLOYEE")
                        //Only the Loan Branch Manager Employee can assign the loans to branch Employees
                        .requestMatchers(HttpMethod.PUT,"/api/loan/assign").hasAuthority("EMPLOYEE")
                        //Only the Assigned Employee can approve the loan
                        .requestMatchers(HttpMethod.PUT,"/api/loan/approve").hasAuthority("EMPLOYEE")
                        //Only the Assigned Employee can Reject the loan
                        .requestMatchers(HttpMethod.PUT,"/api/loan/reject/{loanId}").hasAuthority("EMPLOYEE")
                        //Only the Manager Employee can disburse a loan from the same branch
                        .requestMatchers(HttpMethod.PUT,"/api/loan/disburse/{loanId}").hasAuthority("EMPLOYEE")
                        //Only the Manager Employee can reject a loan from the same branch after its accepted
                        .requestMatchers(HttpMethod.PUT,"/api/loan/manager/reject/{loanId}").hasAuthority("EMPLOYEE")
                        //Only the Assigned Employee can Close the assigned loan
                        .requestMatchers(HttpMethod.PUT,"/api/loan/complete/{loanId}").hasAuthority("EMPLOYEE")
                        //Only Employee can access all transactions
                        .requestMatchers(HttpMethod.GET,"/api/transaction/filter").hasAuthority("EMPLOYEE")
                        //Only Employee can access all Loans
                        .requestMatchers(HttpMethod.GET,"/api/loan/filter").hasAuthority("EMPLOYEE")
                        //Only Employee can access all beneficiaries
                        .requestMatchers(HttpMethod.GET,"/api/beneficiary/filter").hasAuthority("EMPLOYEE")
                        //Only Employee can see the Branch stat for loan status
                        .requestMatchers(HttpMethod.GET,"/api/loan/status/stat").hasAuthority("EMPLOYEE")
                        //Only Employee can see the Branch stat for loan type
                        .requestMatchers(HttpMethod.GET,"/api/loan/type/stat").hasAuthority("EMPLOYEE")
                        //Only Employee can see the stats of the number of accounts in each RequestStatus in their branch
                        .requestMatchers(HttpMethod.GET,"/api/account/request-status/stat").hasAuthority("EMPLOYEE")


                        //Only Customer can add Account
                        .requestMatchers(HttpMethod.POST,"/api/account/add").hasAuthority("CUSTOMER")
                        //Only Customer can add their Picture
                        .requestMatchers(HttpMethod.POST,"/api/account/picture").hasAuthority("CUSTOMER")
                        //Only Customer can add Account
                        .requestMatchers(HttpMethod.POST,"/api/account/identification-doc").hasAuthority("CUSTOMER")
                        //Only Customer can deposit the amount in their account
                        .requestMatchers(HttpMethod.POST,"/api/transaction/deposit").hasAuthority("CUSTOMER")
                        //Only Customer can deposit the amount in their account
                        .requestMatchers(HttpMethod.POST,"/api/transaction/withdraw").hasAuthority("CUSTOMER")
                        //Only Customer can transfer the amount in their account to other accounts
                        .requestMatchers(HttpMethod.POST,"/api/transaction/transfer").hasAuthority("CUSTOMER")
                        //Only Customer can transfer the amount in their account to other accounts
                        .requestMatchers(HttpMethod.GET,"/api/transaction/customer/statement").hasAuthority("CUSTOMER")
                        //Customers can only filter their own accounts
                        .requestMatchers(HttpMethod.GET,"/api/account/customer/filter").hasAuthority("CUSTOMER")
                        //Customers can see the status of their upload
                        .requestMatchers(HttpMethod.GET,"/api/customer/upload-status").hasAuthority("CUSTOMER")
                        //Customers can only filter their own transactions
                        .requestMatchers(HttpMethod.GET,"/api/transaction/customer/filter").hasAuthority("CUSTOMER")
                        //Only Customer can add their Beneficiaries
                        .requestMatchers(HttpMethod.POST,"/api/beneficiary/add").hasAuthority("CUSTOMER")
                        //Only Customer can delete their Beneficiaries
                        .requestMatchers(HttpMethod.DELETE,"/api/beneficiary/delete/{id}").hasAuthority("CUSTOMER")
                        //Only Customer can add their Beneficiaries in Batches
                        .requestMatchers(HttpMethod.POST,"/api/beneficiary/batch/add").hasAuthority("CUSTOMER")
                        //Customer can only filter through their beneficiaries
                        .requestMatchers(HttpMethod.POST,"/api/beneficiary/customer/filter").hasAuthority("CUSTOMER")
                        //Only Customer can add Loan
                        .requestMatchers(HttpMethod.POST,"/api/loan/add").hasAuthority("CUSTOMER")
                        //Only Customer can add Verification Document
                        .requestMatchers(HttpMethod.POST,"/api/loan/upload-document/{id}").hasAuthority("CUSTOMER")
                        //Only Employee can access all Loans
                        .requestMatchers(HttpMethod.GET,"/api/loan/customer/filter").hasAnyAuthority("CUSTOMER")
                        //Only Customer can Accept their own loan after Approved
                        .requestMatchers(HttpMethod.PUT,"/api/loan/customer/accept/{loanId}").hasAuthority("CUSTOMER")
                        //Only Customer can Reject their own loan after Approved
                        .requestMatchers(HttpMethod.PUT,"/api/loan/customer/reject/{loanId}").hasAuthority("CUSTOMER")
                        //Customers can only view their own info
                        .requestMatchers(HttpMethod.GET,"/api/customer/profile").hasAuthority("CUSTOMER")
                        //Customers can only view their own info
                        .requestMatchers(HttpMethod.PUT,"/api/customer/profile").hasAuthority("CUSTOMER")
                        //Only Customer can request Close of an account
                        .requestMatchers(HttpMethod.PUT,"/api/account/close").hasAuthority("CUSTOMER")
                        .anyRequest().permitAll()
                );

        http.addFilterBefore(jwtFilter , UsernamePasswordAuthenticationFilter.class);

        http.httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public AuthenticationProvider getProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(myUserDetailsService);
        authenticationProvider.setPasswordEncoder(getEncoder());
        return authenticationProvider;
    }

    @Bean
    public PasswordEncoder getEncoder(){
        return new BCryptPasswordEncoder();
    }
}
