package ru.nsu.fit.mihanizzm.litecrm.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.TransactionRequestDto;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.TransactionResponseDto;
import ru.nsu.fit.mihanizzm.litecrm.services.TransactionService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transaction CRUD")
public class TransactionController {
    private final TransactionService transactionService;

    @Operation(
            summary = "Returns all transactions",
            description = "Returns all transactions from a database."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved"
            )
    })
    @GetMapping()
    public ResponseEntity<List<TransactionResponseDto>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @Operation(
            summary = "Returns a transaction with given id",
            description = "Returns a transaction with given id from a database."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transaction with given id not found"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDto> getTransactionById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @Operation(
            summary = "Creates a new transaction",
            description = "Creates a new transaction and saves it to a database."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Successfully created"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Seller with such seller_id not found"
            )
    })
    @PostMapping()
    public ResponseEntity<TransactionResponseDto> createTransaction(
            @RequestBody TransactionRequestDto transactionRequestDto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transactionService.createTransaction(transactionRequestDto));
    }

    @Operation(
            summary = "Updates a transaction with given id",
            description = "Updates a transaction with given id and saves it to a database."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully updated"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transaction with given id not found"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Seller with such seller_id not found"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponseDto> updateTransaction(
            @PathVariable("id") Integer id,
            @RequestBody TransactionRequestDto transactionRequestDto
    ) {
        return ResponseEntity.ok(transactionService.updateTransaction(id, transactionRequestDto));
    }

    @Operation(
            summary = "Deletes a transaction with given id",
            description = "Deletes a transaction with given id and removes it from a database."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Successfully deleted"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transaction with given id not found"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable("id") Integer id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity
                .noContent()
                .build();
    }
}
