namespace DeliveryService.Models
{
    public partial class DeliveryContext
    {
        public enum Status
        {
            RECEIVED,
            CONFIRMED,
            PAYMENT_SUCCESS,
            PAYMENT_FAIL,
            USER_CANCELLED,
            RESTAURANT_CANCELLED,
            DELIVERED
        }
    }
}
