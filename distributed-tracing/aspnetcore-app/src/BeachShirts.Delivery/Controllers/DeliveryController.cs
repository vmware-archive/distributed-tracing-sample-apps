using System;
using BeachShirts.Common.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;

namespace BeachShirts.Delivery.Controllers
{
    [Route("api/delivery")]
    [ApiController]
    public class DeliveryController : Controller
    {
        private readonly Random random;
        private readonly ILogger logger;

        public DeliveryController(ILogger<DeliveryController> logger)
        {
            random = new Random();
            this.logger = logger;
        }

        // POST api/delivery/dispatch/{orderNum}
        [Route("dispatch/{orderNum}")]
        [HttpPost]
        public ActionResult<DeliveryStatus> Dispatch(string orderNum, PackedShirts packedShirts)
        {
            if (random.Next(0, 5) == 0)
            {
                string msg = "Failed to dispatch shirts!";
                logger.LogWarning(msg);
                return StatusCode(503, msg);
            }
            if (random.Next(0, 10) == 0)
            {
                orderNum = "";
            }
            if (string.IsNullOrWhiteSpace(orderNum))
            {
                string msg = "Invalid Order Num";
                logger.LogWarning(msg);
                return BadRequest(msg);
            }
            if (packedShirts == null || packedShirts.Shirts == null || packedShirts.Shirts.Count == 0)
            {
                string msg = "No shirts to deliver";
                logger.LogWarning(msg);
                return BadRequest(msg);
            }
            string trackingNum = Guid.NewGuid().ToString();
            logger.LogInformation($"Tracking number of Order {orderNum} is {trackingNum}");
            return Ok(new DeliveryStatus(orderNum, trackingNum, "shirts delivery dispatched"));
        }

        // POST api/delivery/return/{orderNum}
        [Route("return/{orderNum}")]
        [HttpPost]
        public ActionResult Retrieve(string orderNum)
        {
            if (string.IsNullOrWhiteSpace(orderNum))
            {
                string msg = "Invalid Order Num";
                logger.LogWarning(msg);
                return BadRequest(msg);
            }
            return Ok($"Order {orderNum} returned");
        }
    }
}
