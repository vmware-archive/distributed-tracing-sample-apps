namespace BeachShirts.Common.Models
{
    public class Order
    {
        public Order(string styleName, int quantity)
        {
            StyleName = styleName;
            Quantity = quantity;
        }

        public string StyleName { get; set; }

        public int Quantity { get; set; }
    }
}
