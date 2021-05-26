using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using OrderService.Models;

namespace OrderService.Misc
{
    public class HelperUtil
    {
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
            //TODO: make map static read only
            switch (modelName)
            {
                case "PaymentStatus":
                    return "fo_pay_sta";
                case "OrderConfirmed":
                    return "fo_ord_cnf";
                case "OrderCancelled":
                    return "fo_ord_can";
                case "OrderReceived":
                    return "fo_ord_rec";
                case "OrderDelivered":
                    return "fo_ord_dlv";
                default:
                    return null;
            }
        }

        public static OrderCancelled GetCancelTopicMsgBody(OrderContext item) {
            return new OrderCancelled(item);
        }

        public static OrderReceived GetReceivedTopicMsgBody(OrderContext item)
        {
            return new OrderReceived(item);
        }
    }
}
