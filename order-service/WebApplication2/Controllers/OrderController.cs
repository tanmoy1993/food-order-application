using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Diagnostics;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using OrderService.Models;
using OrderService.Services;

namespace OrderService.Controllers
{
    [ApiController]
    [Route("[controller]")]
    public class OrderController : ControllerBase
    {
        private readonly ILogger<OrderController> _logger;
        private readonly OrderContextService _orderService;

        public OrderController(ILogger<OrderController> logger, OrderContextService orderService)
        {
            _logger = logger;
            _orderService = orderService;
        }

        //create order
        [HttpPost]
        public async Task<ActionResult<OrderContext>> CreateOrder([FromBody]OrderContext order)
        {
            return await _orderService.Create(order);
        }

        //get order status
        [HttpGet("track/{id:regex(^[[a-f0-9]]{{24}}$)}")]
        public async Task<ActionResult<OrderContext>> TrackOrder([FromRoute]string id)
        {
            return await _orderService.GetAsync(id);
        }

        //cancel order
        [HttpPut("cancel")]
        public async Task<IActionResult> CancelOrderAsync([FromBody]OrderContext order)
        {
            bool result = await _orderService.UpdateStatusAndRemove(order.OrderId, OrderContext.Status.USER_CANCELLED);
            if (result) 
                GenericProducer<OrderCancelled>.ordCtxQueue.Enqueue(order);
            return Ok();
        }

        [Route("error")]
        public IActionResult Error()
        {
            var exceptionHandlerPathFeature =
                    HttpContext.Features.Get<IExceptionHandlerPathFeature>();

            return BadRequest(new ExceptionResponse(exceptionHandlerPathFeature.Error.Message));
        }
    }
}
