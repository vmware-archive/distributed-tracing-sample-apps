namespace BeachShirts.Common.Models
{
    public class Shirt
    {
        public Shirt(ShirtStyle style)
        {
            Style = style;
        }

        public ShirtStyle Style { get; set; }
    }
}
