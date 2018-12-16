namespace BeachShirts.Common
{
    public static class Configuration
    {

        public static string Host { get; } = "localhost";

        public static int ShoppingPort { get; } = 50050;

        public static int StylingPort { get; } = 50051;

        public static int DeliveryPort { get; } = 50052;
    }
}
