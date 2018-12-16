using System.Collections.Generic;

namespace BeachShirts.Common.Models
{
    public class PackedShirts
    {
        public PackedShirts(List<Shirt> shirts)
        {
            Shirts = shirts;
        }

        public List<Shirt> Shirts { get; set; }
    }
}
