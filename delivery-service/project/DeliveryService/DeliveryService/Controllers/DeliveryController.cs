using DeliveryService.Models;
using DeliveryService.Services;
using Microsoft.AspNetCore.Diagnostics;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace DeliveryService.Controllers
{
    [ApiController]
    [Route("[controller]")]
    public class DeliveryController : ControllerBase
    {
        private readonly ILogger<DeliveryController> _logger;
        private readonly DeliveryContextService _dlvService;

        public DeliveryController(ILogger<DeliveryController> logger,
                                                DeliveryContextService dlvService)
        {
            _logger = logger;
            _dlvService = dlvService;
        }

        //get delivery by order id
        [HttpGet("track/{id:regex(^[[a-f0-9]]{{24}}$)}")]
        public async Task<ActionResult<DeliveryContext>> TrackOrder([FromRoute]string id)
        {
            return await _dlvService.GetAsync(id);
        }

        //get all delivery by restaurant id
        [HttpGet("status/{id:regex(^[[a-f0-9]]{{24}}$)}")]
        public async Task<ActionResult<List<DeliveryContext>>> RestaurantAllOrders([FromRoute]string id)
        {
            return await _dlvService.GetByRestaurantAsync(id);
        }

        //confirm/cancel/deliver by orderid -> all put
        [HttpPut("status")]
        public async Task<ActionResult<bool>> UpdateStatus([FromBody]DeliveryContext ctx)
        {
            return await _dlvService.UpdateStatus(ctx);
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
