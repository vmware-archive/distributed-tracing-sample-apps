namespace BeachShirts.Common.Models
{
    public class ShirtStyle
    {
        public ShirtStyle(string name, string imageUrl)
        {
            Name = name;
            ImageUrl = imageUrl;
        }

        public string Name { get; set; }

        public string ImageUrl { get; set; }
    }
}
