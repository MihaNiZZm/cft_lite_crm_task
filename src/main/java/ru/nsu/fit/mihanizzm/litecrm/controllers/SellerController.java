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
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.SellerRequestDto;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.SellerResponseDto;
import ru.nsu.fit.mihanizzm.litecrm.models.dtos.TransactionResponseDto;
import ru.nsu.fit.mihanizzm.litecrm.services.SellerService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/sellers")
@Tag(name = "Seller CRUD")
public class SellerController {
    private final SellerService sellerService;

    @Operation(
            summary = "Returns all sellers",
            description = "Returns all sellers from a database."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved"
            )
    })
    @GetMapping()
    public ResponseEntity<List<SellerResponseDto>> getAllSellers() {
        return ResponseEntity.ok(sellerService.getAllSellers());
    }

    @Operation(
            summary = "Returns a seller with given id",
            description = "Returns a seller with given id from a database."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Seller with given id not found"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<SellerResponseDto> getSellerById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(sellerService.getSellerById(id));
    }

    @Operation(
            summary = "Returns all transactions of a seller with given id",
            description = "Returns all transactions of a seller with given id from a database."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Seller with given id not found"
            )
    })
    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionsBySellerId(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(sellerService.getSellerTransactions(id));
    }

    @Operation(
            summary = "Creates a new seller",
            description = "Creates a new seller and saves it to a database."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Successfully created"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request"
            )
    })
    @PostMapping()
    public ResponseEntity<SellerResponseDto> createSeller(
            @RequestBody SellerRequestDto sellerRequestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(sellerService.createSeller(sellerRequestDto));
    }

    @Operation(
            summary = "Updates a seller with given id",
            description = "Updates a seller with given id and saves it to a database."
    )
    @ApiResponses(
            value = {
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
                            description = "Seller with given id not found"
                    )
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<SellerResponseDto> updateSeller(
            @PathVariable Integer id,
            @RequestBody SellerRequestDto sellerRequestDto
    ) {
        return ResponseEntity.ok(sellerService.updateSeller(id, sellerRequestDto));
    }

    @Operation(
            summary = "Deletes a seller with given id",
            description = "Deletes a seller with given id and removes it from a database."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Successfully deleted"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Seller with given id not found"
                    )
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSeller(@PathVariable("id") Integer id) {
        sellerService.deleteSeller(id);
        return ResponseEntity
                .noContent()
                .build();
    }
}
