namespace OrderService.Models
{
    public partial class OrderContext
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
