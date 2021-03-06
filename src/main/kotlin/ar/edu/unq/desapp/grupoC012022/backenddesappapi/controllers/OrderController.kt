package ar.edu.unq.desapp.grupoC012022.backenddesappapi.controllers

import ar.edu.unq.desapp.grupoC012022.backenddesappapi.dtos.OfferedOrderDto
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.dtos.OrderDto
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.dtos.OrderSavedDto
import ar.edu.unq.desapp.grupoC012022.backenddesappapi.services.OrderService
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.Authorization
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
class OrderController {

    @Autowired
    lateinit var orderService: OrderService

    @ApiOperation(value = "", authorizations = [Authorization(value = "jwtToken")])
    @PostMapping("/orders")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody orderDto: OrderDto) : OrderSavedDto? {
        return orderService.create(orderDto).toOrderSavedDto()
    }

    @ApiOperation(value = "", authorizations = [Authorization(value = "jwtToken")])
    @GetMapping("/orders")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    fun getActives() : List<OfferedOrderDto> {
        return orderService.getActives()
    }
}