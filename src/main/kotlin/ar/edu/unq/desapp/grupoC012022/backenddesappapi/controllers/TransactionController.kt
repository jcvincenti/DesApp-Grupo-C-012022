package ar.edu.unq.desapp.grupoC012022.backenddesappapi.controllers

import ar.edu.unq.desapp.grupoC012022.backenddesappapi.dtos.OperatedVolumeRequestDto
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.dtos.OperatedVolumeResultDto
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.dtos.TransactionCompletedDto
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.dtos.TransactionDto
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.services.transaction.TransactionService
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.Authorization
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/transactions")
class TransactionController {

    @Autowired
    private lateinit var transactionService: TransactionService

    @ApiOperation(value = "", authorizations = [Authorization(value = "jwtToken")])
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    @RequestMapping(
        method = [RequestMethod.POST],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun processTransaction(@RequestBody transaction: TransactionDto): TransactionCompletedDto {
        return transactionService.processTransaction(transaction)
    }

    @ApiOperation(value = "", authorizations = [Authorization(value = "jwtToken")])
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @RequestMapping(
        "/volume",
        method = [RequestMethod.POST],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getOperatedVolumeBetweenDates(@RequestBody operatedVolumeRequestDto: OperatedVolumeRequestDto): OperatedVolumeResultDto {
        return transactionService.getOperatedVolumeBetweenDates(operatedVolumeRequestDto)
    }
}