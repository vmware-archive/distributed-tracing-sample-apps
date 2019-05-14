namespace BeachShirts.Common.Models
{
    public class DeliveryStatus
    {
        public DeliveryStatus(string orderNum, string trackingNum, string status)
        {
            OrderNum = orderNum;
            TrackingNum = trackingNum;
            Status = status;
        }

        public string OrderNum { get; set; }

        public string TrackingNum { get; set; }

        public string Status { get; set; }
    }
}
