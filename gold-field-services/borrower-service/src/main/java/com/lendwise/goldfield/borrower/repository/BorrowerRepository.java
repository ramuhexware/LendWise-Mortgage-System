package com.lendwise.goldfield.borrower.repository;

import com.lendwise.goldfield.borrower.model.Borrower;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive MongoDB repository for borrowers.
 */
@Repository
public interface BorrowerRepository extends ReactiveMongoRepository<Borrower, String> {

    Mono<Borrower> findByBorrowerId(String borrowerId);

    Flux<Borrower> findByLoanId(String loanId);

    Mono<Borrower> findBySsn(String ssn);

    Flux<Borrower> findByStatus(Borrower.BorrowerStatus status);
}
