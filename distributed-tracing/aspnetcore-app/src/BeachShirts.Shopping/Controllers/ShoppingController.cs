using System.Collections.Generic;
using Microsoft.AspNetCore.Mvc;
using BeachShirts.Common.Models;
using BeachShirts.Common;
using Microsoft.Extensions.Configuration;
using System;
using Microsoft.Extensions.Logging;

namespace BeachShirts.Shopping.Controllers
{
    [Route("api/shop")]
    [ApiController]
    public class ShoppingController : Controller
    {
        private readonly string stylingHost;
        private readonly Random random;
        private readonly ILogger logger;

        public ShoppingController(IConfiguration configuration, ILogger<ShoppingController> logger)
        {
            stylingHost = configuration.GetValue("StylingHost", Configuration.Host);
            random = new Random();
            this.logger = logger;
        }

        // GET api/shop/menu
        [Route("menu")]
        [HttpGet]
        public ActionResult<IEnumerable<ShirtStyle>> Menu()
        {
            return Utils.HttpGet<IEnumerable<ShirtStyle>>(
                stylingHost, Configuration.StylingPort, "api/style");
        }

        // POST api/shop/order
        [Route("order")]
        [HttpPost]
        public ActionResult<DeliveryStatus> OrderShirts(Order order)
        {
            if (random.Next(0, 10) == 0)
            {
                string msg = "Failed to order shirts!";
                logger.LogWarning(msg);
                return StatusCode(503, msg);
            }

            if (order.StyleName == null)
            {
                string msg = "StyleName is null";
                logger.LogWarning(msg);
                return BadRequest(msg);
            }
            if (order.Quantity <= 0)
            {
                string msg = "Quantity is not a positive number: " + order.Quantity;
                logger.LogWarning(msg);
                return BadRequest(msg);
            }

            var deliveryResponse = Utils.HttpGet<DeliveryStatus>(
                stylingHost,
                Configuration.StylingPort,
                $"api/style/{order.StyleName}/make/{order.Quantity}"
            );

            if (deliveryResponse.Result is OkObjectResult)
            {
                return deliveryResponse;
            }
            else
            {
                string msg = "Failed to order shirts!";
                logger.LogWarning(msg);
                return StatusCode(((StatusCodeResult)deliveryResponse.Result).StatusCode, msg);
            }
        }
    }
}
