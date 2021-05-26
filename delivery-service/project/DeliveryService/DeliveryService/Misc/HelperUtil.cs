using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using DeliveryService.Models;

namespace DeliveryService.Misc
{
    public class HelperUtil
    {

        private static Dictionary<string, string> TopicNameMapping = new Dictionary<string, string>();

        static HelperUtil() {
            TopicNameMapping.Add("PaymentStatus", "fo_pay_sta");
            TopicNameMapping.Add("OrderConfirmed", "fo_ord_cnf");
            TopicNameMapping.Add("OrderCancelled", "fo_ord_can");
            TopicNameMapping.Add("OrderReceived", "fo_ord_rec");
            TopicNameMapping.Add("BuyerContact", "fo_buyer_pref_contact");
            TopicNameMapping.Add("OrderDelivered", "fo_ord_dlv");
        }

        public static string PropertyList(object obj)
        {
            var props = obj.GetType().GetProperties();
            var sb = new StringBuilder();
            sb.Append("{");
            foreach (var p in props)
            {
                sb.Append(p.Name + ": " + p.GetValue(obj, null) + ", ");
            }
            sb.Append("}");
            return sb.ToString();
        }

        public static string GetTopicName(string modelName) 
        {
            TopicNameMapping.TryGetValue(modelName, out string topic);
            return topic;
        }

        public static OrderCancelled GetCancelTopicMsgBody(DeliveryContext item) {
            return new OrderCancelled(item);
        }

        public static OrderConfirmed GetConfirmedTopicMsgBody(DeliveryContext item)
        {
            return new OrderConfirmed(item);
        }

        public static OrderDelivered GetDeliveredTopicMsgBody(DeliveryContext item)
        {
            return new OrderDelivered(item);
        }

    }
}
